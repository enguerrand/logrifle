/*
 *  Copyright 2019, Enguerrand de Rochefort
 *
 * This file is part of logrifle.
 *
 * logrifle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * logrifle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with logrifle.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.logrifle.ui;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import de.logrifle.base.LogDispatcher;
import de.logrifle.base.Patterns;
import de.logrifle.base.Strings;
import de.logrifle.data.bookmarks.Bookmark;
import de.logrifle.data.bookmarks.Bookmarks;
import de.logrifle.data.highlights.Highlight;
import de.logrifle.data.highlights.HighlightsData;
import de.logrifle.data.io.FileOpener;
import de.logrifle.data.parsing.Line;
import de.logrifle.data.parsing.Lines;
import de.logrifle.data.views.DataView;
import de.logrifle.data.views.DataViewFiltered;
import de.logrifle.data.views.UserInputProcessingFailedException;
import de.logrifle.data.views.ViewsTree;
import de.logrifle.data.views.ViewsTreeNode;
import de.logrifle.ui.cmd.CommandHandler;
import de.logrifle.ui.cmd.ExecutionResult;
import de.logrifle.ui.cmd.KeyStrokeHandler;
import de.logrifle.ui.cmd.Query;
import de.logrifle.ui.completion.CommandAutoCompleter;
import de.logrifle.ui.completion.FileArgumentsCompleter;
import de.logrifle.ui.completion.IdArgumentsCompleter;
import de.logrifle.ui.completion.IndexArgumentsCompleter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MainController {
    public static final String COMMAND_PREFIX = ":";
    public static final String FIND_PREFIX = "/";
    public static final String FIND_BACKWARDS_PREFIX = "?";
    private final MainWindow mainWindow;
    private final KeyStrokeHandler keyStrokeHandler;
    private final Deque<Query> queryHistory = new LinkedList<>();
    private final LogDispatcher logDispatcher;
    private final ViewsTree viewsTree;
    private final HighlightsData highlightsData;
    private final Bookmarks bookmarks;
    private final FileOpener logFileOpener;
    private final RingIterator<HighlightingTextColors> highlightsIterator = new RingIterator<>(Arrays.asList(
            HighlightingTextColors.values()
    ));
    private final Charset charset;

    public MainController(
            MainWindow mainWindow,
            CommandHandler commandHandler,
            KeyStrokeHandler keyStrokeHandler,
            LogDispatcher logDispatcher,
            ViewsTree viewsTree,
            HighlightsData highlightsData,
            Bookmarks bookmarks,
            FileOpener logFileOpener,
            Charset charset
    ) {
        this.mainWindow = mainWindow;
        this.keyStrokeHandler = keyStrokeHandler;
        this.logDispatcher = logDispatcher;
        this.viewsTree = viewsTree;
        this.highlightsData = highlightsData;
        this.bookmarks = bookmarks;
        this.logFileOpener = logFileOpener;
        this.charset = charset;
        CommandAutoCompleter commandAutoCompleter = new CommandAutoCompleter(
                COMMAND_PREFIX,
                commandHandler.getAvailableCommands(),
                new IndexArgumentsCompleter(() -> highlightsData.getHighlights().size(), "dh", "delete-highlight", "eh", "edit-highlight"),
                new IdArgumentsCompleter(ViewsTreeNode.NAV_INDEX_LOOKUP::keySet, "jump"),
                new FileArgumentsCompleter(
                        Paths.get(System.getProperty("user.dir")),
                        Strings::expandPathPlaceHolders,
                        "open", "of", "write-bookmarks", "wb", "wcv", "write-current-view"
                )
        );
        this.mainWindow.setCommandAutoCompleter(commandAutoCompleter);
        this.mainWindow.setCommandViewListener(new CommandViewListener() {
            @Override
            public void onCommand(String commandLine) {
                mainWindow.closeCommandBar();
                ExecutionResult result;
                if (commandLine.length() <= 1) {
                    return;
                }
                String prefix = commandLine.substring(0, 1);
                String command = commandLine.substring(1);
                //noinspection IfCanBeSwitch
                if (prefix.equals(COMMAND_PREFIX)) {
                    result = commandHandler.handle(command);
                } else if (prefix.equals(FIND_PREFIX)) {
                    result = find(new Query(command, false, false));
                } else if (prefix.equals(FIND_BACKWARDS_PREFIX)) {
                    result = find(new Query(command, true, false));
                } else {
                    return;
                }
                result.getUserMessage().ifPresent(msg ->
                        mainWindow.showCommandViewMessage(msg, TextColor.ANSI.RED));
                if (result.isUiUpdateRequired()) {
                    mainWindow.updateView();
                }
            }

            @Override
            public void onEmptied() {
                mainWindow.closeCommandBar();
            }
        });
    }

    public ExecutionResult prepareCommand(String args) {
        mainWindow.openCommandBar(args);
        return new ExecutionResult(true);
    }

    public ExecutionResult moveFilterPrev() {
        boolean changed = viewsTree.moveFocusPrev();
        return new ExecutionResult(changed, null);
    }

    public ExecutionResult moveFilterNext() {
        boolean changed = viewsTree.moveFocusNext();
        return new ExecutionResult(changed, null);
    }

    public ExecutionResult moveFilterParent() {
        boolean changed = viewsTree.moveFocusParent();
        return new ExecutionResult(changed, null);
    }

    public ExecutionResult moveFilterFirstChild() {
        boolean changed = viewsTree.moveFocusFirstChild();
        return new ExecutionResult(changed, null);
    }

    public ExecutionResult moveFilterTo(String args) {
        try {
            int navIndex = Integer.parseInt(args);
            boolean changed = viewsTree.moveFocusTo(navIndex);
            String userMessage = changed ? null : navIndex + ": no such view found!";
            return new ExecutionResult(changed, userMessage);
        } catch (NumberFormatException e) {
            return new ExecutionResult(false, args + ": not a valid view index!");
        }
    }

    public ExecutionResult find(Query query) {
        return find(query, false);
    }

    private ExecutionResult find(Query query, boolean noRecordHistory) {
        if (query.getSearchTerm().matches("\\s*")) {
            return new ExecutionResult(false);
        }
        if (!noRecordHistory) {
            this.queryHistory.remove(query);
            this.queryHistory.add(query);
        }
        Pattern p;
        try {
            p = Patterns.compilePatternChecked(query.getSearchTerm());
        } catch (UserInputProcessingFailedException e) {
            return new ExecutionResult(false, e.getMessage());
        }
        LogView logView = this.mainWindow.getLogView();
        int focusedLineIndex = logView.getFocusedLineIndexInView();
        DataView dataView = this.mainWindow.getDataView();
        List<Line> allLines = dataView.getAllLines();
        if (isEofReached(query, focusedLineIndex, allLines)) {
            return new ExecutionResult(false, query.getSearchTerm() + ": End of file reached.");
        }
        if (query.isBackwards()) {
            for (int i = focusedLineIndex - 1; i >= 0; i--) {
                Line line = allLines.get(i);
                if (line.contains(p)) {
                    logView.scrollVertically(i - focusedLineIndex);
                    return new ExecutionResult(true);
                }
            }
        } else {
            for (int i = focusedLineIndex + 1; i < allLines.size(); i++) {
                Line line = allLines.get(i);
                if (line.contains(p)) {
                    logView.scrollVertically(i - focusedLineIndex);
                    return new ExecutionResult(true);
                }
            }
        }

        return new ExecutionResult(false, query.getSearchTerm() + ": pattern not found.");
    }

    public ExecutionResult addFilter(String args, boolean inverted, boolean caseInsensitive, boolean blocking) {
        if (args.isEmpty()) {
            return new ExecutionResult(false, "Missing argument: filter pattern");
        }
        String regex = caseInsensitive ? Patterns.makeCaseInsensitive(args) : args;
        ViewsTree viewsTree = this.viewsTree;
        ViewsTreeNode focusedTreeNode = viewsTree.getFocusedNode();
        DataView focusedView = focusedTreeNode.getDataView();
        try {
            DataViewFiltered dataViewFiltered = new DataViewFiltered(regex, focusedView, inverted, logDispatcher, bookmarks::isLineForcedVisible);
            Runnable treeUpdater = () -> {
                ViewsTreeNode child = new ViewsTreeNode(focusedTreeNode, dataViewFiltered);
                viewsTree.addNodeAndSetFocus(focusedTreeNode, child);
            };
            CompletableFuture<Void> f = CompletableFuture.supplyAsync(
                    () -> {
                        focusedView.addListener(dataViewFiltered);
                        dataViewFiltered.onFullUpdate(focusedView);
                        if (!blocking) {
                            UI.runLater(
                                    () -> {
                                        treeUpdater.run();
                                        mainWindow.updateView();
                                    }
                            );
                        }
                        return null;
                    },
                    logDispatcher
            );
            if (!blocking) {
                return new ExecutionResult(false);
            }
            try {
                f.get();
                treeUpdater.run();
                return new ExecutionResult(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ExecutionResult(false, "Thread was interrupted!");
            } catch (ExecutionException e) {
                return new ExecutionResult(false, e.getCause().toString());
            }
        } catch (UserInputProcessingFailedException e) {
            return new ExecutionResult(false, "Cannot create filter: " + e.getMessage());
        }
    }

    public ExecutionResult addHighlight(String args, boolean caseInsensitive, @Nullable HighlightingTextColors colors) {
        try {
            String regex = caseInsensitive ? Patterns.makeCaseInsensitive(args) : args;
            Highlight highlight = new Highlight(regex, colors != null ? colors : highlightsIterator.next());
            this.highlightsData.addHighlight(highlight);
            return new ExecutionResult(true);
        } catch (UserInputProcessingFailedException e) {
            return new ExecutionResult(false, e.getMessage());
        }
    }

    public ExecutionResult deleteHighlight(String args) {
        try {
            int index = Integer.parseInt(args);
            return this.highlightsData.removeHighlight(index);
        } catch (NumberFormatException e) {
            return new ExecutionResult(false, "Not a valid highlight index: " + args);
        }
    }

    public ExecutionResult editHighlight(String args) {
        int index;
        try {
            index = Integer.parseInt(args);
        } catch (NumberFormatException e) {
            return new ExecutionResult(false, "Not a valid highlight index: " + args);
        }
        Highlight highlight;
        try {
            highlight = this.highlightsData.getHighlights().get(index);
        } catch (RuntimeException e) {
            return new ExecutionResult(false, "No highlight found at index " + index);
        }
        ExecutionResult executionResult = this.highlightsData.removeHighlight(index);
        if (!executionResult.isUiUpdateRequired()) {
            // Due to the previous check this should never happen
            return executionResult;
        }
        // TODO: Would be nice to keep old styles here...
        return prepareCommand(":highlight " + highlight.getRegex());
    }

    public ExecutionResult deleteFilter() {
        ViewsTree viewsTree = this.viewsTree;
        ViewsTreeNode focusedTreeNode = viewsTree.getFocusedNode();
        return viewsTree.removeNode(focusedTreeNode);
    }

    public ExecutionResult editFilter() {
        ViewsTree viewsTree = this.viewsTree;
        ViewsTreeNode focusedTreeNode = viewsTree.getFocusedNode();
        DataView dataView = focusedTreeNode.getDataView();
        if (!(dataView instanceof DataViewFiltered)) {
            return new ExecutionResult(false, "Cannot edit this view!");
        }
        DataViewFiltered filter = (DataViewFiltered) dataView;
        String preparedCommand = ":replace-filter " + filter.getRegex();
        return prepareCommand(preparedCommand);
    }

    public ExecutionResult replaceFilter(String newRegex, boolean blocking) {
        ViewsTree viewsTree = this.viewsTree;
        ViewsTreeNode focusedTreeNode = viewsTree.getFocusedNode();
        DataView focusedDataView = focusedTreeNode.getDataView();
        if (!(focusedDataView instanceof DataViewFiltered)) {
            return new ExecutionResult(false, "Cannot edit this view!");
        }
        DataViewFiltered currentFilter = (DataViewFiltered) focusedDataView;
        currentFilter.updateTitle(newRegex);

        return runAsyncIfPossible(
                () -> currentFilter.setPattern(newRegex),
                blocking,
                "editing filter"
        );
    }

    public ExecutionResult toggleBookmark() {
        @Nullable Line focusedLine = mainWindow.getLogView().getFocusedLine();
        if (focusedLine == null) {
            return new ExecutionResult(false, "No line is currently focused.");
        }
        return bookmarks.toggle(focusedLine);
    }

    public ExecutionResult toggleBookmarkAndMoveFocus(String args) {
        @Nullable Line focusedLine = mainWindow.getLogView().getFocusedLine();
        if (focusedLine == null) {
            return new ExecutionResult(false, "No line is currently focused.");
        }
        ExecutionResult toggleResult = bookmarks.toggle(focusedLine);
        ExecutionResult moveFocusResult = moveFocus(args);
        return ExecutionResult.merged(toggleResult, moveFocusResult);
    }

    public ExecutionResult clearBookmarks() {
        return bookmarks.clear();
    }

    public ExecutionResult writeBookmarks(String path) {
        if (Strings.isBlank(path)) {
            return new ExecutionResult(false, "Argument missing: path");
        }
        LineLabelDisplayMode lineLabelDisplayMode = this.mainWindow.getLogView().getLineLabelDisplayMode();
        Collection<String> export = bookmarks.export(lineLabelDisplayMode);
        if (export.isEmpty()) {
            return new ExecutionResult(false, "Could not write bookmarks: No bookmarks found!");
        }
        return writeToFile(path, "write bookmarks", export);
    }

    public ExecutionResult writeView(String path) {
        if (Strings.isBlank(path)) {
            return new ExecutionResult(false, "Argument missing: path");
        }
        LineLabelDisplayMode lineLabelDisplayMode = this.mainWindow.getLogView().getLineLabelDisplayMode();
        List<Line> linesInView = this.viewsTree.getFocusedNode().getDataView().getAllLines();
        Collection<String> export = Lines.export(linesInView, lineLabelDisplayMode);
        return writeToFile(path, "write view", export);
    }

    private ExecutionResult writeToFile(String path, String operationDescription, Collection<String> linesToWrite) {
        try {
            Files.write(
                    Paths.get(
                            Strings.expandPathPlaceHolders(path)
                    ),
                    linesToWrite,
                    charset,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            return new ExecutionResult(false);
        } catch (IOException e) {
            return new ExecutionResult(false, "Could not " + operationDescription+": " + e);
        }
    }

    public ExecutionResult toggleForceDisplayBookmarks() {
        return bookmarks.toggleForceBookmarksDisplay();
    }

    public ExecutionResult toggleLineNumbers() {
        mainWindow.getLogView().toggleLineNumbers();
        return new ExecutionResult(true);
    }

    public ExecutionResult findAgain() {
        if (this.queryHistory.isEmpty()) {
            return new ExecutionResult(false);
        }
        return find(this.queryHistory.getLast());
    }

    public ExecutionResult findAgainBackwards() {
        if (this.queryHistory.isEmpty()) {
            return new ExecutionResult(false);
        }
        Query last = this.queryHistory.getLast();
        return find(new Query(last.getSearchTerm(), !last.isBackwards(), false), true);
    }

    public ExecutionResult moveFocus(String args) {
        try {
            int lineCount = Integer.parseInt(args);
            return this.mainWindow.getLogView().moveFocus(lineCount);
        } catch (NumberFormatException e) {
            return new ExecutionResult(false, args + ": Not a valid line count");
        }
    }

    public ExecutionResult toggleDetail() {
        return this.mainWindow.getLogView().toggleDetailLine();
    }

    public ExecutionResult quit() {
        try {
            this.mainWindow.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ExecutionResult(false);
    }

    public ExecutionResult refresh() {
        this.mainWindow.updateView();
        return new ExecutionResult(true);
    }

    public ExecutionResult scrollHorizontally(String args) {
        try {
            int columnCount = Integer.parseInt(args);
            return this.mainWindow.getLogView().scrollHorizontally(columnCount);
        } catch (NumberFormatException e) {
            return new ExecutionResult(false, args + ": Not a valid column count");
        }
    }

    public ExecutionResult scrollToLineEnd() {
        return this.mainWindow.getLogView().scrollToLineEnd();
    }

    public ExecutionResult scrollToLineStart() {
        return this.mainWindow.getLogView().scrollToLineStart();
    }

    public ExecutionResult scrollVertically(String args) {
        try {
            int lineCount = Integer.parseInt(args);
            return this.mainWindow.getLogView().scrollVertically(lineCount);
        } catch (NumberFormatException e) {
            return new ExecutionResult(false, args + ": Not a valid line count");
        }
    }

    public ExecutionResult scrollPage(String args) {
        try {
            float factor = Float.parseFloat(args);
            return this.mainWindow.getLogView().scrollPage(factor);
        } catch (NumberFormatException e) {
            return new ExecutionResult(false, args + ": Not a valid factor");
        }
    }

    public ExecutionResult gotoLine(String args) {
        try {
            int index = Integer.parseInt(args);
            return this.mainWindow.getLogView().gotoLine(index);
        } catch (NumberFormatException e) {
            return new ExecutionResult(false, args + ": Not a valid line index");
        }
    }

    public ExecutionResult scrollToTop() {
        return this.mainWindow.getLogView().scrollToStart();
    }

    public ExecutionResult moveFocusToEnd() {
        return this.mainWindow.getLogView().moveFocusToEnd();
    }

    public ExecutionResult scrollToNextBookmark() {
        int lineIndexOfBookmark = this.mainWindow.getLogView().getGlobalIndexOfFocusedLineOrZero();
        Bookmark nextBookmark = this.bookmarks.findNext(lineIndexOfBookmark).orElse(null);
        if (nextBookmark == null) {
            return new ExecutionResult(false);
        }
        return this.mainWindow.getLogView().scrollToLine(nextBookmark.getLine());
    }

    public ExecutionResult scrollToPreviousBookmark() {
        int lineIndexOfBookmark = this.mainWindow.getLogView().getGlobalIndexOfFocusedLineOrZero();
        Bookmark prevBookmark = this.bookmarks.findPrevious(lineIndexOfBookmark).orElse(null);
        if (prevBookmark == null) {
            return new ExecutionResult(false);
        }
        return this.mainWindow.getLogView().scrollToLine(prevBookmark.getLine());
    }

    public ExecutionResult cycleLineLabelDisplayMode() {
        return this.mainWindow.getLogView().cycleLineLabelDisplayMode();
    }

    public ExecutionResult toggleSidebar() {
        this.mainWindow.toggleSidebar();
        return new ExecutionResult(true);
    }

    public ExecutionResult toggleBookmarks() {
        this.mainWindow.toggleBookmarksView();
        return new ExecutionResult(true);
    }

    public ExecutionResult toggleFollowTail() {
        return this.mainWindow.getLogView().toggleFollowTail();
    }

    public ExecutionResult setMaxSidebarWidthColums(String arg) {
        int columns;
        try {
            columns = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            return new ExecutionResult(false, "Not a valid column count: "+arg);
        }
        if (columns < 0) {
            return new ExecutionResult(false, "Need a value >= 0!");
        }
        this.mainWindow.getSideBar().setMaxAbsoluteWidth(columns);
        return new ExecutionResult(true);
    }

    public ExecutionResult setMaxSidebarWidthRatio(String arg) {
        double ratio;
        try {
            ratio = Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            return new ExecutionResult(false, "Not a valid column ratio: "+arg);
        }
        if (ratio < 0 || ratio > 1) {
            return new ExecutionResult(false, "Need a value between 0 and 1!");
        }
        this.mainWindow.getSideBar().setMaxRelativeWidth(ratio);
        return new ExecutionResult(true);
    }

    public ExecutionResult toggleViewVisible(String arg) {
        try {
            int index = Integer.parseInt(arg);
            return this.viewsTree.toggleView(index);
        }  catch (NumberFormatException e) {
            return new ExecutionResult(false, arg + ": Not a valid view index!");
        }
    }

    public ExecutionResult openFile(String arg) {
        Path path = Paths.get(arg);
        try {
            Collection<DataView> logfiles = logFileOpener.open(path);
            if (logfiles.isEmpty()) {
                return new ExecutionResult(false, "No logfiles could be found unter " + path.toString());
            }
            return ExecutionResult.merged((logfiles.stream()
                    .map(viewsTree::addView)
                    .collect(Collectors.toList())));
        } catch (IOException e) {
            return new ExecutionResult(false, "Could not open file: " + e.toString());
        }
    }

    public ExecutionResult closeFile(String arg) {
        try {
            int index = Integer.parseInt(arg);
            DataView dataView = this.viewsTree.removeView(index);
            dataView.destroy();
            return new ExecutionResult(true);
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            return new ExecutionResult(false, arg + ": Not a valid view index!");
        }
    }

    public ExecutionResult runAsyncIfPossible(Supplier<ExecutionResult> task, boolean blocking, String descriptionInPresentParticiple) {
        CompletableFuture<ExecutionResult> f = CompletableFuture.supplyAsync(task, logDispatcher);
        if (blocking) {
            try {
                return f.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ExecutionResult(false, "Interrupted while " + descriptionInPresentParticiple + "!");
            } catch (ExecutionException e) {
                return new ExecutionResult(false, "Error while " + descriptionInPresentParticiple + ": "+e.toString());
            }
        } else {
            f.thenRunAsync(mainWindow::updateView, UI::runLater);
            return new ExecutionResult(true);
        }
    }

    private boolean isEofReached(Query query, int focusedLineIndex, List<Line> allLines) {
        if (query.isBackwards()) {
            return focusedLineIndex == 0;
        } else {
            return focusedLineIndex == allLines.size() - 1;
        }
    }

    public boolean handleKeyStroke(KeyStroke keyStroke) {
        if (this.mainWindow.isCommandBarEditing()) {
            return false;
        }
        if (keyStroke.getKeyType() == KeyType.Character) {
            Character character = keyStroke.getCharacter();
            if (character == ':' || character == '/' || character == '?') {
                this.mainWindow.openCommandBar(character.toString());
                return false;
            }
        }
        ExecutionResult executionResult = keyStrokeHandler.handleKeyStroke(keyStroke);
        return executionResult.isUiUpdateRequired();
    }
}
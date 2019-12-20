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

package de.rochefort.logrifle.ui;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import de.rochefort.logrifle.base.LogDispatcher;
import de.rochefort.logrifle.data.bookmarks.Bookmarks;
import de.rochefort.logrifle.data.parsing.Line;
import de.rochefort.logrifle.data.views.DataView;
import de.rochefort.logrifle.data.views.DataViewFiltered;
import de.rochefort.logrifle.data.views.ViewsTree;
import de.rochefort.logrifle.data.views.ViewsTreeNode;
import de.rochefort.logrifle.ui.cmd.CommandHandler;
import de.rochefort.logrifle.ui.cmd.ExecutionResult;
import de.rochefort.logrifle.ui.cmd.KeyStrokeHandler;
import de.rochefort.logrifle.ui.cmd.Query;
import de.rochefort.logrifle.data.highlights.Highlight;
import de.rochefort.logrifle.data.highlights.HighlightsData;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class MainController {
    private static final String COMMAND_PREFIX = ":";
    private static final String FIND_PREFIX = "/";
    private static final String FIND_BACKWARDS_PREFIX = "?";
    private final MainWindow mainWindow;
    private final KeyStrokeHandler keyStrokeHandler;
    private final Deque<Query> queryHistory = new LinkedList<>();
    private final LogDispatcher logDispatcher;
    private final ViewsTree viewsTree;
    private final HighlightsData highlightsData;
    private final Bookmarks bookmarks;
    private final TextColorIterator highlightsFgIterator = new TextColorIterator(Arrays.asList(
            TextColor.ANSI.BLACK,
            TextColor.ANSI.BLACK,
            TextColor.ANSI.BLACK,
            TextColor.ANSI.WHITE,
            TextColor.ANSI.WHITE
    ));
    private final TextColorIterator highlightsBgIterator = new TextColorIterator(Arrays.asList(
            TextColor.ANSI.YELLOW,
            TextColor.ANSI.CYAN,
            TextColor.ANSI.MAGENTA,
            TextColor.ANSI.BLUE,
            TextColor.ANSI.RED
    ));

    public MainController(
            MainWindow mainWindow,
            CommandHandler commandHandler,
            KeyStrokeHandler keyStrokeHandler,
            LogDispatcher logDispatcher,
            ViewsTree viewsTree,
            HighlightsData highlightsData,
            Bookmarks bookmarks
    ) {
        this.mainWindow = mainWindow;
        this.keyStrokeHandler = keyStrokeHandler;
        this.logDispatcher = logDispatcher;
        this.viewsTree = viewsTree;
        this.highlightsData = highlightsData;
        this.bookmarks = bookmarks;
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
                if (prefix.equals(COMMAND_PREFIX)) {
                    result = commandHandler.handle(command);
                } else if (prefix.equals(FIND_PREFIX)) {
                    result = find(new Query(command, false));
                } else if (prefix.equals(FIND_BACKWARDS_PREFIX)) {
                    result = find(new Query(command, true));
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

    public ExecutionResult moveFilterUp() {
        boolean changed = viewsTree.moveFocusUp();
        return new ExecutionResult(changed, null);
    }

    public ExecutionResult moveFilterDown() {
        boolean changed = viewsTree.moveFocusDown();
        return new ExecutionResult(changed, null);
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
        Pattern p = Pattern.compile(query.getSearchTerm());
        LogView logView = this.mainWindow.getLogView();
        int focusedLineIndex = logView.getFocusedLineIndex();
        DataView dataView = this.mainWindow.getDataView();
        List<Line> allLines = dataView.getAllLines();
        if (isEofReached(query, focusedLineIndex, allLines)) {
            return new ExecutionResult(false, query + ": End of file reached.");
        }
        if (query.isBackwards()) {
            for (int i = focusedLineIndex - 1; i >= 0; i--) {
                String raw = allLines.get(i).getRaw();
                if (p.matcher(raw).find()) {
                    logView.scrollVertically(i - focusedLineIndex);
                    return new ExecutionResult(true);
                }
            }
        } else {
            for (int i = focusedLineIndex + 1; i < allLines.size(); i++) {
                String raw = allLines.get(i).getRaw();
                if (p.matcher(raw).find()) {
                    logView.scrollVertically(i - focusedLineIndex);
                    return new ExecutionResult(true);
                }
            }
        }

        return new ExecutionResult(false, query + ": pattern not found.");
    }

    public ExecutionResult addFilter(String regex, boolean inverted) {
        if (regex.isEmpty()) {
            return new ExecutionResult(false, "Missing argument: filter pattern");
        }
        ViewsTree viewsTree = this.viewsTree;
        ViewsTreeNode focusedTreeNode = viewsTree.getFocusedNode();
        DataView focusedView = focusedTreeNode.getDataView();
        DataViewFiltered dataViewFiltered = new DataViewFiltered(regex, focusedView, inverted, logDispatcher);
        logDispatcher.execute(() -> {
            focusedView.addListener(dataViewFiltered);
            dataViewFiltered.onUpdated(focusedView);
            UI.runLater(() -> {
	            ViewsTreeNode child = new ViewsTreeNode(focusedTreeNode, dataViewFiltered);
	            viewsTree.addNodeAndSetFocus(focusedTreeNode, child);
	            mainWindow.updateView();
            });
        });
        return new ExecutionResult(false);
    }

    public ExecutionResult addHighlight(String args) {
        Highlight highlight = new Highlight(args, highlightsFgIterator.next(), highlightsBgIterator.next());
        this.highlightsData.addHighlight(highlight);
        return new ExecutionResult(true);

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
        if (focusedTreeNode.getParent() == null) {
            return new ExecutionResult(false, "Cannot edit this view!");
        }
        String regex = focusedTreeNode.getTitle();
        String preparedCommand;
        if (regex.startsWith("!")) {
            preparedCommand = "!filter " + regex.substring(1);
        } else {
            preparedCommand = ":filter " + regex;
        }
        viewsTree.removeNode(focusedTreeNode);
        return prepareCommand(preparedCommand);
    }

    public ExecutionResult addBookmark() {
        @Nullable Line focusedLine = mainWindow.getLogView().getFocusedLine();
        if (focusedLine == null) {
            return new ExecutionResult(false, "No line is currently focused.");
        }
        return bookmarks.add(focusedLine);
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
        return find(new Query(last.getSearchTerm(), !last.isBackwards()), true);
    }

    public ExecutionResult moveFocus(String args) {
        try {
            int lineCount = Integer.parseInt(args);
            return this.mainWindow.getLogView().moveFocus(lineCount);
        } catch (NumberFormatException e) {
            return new ExecutionResult(false, args + ": Not a valid line count");
        }
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

    public ExecutionResult scrollHotizontally(String args) {
        try {
            int columnCount = Integer.parseInt(args);
            return this.mainWindow.getLogView().scrollHorizontally(columnCount);
        } catch (NumberFormatException e) {
            return new ExecutionResult(false, args + ": Not a valid column count");
        }
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

    public ExecutionResult toggleLineLabels() {
        return this.mainWindow.getLogView().toggleLineLabels();
    }

    public ExecutionResult toggleSidebar() {
        this.mainWindow.toggleSidebar();
        return new ExecutionResult(true);
    }

    public ExecutionResult toggleBookmarks() {
        this.mainWindow.toggleBookmarksView();
        return new ExecutionResult(true);
    }

    private boolean isEofReached(Query query, int focusedLineIndex, List<Line> allLines) {
        if (query.isBackwards()) {
            return focusedLineIndex == 0;
        } else {
            return focusedLineIndex == allLines.size() - 1;
        }
    }

    public boolean handleKeyStroke(KeyStroke keyStroke) {
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
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
import de.rochefort.logrifle.data.parsing.Line;
import de.rochefort.logrifle.data.views.DataView;
import de.rochefort.logrifle.data.views.DataViewFiltered;
import de.rochefort.logrifle.data.views.ViewsTree;
import de.rochefort.logrifle.data.views.ViewsTreeNode;
import de.rochefort.logrifle.ui.cmd.Command;
import de.rochefort.logrifle.ui.cmd.CommandHandler;
import de.rochefort.logrifle.ui.cmd.ExecutionResult;
import de.rochefort.logrifle.ui.cmd.KeyStrokeHandler;
import de.rochefort.logrifle.ui.cmd.Query;

import java.io.IOException;
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

    public MainController(MainWindow mainWindow, CommandHandler commandHandler, KeyStrokeHandler keyStrokeHandler) {
        this.mainWindow = mainWindow;
        this.keyStrokeHandler = keyStrokeHandler;
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

        commandHandler.register(new Command("!filter") {
            @Override
            protected ExecutionResult execute(String args) {
                return addFilter(args, true);
            }
        });

        commandHandler.register(new Command("filter") {
            @Override
            protected ExecutionResult execute(String args) {
                return addFilter(args, false);
            }
        });

        commandHandler.register(new Command("filter-view-up") {
            @Override
            protected ExecutionResult execute(String args) {
                return moveFilterUp();
            }
        });

        commandHandler.register(new Command("filter-view-down") {
            @Override
            protected ExecutionResult execute(String args) {
                return moveFilterDown();
            }
        });

        commandHandler.register(new Command("find") {
            @Override
            protected ExecutionResult execute(String args) {
                return find(new Query(args, false));
            }
        });

        commandHandler.register(new Command("find-again") {
            @Override
            protected ExecutionResult execute(String args) {
                return findAgain();
            }
        });

        commandHandler.register(new Command("find-again-backwards") {
            @Override
            protected ExecutionResult execute(String args) {
                return findAgainBackwards();
            }
        });

        commandHandler.register(new Command("find-backwards") {
            @Override
            protected ExecutionResult execute(String args) {
                return find(new Query(args, true));
            }
        });

        commandHandler.register(new Command("quit") {
            @Override
            protected ExecutionResult execute(String args) {
                return quit();
            }
        });

        commandHandler.register(new Command("refresh") {
            @Override
            protected ExecutionResult execute(String args) {
                return refresh();
            }
        });

        commandHandler.register(new Command("scroll") {
            @Override
            protected ExecutionResult execute(String args) {
                return scroll(args);
            }
        });

        commandHandler.register(new Command("move-focus") {
            @Override
            protected ExecutionResult execute(String args) {
                return moveFocus(args);
            }
        });
    }

    private ExecutionResult moveFilterUp() {
        boolean changed = mainWindow.getViewsTree().moveFocusUp();
        return new ExecutionResult(changed, null);
    }

    private ExecutionResult moveFilterDown() {
        boolean changed = mainWindow.getViewsTree().moveFocusDown();
        return new ExecutionResult(changed, null);
    }

    private ExecutionResult find(Query query) {
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
                    logView.scroll(i - focusedLineIndex);
                    return new ExecutionResult(true);
                }
            }
        } else {
            for (int i = focusedLineIndex + 1; i < allLines.size(); i++) {
                String raw = allLines.get(i).getRaw();
                if (p.matcher(raw).find()) {
                    logView.scroll(i - focusedLineIndex);
                    return new ExecutionResult(true);
                }
            }
        }

        return new ExecutionResult(false, query + ": pattern not found.");
    }

    private ExecutionResult addFilter(String regex, boolean inverted) {
        if (regex.isEmpty()) {
            return new ExecutionResult(false, "Missing argument: filter pattern");
        }
        ViewsTree viewsTree = this.mainWindow.getViewsTree();
        ViewsTreeNode focusedTreeNode = viewsTree.getFocusedNode();
        DataViewFiltered dataViewFiltered = new DataViewFiltered(regex, focusedTreeNode.getDataView(), inverted);
        ViewsTreeNode child = new ViewsTreeNode(focusedTreeNode, dataViewFiltered);
        viewsTree.addNodeAndSetFocus(focusedTreeNode, child);
        return new ExecutionResult(true);
    }

    private ExecutionResult findAgain() {
        if (this.queryHistory.isEmpty()) {
            return new ExecutionResult(false);
        }
        return find(this.queryHistory.getLast());
    }

    private ExecutionResult findAgainBackwards() {
        if (this.queryHistory.isEmpty()) {
            return new ExecutionResult(false);
        }
        Query last = this.queryHistory.getLast();
        return find(new Query(last.getSearchTerm(), !last.isBackwards()), true);
    }

    private ExecutionResult quit() {
        try {
            this.mainWindow.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ExecutionResult(false);
    }

    private ExecutionResult refresh() {
        this.mainWindow.updateView();
        return new ExecutionResult(true);
    }

    private ExecutionResult scroll(String args) {
        try {
            int lineCount = Integer.parseInt(args);
            return this.mainWindow.getLogView().scroll(lineCount);
        } catch (NumberFormatException e) {
            return new ExecutionResult(false, args + ": Not a valid line count");
        }
    }


    private ExecutionResult moveFocus(String args) {
        try {
            int lineCount = Integer.parseInt(args);
            return this.mainWindow.getLogView().moveFocus(lineCount);
        } catch (NumberFormatException e) {
            return new ExecutionResult(false, args + ": Not a valid line count");
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
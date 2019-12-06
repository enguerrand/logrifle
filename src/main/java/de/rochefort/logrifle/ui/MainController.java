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
import de.rochefort.logrifle.LogReader;
import de.rochefort.logrifle.data.parsing.Line;
import de.rochefort.logrifle.data.views.DataView;
import de.rochefort.logrifle.ui.cmd.Command;
import de.rochefort.logrifle.ui.cmd.CommandHandler;
import de.rochefort.logrifle.ui.cmd.ExecutionResult;
import de.rochefort.logrifle.ui.cmd.KeyStrokeHandler;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class MainController {
    private static final String COMMAND_PREFIX = ":";
    private static final String SEARCH_NEXT_PREFIX = "/";
    private static final String SEARCH_PREV_PREFIX = "?";
    private final MainWindow mainWindow;
    private final KeyStrokeHandler keyStrokeHandler;

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
                } else if (prefix.equals(SEARCH_NEXT_PREFIX)) {
                    result = searchNext(command);
                } else if (prefix.equals(SEARCH_PREV_PREFIX)) {
                    result = searchPrevious(command);
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

        commandHandler.register(new Command("search-next") {
            @Override
            protected ExecutionResult execute(String args) {
                return searchNext(args);
            }
        });

        commandHandler.register(new Command("search-prev") {
            @Override
            protected ExecutionResult execute(String args) {
                return searchPrevious(args);
            }
        });

    }

    private ExecutionResult scroll(String args) {
        int lineCount = 1;
        if (!args.matches("^\\s*$")) {
            try {
                lineCount = Integer.parseInt(args);
            } catch (NumberFormatException e) {
                return new ExecutionResult(false, args + ": Not a valid line count");
            }
        }
        return this.mainWindow.getLogView().scroll(lineCount);
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

    private ExecutionResult searchNext(String query) {
        if (query.matches("\\s*")) {
            return new ExecutionResult(false);
        }
        Pattern p = Pattern.compile(query);
        LogView logView = this.mainWindow.getLogView();
        int focusedLineIndex = logView.getFocusedLineIndex();
        DataView dataView = this.mainWindow.getDataView();
        List<Line> allLines = dataView.getAllLines();
        if (focusedLineIndex < allLines.size() - 1) {
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

    private ExecutionResult searchPrevious(String query) {
        if (query.matches("\\s*")) {
            return new ExecutionResult(false);
        }
        Pattern p = Pattern.compile(query);
        LogView logView = this.mainWindow.getLogView();
        int focusedLineIndex = logView.getFocusedLineIndex();
        if (focusedLineIndex > 0) {
            DataView dataView = this.mainWindow.getDataView();
            List<Line> allLines = dataView.getAllLines();

            for (int i = focusedLineIndex - 1; i >= 0; i--) {
                String raw = allLines.get(i).getRaw();
                if (p.matcher(raw).find()) {
                    logView.scroll(i - focusedLineIndex);
                    return new ExecutionResult(true);
                }
            }
        }

        return new ExecutionResult(false, query + ": pattern not found.");
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

    public void setDataView(LogReader dataView) {
        this.mainWindow.setDataView(dataView);
    }
}
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
import de.rochefort.logrifle.ui.cmd.Command;
import de.rochefort.logrifle.ui.cmd.CommandHandler;
import de.rochefort.logrifle.ui.cmd.ExecutionResult;
import de.rochefort.logrifle.ui.cmd.KeyStrokeHandler;

import java.io.IOException;
import java.util.List;

public class MainController {
    private final MainWindow mainWindow;
    private final KeyStrokeHandler keyStrokeHandler;

    public MainController(MainWindow mainWindow, CommandHandler commandHandler, KeyStrokeHandler keyStrokeHandler) {
        this.mainWindow = mainWindow;
        this.keyStrokeHandler = keyStrokeHandler;
        this.mainWindow.setCommandViewListener(new CommandViewListener() {
            @Override
            public void onCommand(String commandLine) {
                mainWindow.closeCommandBar();
                ExecutionResult result = commandHandler.handle(commandLine);
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

        commandHandler.register(new Command(":quit") {
            @Override
            protected ExecutionResult execute(List<String> args) {
                return quit();
            }
        });

        commandHandler.register(new Command(":refresh") {
            @Override
            protected ExecutionResult execute(List<String> args) {
                return refresh();
            }
        });

        commandHandler.register(new Command(":scroll") {
            @Override
            protected ExecutionResult execute(List<String> args) {
                return scroll(args);
            }
        });

    }

    private ExecutionResult scroll(List<String> args) {
        int lineCount = 1;
        if (!args.isEmpty()) {
            String arg1 = args.get(0);
            try {
                lineCount = Integer.parseInt(arg1);
            } catch (NumberFormatException e) {
                return new ExecutionResult(false, arg1 + ": Not a valid line count");
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

    public boolean handleKeyStroke(KeyStroke keyStroke) {
        if (keyStroke.getKeyType() == KeyType.Character) {
            Character character = keyStroke.getCharacter();
            if (character == ':' || character == '/') {
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
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
import de.rochefort.logrifle.LogReader;
import de.rochefort.logrifle.ui.cmd.CommandHandler;
import de.rochefort.logrifle.ui.cmd.ExecutionResult;

import java.io.IOException;

public class MainController {
    private final MainWindow mainWindow;

    public MainController(MainWindow mainWindow, CommandHandler commandHandler) {
        this.mainWindow = mainWindow;
        this.mainWindow.setCommandViewListener(new CommandViewListener() {
            @Override
            public void onCommand(String commandLine) {
                mainWindow.closeCommandBar();
                ExecutionResult result = commandHandler.handle(commandLine);
                result.getUserMessage().ifPresent(msg -> {
                    mainWindow.showCommandViewMessage(msg, TextColor.ANSI.RED);
                });
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

    public boolean handleKeyStroke(KeyStroke keyStroke) {
        switch (keyStroke.getKeyType()) {
            case F5:
                mainWindow.updateView();
                break;
            case Character:
                handleCharacter(keyStroke);
                break;
            default:
                break;
        }
        return false;
    }

    private void handleCharacter(KeyStroke keyStroke) {
        Character character = keyStroke.getCharacter();
        switch(character) {
            case ':':
                mainWindow.openCommandBar(character.toString());
                break;
            case 'q': {
                try {
                    mainWindow.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            default:
                // ignored
        }
    }

    public void setDataView(LogReader dataView) {
        mainWindow.setDataView(dataView);
    }
}

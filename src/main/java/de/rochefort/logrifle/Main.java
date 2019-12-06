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

package de.rochefort.logrifle;

import com.googlecode.lanterna.gui2.TextGUI;
import com.googlecode.lanterna.input.KeyStroke;
import de.rochefort.logrifle.data.parsing.LineParser;
import de.rochefort.logrifle.data.parsing.LineParserTimestampedTextImpl;
import de.rochefort.logrifle.ui.MainController;
import de.rochefort.logrifle.ui.MainWindow;
import de.rochefort.logrifle.ui.MainWindowListener;
import de.rochefort.logrifle.ui.cmd.CommandHandler;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Need path to file!");
            return;
        }
        ExecutorService workerPool = Executors.newCachedThreadPool();
        String pathToLogFile = args[0];
        Path path = Paths.get(pathToLogFile);
        LineParser lineParser = new LineParserTimestampedTextImpl();
        LogReader logReader = new LogReader(lineParser, path, workerPool);
        MainWindow mainWindow = new MainWindow();
        CommandHandler commandHandler = new CommandHandler();
        MainController mainController = new MainController(mainWindow, commandHandler);
        mainWindow.start(workerPool, new MainWindowListener() {
            @Override
            public boolean onUnhandledKeyStroke(TextGUI textGUI, KeyStroke keyStroke) {
                return mainController.handleKeyStroke(keyStroke);
            }

            @Override
            public void onClosed() {
                logReader.shutdown();
                workerPool.shutdown();
                System.exit(0);
            }
        });
        mainController.setDataView(logReader);
    }
}

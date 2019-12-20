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

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.TextGUI;
import com.googlecode.lanterna.input.KeyStroke;
import de.rochefort.logrifle.base.LogDispatcher;
import de.rochefort.logrifle.data.parsing.LineParser;
import de.rochefort.logrifle.data.parsing.LineParserTimestampedTextImpl;
import de.rochefort.logrifle.data.views.DataView;
import de.rochefort.logrifle.data.views.DataViewMerged;
import de.rochefort.logrifle.data.views.ViewsTree;
import de.rochefort.logrifle.ui.MainController;
import de.rochefort.logrifle.ui.MainWindow;
import de.rochefort.logrifle.ui.MainWindowListener;
import de.rochefort.logrifle.ui.TextColorIterator;
import de.rochefort.logrifle.ui.cmd.CommandHandler;
import de.rochefort.logrifle.ui.cmd.KeyMapFactory;
import de.rochefort.logrifle.ui.cmd.KeyStrokeHandler;
import de.rochefort.logrifle.data.highlights.HighlightsData;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main {
    public static void main(String[] args) throws IOException {
        KeyMapFactory keyMapFactory = new KeyMapFactory();
        CommandHandler commandHandler = new CommandHandler();
        if (Arrays.stream(args).anyMatch(a -> a.equals("-h") || a.equals("--help"))) {
            commandHandler.printHelp(keyMapFactory.get());
            System.exit(0);
        }
        if (args.length == 0) {
            System.err.println("Need path to file!");
            return;
        }
        List<Path> logfiles = new ArrayList<>();
        for (String arg : args) {
            Path path = Paths.get(arg);
            logfiles.add(path);
        }
        ExecutorService workerPool = Executors.newCachedThreadPool();
        ScheduledExecutorService timerPool = Executors.newScheduledThreadPool(10);

        LogDispatcher logDispatcher = new LogDispatcher();
        LineParser lineParser = new LineParserTimestampedTextImpl();
        DataView rootView;
        List<LogReader> logReaders = new ArrayList<>();
        TextColorIterator textColorIterator = new TextColorIterator(Arrays.asList(
                TextColor.ANSI.BLUE,
                TextColor.ANSI.GREEN,
                TextColor.ANSI.MAGENTA,
                TextColor.ANSI.RED,
                TextColor.ANSI.CYAN,
                TextColor.ANSI.YELLOW,
                TextColor.ANSI.WHITE
        ));
        for (Path logfile : logfiles) {
            logReaders.add(new LogReader(lineParser, logfile, textColorIterator.next(), workerPool, timerPool, logDispatcher));
        }
        if (logReaders.size() == 1) {
            rootView = logReaders.get(0);
        } else {
            rootView = new DataViewMerged(logReaders, logDispatcher, timerPool);
        }

        ViewsTree viewsTree = new ViewsTree(rootView);
        HighlightsData highlightsData = new HighlightsData();
        MainWindow mainWindow = new MainWindow(viewsTree, highlightsData, logDispatcher);
        KeyStrokeHandler keyStrokeHandler = new KeyStrokeHandler(keyMapFactory.get(), commandHandler);
        MainController mainController = new MainController(mainWindow, commandHandler, keyStrokeHandler, logDispatcher, viewsTree, highlightsData);
        commandHandler.setMainController(mainController);
        mainWindow.start(workerPool, new MainWindowListener() {
            @Override
            public boolean onUnhandledKeyStroke(TextGUI textGUI, KeyStroke keyStroke) {
                return mainController.handleKeyStroke(keyStroke);
            }

            @Override
            public void onClosed() {
                for (LogReader logReader : logReaders) {
                    logReader.shutdown();
                }
                workerPool.shutdown();
                System.exit(0);
            }
        });
        mainWindow.updateView();
    }
}

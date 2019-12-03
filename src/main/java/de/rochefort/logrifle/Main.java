package de.rochefort.logrifle;

import com.googlecode.lanterna.gui2.TextGUI;
import com.googlecode.lanterna.input.KeyStroke;
import de.rochefort.logrifle.data.parsing.LineParser;
import de.rochefort.logrifle.data.parsing.LineParserTimestampedTextImpl;
import de.rochefort.logrifle.ui.MainController;
import de.rochefort.logrifle.ui.MainWindow;
import de.rochefort.logrifle.ui.MainWindowListener;

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
        MainController mainController = new MainController(mainWindow);
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

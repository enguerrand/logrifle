package de.rochefort.logrifle;

import de.rochefort.logrifle.data.parsing.LineParser;
import de.rochefort.logrifle.data.parsing.LineParserTimestampedTextImpl;
import de.rochefort.logrifle.ui.MainWindow;

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
        workerPool.execute(() -> {
            try {
                mainWindow.start();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                logReader.shutdown();
                workerPool.shutdown();
            }
        });
        mainWindow.setDataView(logReader);
    }
}

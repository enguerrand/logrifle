package de.rochefort.logrifle;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LogReader {
    public LogReader(Path path) {
        TailerListener tailerListener = new TailerListenerAdapter() {
            @Override
            public void handle(String s) {
                System.out.println(s);
            }
        };
        Tailer tailer = new Tailer(path.toFile(), tailerListener);
        tailer.run();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Need path to file!");
            return;
        }
        String pathToLogFile = args[0];
        Path path = Paths.get(pathToLogFile);
        LogReader logReader = new LogReader(path);

    }
}

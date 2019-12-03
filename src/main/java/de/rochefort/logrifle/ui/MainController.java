package de.rochefort.logrifle.ui;

import com.googlecode.lanterna.input.KeyStroke;
import de.rochefort.logrifle.LogReader;

import java.io.IOException;

public class MainController {
    private final MainWindow mainWindow;

    public MainController(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public boolean handleKeyStroke(KeyStroke keyStroke) {
        switch (keyStroke.getKeyType()) {
            case Escape:
                try {
                    mainWindow.close();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case F5:
                mainWindow.updateLogView();
                break;
            default:
                break;
        }
        return false;
    }

    public void setDataView(LogReader dataView) {
        mainWindow.setDataView(dataView);
    }
}

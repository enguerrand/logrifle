package de.rochefort.logrifle.ui;

import com.googlecode.lanterna.input.KeyStroke;
import de.rochefort.logrifle.LogReader;

import java.io.IOException;

public class MainController {
    private final MainWindow mainWindow;

    public MainController(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.mainWindow.setCommandViewListener(new CommandViewListener() {
            @Override
            public void onCommand(String command) {
                System.out.println("Command received: "+command);
                mainWindow.closeCommandBar();
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
        System.out.println("Character typed: "+ character);
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
                return;
        }
    }

    public void setDataView(LogReader dataView) {
        mainWindow.setDataView(dataView);
    }
}

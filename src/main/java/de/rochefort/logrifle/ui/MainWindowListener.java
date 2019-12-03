package de.rochefort.logrifle.ui;

import com.googlecode.lanterna.gui2.TextGUI;

public interface MainWindowListener extends TextGUI.Listener {
    void onClosed();
}

package de.rochefort.logrifle.ui;

import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.input.KeyStroke;

public interface InteractableKeystrokeListener {
    void onKeyStroke(Interactable interactable, KeyStroke keyStroke);
    Interactable getInteractable();
}
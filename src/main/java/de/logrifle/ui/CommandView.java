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

package de.logrifle.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LayoutManager;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import org.jetbrains.annotations.Nullable;

class CommandView implements InteractableKeystrokeListener {
    private final Panel panel;
    private final TextBox commandInput;
    private final Label messageBox;
    /**
     * only access on ui thread
     */
    private CommandViewListener listener;
    private int height = 0;

    CommandView() {
        LayoutManager layout = new ZeroMarginsGridLayout(1);
        panel = new Panel(layout);
        TextBox.DefaultTextBoxRenderer renderer = new TextBox.DefaultTextBoxRenderer();
        renderer.setUnusedSpaceCharacter(' ');
        commandInput = new TextBox("", TextBox.Style.SINGLE_LINE)
            .setRenderer(renderer);
        messageBox = new Label("");
    }

    void setListener(CommandViewListener listener) {
        this.listener = listener;
    }

    void show(String initialText){
        hide();
        commandInput.setText(initialText);
        commandInput.setCaretPosition(initialText.length());
        panel.addComponent(commandInput);
        commandInput.takeFocus();
        height = 1;
    }

    void showMessage(String message, @Nullable TextColor textColor) {
        hide();
        messageBox.setText(message);
        if (textColor != null) {
            messageBox.setForegroundColor(textColor);
        }
        panel.addComponent(messageBox);
        height = 1;
    }

    void hide(){
        UI.checkGuiThreadOrThrow();
        panel.removeComponent(commandInput);
        panel.removeComponent(messageBox);
        height = 0;
    }

    Panel getPanel() {
        return panel;
    }

    void update(@Nullable TerminalSize commandBarSize) {
        if (commandBarSize != null) {
            commandInput.setPreferredSize(commandBarSize);
        }
    }

    @Override
    public void onKeyStroke(Interactable interactable, KeyStroke keyStroke) {
        String command = commandInput.getText();
        if(command.isEmpty()) {
            this.listener.onEmptied();
            return;
        }
        KeyType keyType = keyStroke.getKeyType();
        switch(keyType) {
            case Escape:
                commandInput.setText("");
                this.listener.onEmptied();
                break;
            case Enter:
                commandInput.setText("");
                this.listener.onCommand(command);
                break;
            default:
                break;
        }
    }

    @Override
    public Interactable getInteractable() {
        return this.commandInput;
    }

    public int getHeight() {
        return height;
    }
}

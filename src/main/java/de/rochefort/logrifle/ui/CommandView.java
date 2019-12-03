package de.rochefort.logrifle.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.LayoutManager;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import org.jetbrains.annotations.Nullable;

class CommandView implements InteractableKeystrokeListener {
    private final Panel panel;
    private final TextBox commandInput;
    private CommandViewListener listener;
    private boolean shown = false;

    CommandView() {
        LayoutManager layout = new GridLayout(1);
        panel = new Panel(layout);
        panel.setLayoutData(BorderLayout.Location.CENTER);
        TextBox.DefaultTextBoxRenderer renderer = new TextBox.DefaultTextBoxRenderer();
        renderer.setUnusedSpaceCharacter(' ');
        commandInput = new TextBox("", TextBox.Style.SINGLE_LINE)
            .setRenderer(renderer);
    }

    void setListener(CommandViewListener listener) {
        this.listener = listener;
    }

    void show(String initialText){
        commandInput.setText(initialText);
        commandInput.setCaretPosition(initialText.length());
        panel.addComponent(commandInput);
        commandInput.takeFocus();
        shown = true;
    }

    void hide(){
        panel.removeComponent(commandInput);
        shown = false;
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

    public boolean isShown() {
        return shown;
    }
}

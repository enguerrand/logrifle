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

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LayoutManager;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import de.logrifle.base.Strings;
import de.logrifle.ui.cmd.KeyBind;
import de.logrifle.ui.completion.CommandAutoCompleter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandView implements InteractableKeystrokeListener {
    private static final Map<KeyStroke, Consumer<CommandView>> KEY_BINDS = new HashMap<>();
    private static List<KeyBind> BIND_HELP = new ArrayList<>();

    static {
        registerKeyBind(new KeyStroke(KeyType.ArrowDown, false, false), CommandView::historyForward, "Go to next command in history");
        registerKeyBind(new KeyStroke(KeyType.ArrowUp, false, false), CommandView::historyBackward,"Go to previous command in history");
        registerKeyBind(new KeyStroke(KeyType.Enter, false, false), CommandView::commitCommand,"Execute current input as command");
        registerKeyBind(new KeyStroke(KeyType.Escape, false, false), CommandView::handleEscape,"Close command input bar");
        registerKeyBind(new KeyStroke(KeyType.Tab, false, false), CommandView::autocomplete,"Autocomplete current command");
        registerKeyBind(new KeyStroke(KeyType.ArrowLeft,  true, false), CommandView::moveWordLeft,"Move caret one word left");
        registerKeyBind(new KeyStroke(KeyType.ArrowRight,true, false), CommandView::moveWordRight,"Move caret one word right");
        registerKeyBind(new KeyStroke('k', true, false), CommandView::killForward,"Kill (cut) everything after caret");
        registerKeyBind(new KeyStroke('u', true, false), CommandView::killBackward,"Kill (cut) everything before caret");
        registerKeyBind(new KeyStroke('w', true, false), CommandView::killWord,"Kill (cut) word before caret");
        registerKeyBind(new KeyStroke('y', true, false), CommandView::yank,"Yank (paste) kill buffer");
    }
    private final Panel panel;
    private final TextBox commandInput;
    private final Label messageBox;
    private final CommandHistory history;
    private final Panel completionPanel;
    /**
     * only access on ui thread
     */
    private CommandAutoCompleter commandAutoCompleter;
    /**
     * only access on ui thread
     */
    private CommandViewListener listener;
    private int height = 0;

    private String killBuffer = "";

    CommandView() {
        history = new CommandHistory();
        LayoutManager layout = new BorderLayout();
        panel = new Panel(layout);
        completionPanel = new Panel(new ZeroMarginsGridLayout(1));
        TextBox.DefaultTextBoxRenderer renderer = new TextBox.DefaultTextBoxRenderer();
        renderer.setUnusedSpaceCharacter(' ');
        commandInput = new TextBox("", TextBox.Style.SINGLE_LINE)
            .setRenderer(renderer);
        commandInput.setInputFilter((interactable, keyStroke) -> {
            return !KEY_BINDS.containsKey(keyStroke);
        });
        messageBox = new SanitizedLabel("");
    }

    public static Collection<KeyBind> getKeyBinds() {
        return BIND_HELP;
    }

    private void killWord() {
        TerminalPosition caretPosition = commandInput.getCaretPosition();
        TokenizedCommandInput tokenized = getTokenizedCurrentInput();
        String lineStart = tokenized.beforeCaret;
        Pattern lastWordPattern = Pattern.compile("(.*\\b)(\\S+\\s*)$");
        Matcher matcher = lastWordPattern.matcher(lineStart);
        int nextColumn;
        String rest;
        if (matcher.matches()) {
            rest = matcher.group(1);
            this.killBuffer = matcher.group(2);
            nextColumn = caretPosition.getColumn() - this.killBuffer.length();
        } else {
            rest = lineStart;
            nextColumn = caretPosition.getColumn();
        }
        String remainder = tokenized.afterCaret;
        setCurrentInput(tokenized.prefix + rest + remainder);
        commandInput.setCaretPosition(nextColumn);
    }

    private void killBackward() {
        TokenizedCommandInput tokenized = getTokenizedCurrentInput();
        this.killBuffer = tokenized.beforeCaret;
        String remainder = tokenized.afterCaret;
        setCurrentInput(tokenized.prefix + remainder);
        commandInput.setCaretPosition(1);
    }

    private void killForward() {
        TokenizedCommandInput tokenized = getTokenizedCurrentInput();
        this.killBuffer = tokenized.afterCaret;
        String remainder = tokenized.beforeCaret;
        setCurrentInput(tokenized.prefix + remainder);
    }

    private void yank() {
        TokenizedCommandInput tokenized = getTokenizedCurrentInput();
        String newLeft = tokenized.prefix + tokenized.beforeCaret + this.killBuffer;
        setCurrentInput(newLeft + tokenized.afterCaret);
        commandInput.setCaretPosition(
                newLeft.length()
        );
    }

    private void moveWordLeft() {
        int caretPosition = commandInput.getCaretPosition().getColumn();
        String text = commandInput.getText();
        commandInput.setCaretPosition(
                Strings.findPreviousWordStartOrStrt(text, caretPosition)
        );
    }

    private void moveWordRight() {
        int caretPosition = commandInput.getCaretPosition().getColumn();
        String text = commandInput.getText();
        commandInput.setCaretPosition(
                Strings.findNextWordStartOrEnd(text, caretPosition)
        );
    }

    private TokenizedCommandInput getTokenizedCurrentInput() {
        String text = commandInput.getText();
        String prefix = "";
        for (String possiblePrefix : Arrays.asList(
                MainController.COMMAND_PREFIX, MainController.FIND_PREFIX, MainController.FIND_BACKWARDS_PREFIX)
        ) {
            if (text.startsWith(possiblePrefix)) {
                prefix = possiblePrefix;
                text = text.substring(prefix.length());
                break;
            }
        }
        TerminalPosition caretPosition = commandInput.getCaretPosition();
        int caret = caretPosition.getColumn() - prefix.length();
        String[] tokenized = Strings.tokenizeAt(text, caret);
        return new TokenizedCommandInput(
                prefix,
                tokenized[0],
                tokenized[1]
        );
    }

    void setListener(CommandViewListener listener) {
        this.listener = listener;
    }

    void setAutoCompleter(CommandAutoCompleter commandAutoCompleter) {
        this.commandAutoCompleter = commandAutoCompleter;
    }

    void show(String initialText){
        hide();
        commandInput.setText(initialText);
        commandInput.setCaretPosition(initialText.length());
        panel.addComponent(commandInput);
        commandInput.setLayoutData(BorderLayout.Location.LEFT);
        commandInput.takeFocus();
        panel.addComponent(completionPanel);
        completionPanel.setLayoutData(BorderLayout.Location.CENTER);
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
        panel.removeComponent(completionPanel);
        height = 0;
    }

    boolean isEditing() {
        return height > 0 && panel.containsComponent(commandInput);
    }

    Panel getPanel() {
        return panel;
    }

    void update(@Nullable TerminalSize commandBarSize) {
        if (commandBarSize != null) {
            int caretWidth = 1;
            int margin = 1;
            String currentInput = commandInput.getText();
            int minWidth = commandAutoCompleter.getMaximumCommandLength(currentInput) + caretWidth + margin;
            TerminalSize nextPreferredSize;
            if (commandBarSize.getColumns() <= minWidth || completionPanel.getPreferredSize().getColumns() == 0) {
                nextPreferredSize = commandBarSize;
            } else {
                nextPreferredSize = new TerminalSize(minWidth, 1);
            }
            commandInput.setPreferredSize(nextPreferredSize);
        }
    }

    private void setCurrentInput(String input) {
        commandInput.setText(input);
        commandInput.setCaretPosition(input.length());
    }

    @Override
    public void onKeyStroke(Interactable interactable, KeyStroke keyStroke) {
        completionPanel.removeAllComponents();
        if(commandInput.getText().isEmpty()) {
            listener.onEmptied();
            return;
        }
        Consumer<CommandView> action = KEY_BINDS.get(keyStroke);
        if (action != null) {
            action.accept(this);
        }

        List<String> currentCommandOptions = commandAutoCompleter.getCompletion(commandInput.getText()).getOptions();
        completionPanel.addComponent(new MultiColoredLabel(
                currentCommandOptions.stream()
                        .map(opt -> new ColoredString(opt + " ", null, null))
                        .collect(Collectors.toList()))
                .asComponent()
        );
    }

    private void commitCommand() {
        String command = commandInput.getText();
        setCurrentInput("");
        history.append(command);
        this.listener.onCommand(command);
    }

    private void autocomplete() {
        String completed = commandAutoCompleter.complete(commandInput.getText());
        setCurrentInput(completed);
    }

    private void historyBackward() {
        history.back(commandInput.getText(), this::setCurrentInput);
    }

    private void historyForward() {
        history.forward(this::setCurrentInput);
    }

    private void handleEscape() {
        setCurrentInput("");
        listener.onEmptied();
        history.reset();
    }

    @Override
    public Interactable getInteractable() {
        return this.commandInput;
    }

    public int getHeight() {
        return height;
    }

    private static void registerKeyBind(KeyStroke keyStroke, Consumer<CommandView> action, String description) {
        KEY_BINDS.put(keyStroke, action);
        BIND_HELP.add(new KeyBind(keyStroke, description));
    }

    private static class TokenizedCommandInput {
        private final String prefix;
        private final String beforeCaret;
        private final String afterCaret;

        public TokenizedCommandInput(String prefix, String beforeCaret, String afterCaret) {
            this.prefix = prefix;
            this.beforeCaret = beforeCaret;
            this.afterCaret = afterCaret;
        }
    }
}

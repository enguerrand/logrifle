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
import de.logrifle.ui.completion.CommandAutoCompleter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class CommandView implements InteractableKeystrokeListener {
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
            boolean ctrlDown = keyStroke.isCtrlDown();
            if (ctrlDown) {
                handleCtrlBind(keyStroke);
                return false;
            } else {
                return true;
            }
        });
        messageBox = new SanitizedLabel("");
    }

    private void handleCtrlBind(KeyStroke keyStroke) {
        switch (keyStroke.getKeyType()) {
            case Character:
                Character character = keyStroke.getCharacter();
                handleCtrlBindCharacters(character);
                break;
            case ArrowLeft:
                moveWordLeft();
                break;
            case ArrowRight:
                moveWordRight();
                break;
            default:
                break;
        }
    }

    private void handleCtrlBindCharacters(Character character) {
        switch (character) {
            case 'w': {
                killWord();
                break;
            }
            case 'k': {
                killForward();
                break;
            }
            case 'u': {
                killBackward();
                break;
            }
            case 'y': {
                yank();
                break;
            }
        }
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

    boolean isVisible() {
        return height > 0;
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
        String command = commandInput.getText();
        completionPanel.removeAllComponents();
        if(command.isEmpty()) {
            listener.onEmptied();
            return;
        }
        KeyType keyType = keyStroke.getKeyType();
        switch(keyType) {
            case Escape:
                setCurrentInput("");
                listener.onEmptied();
                history.reset();
                break;
            case Enter:
                setCurrentInput("");
                history.append(command);
                this.listener.onCommand(command);
                break;
            case Tab:
                String completed = commandAutoCompleter.complete(command);
                setCurrentInput(completed);
                break;
            case ArrowUp:
                history.back(commandInput.getText(), this::setCurrentInput);
                break;
            case ArrowDown:
                history.forward(this::setCurrentInput);
                break;
            default:
                break;
        }
        List<String> currentCommandOptions = commandAutoCompleter.getCompletion(commandInput.getText()).getOptions();
        completionPanel.addComponent(new MultiColoredLabel(
                currentCommandOptions.stream()
                        .map(opt -> new ColoredString(opt + " ", null, null))
                        .collect(Collectors.toList()))
                .asComponent()
        );
    }

    @Override
    public Interactable getInteractable() {
        return this.commandInput;
    }

    public int getHeight() {
        return height;
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

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

package de.rochefort.logrifle.ui.cmd;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import de.rochefort.logrifle.base.Strings;
import de.rochefort.logrifle.ui.MainController;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class CommandHandler {
    private final Map<String, Command> commands;
    private MainController mainController;

    public CommandHandler() {
        commands = new HashMap<>();
        register(new Command("prepare", null, "Opens the command line and adds the provided arguments as a prepared command.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.prepareCommand(args);
            }
        });

        register(new Command("bookmark", "b", "Bookmarks the currently focused line.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.toggleBookmark();
            }
        });

        register(new Command("bookmark-move-focus", "bmf", "Bookmarks the currently focused line and moves the focus by the increment provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.toggleBookmarkAndMoveFocus(args);
            }
        });

        register(new Command("prev-bookmark", "pb", "Scrolls to the previous bookmark.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.scrollToPreviousBookmark();
            }
        });

        register(new Command("next-bookmark", "nb", "Scrolls to the next bookmark.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.scrollToNextBookmark();
            }
        });

        register(new Command("delete-filter", "df", "Deletes the currently focused filter.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.deleteFilter();
            }
        });

        register(new Command("delete-highlight", "dh", "Deletes the highlight at the index provided as the first argument") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.deleteHighlight(args);
            }
        });

        register(new Command("!filter", null, "Adds a filter that displays lines that do not match the regex provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.addFilter(args, true, blocking);
            }
        });

        register(new Command("filter", "f", "Adds a filter that displays lines that match the regex provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.addFilter(args, false, blocking);
            }
        });

        register(new Command("edit-filter", "ef", "Edits the currently focused filter.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.editFilter();
            }
        });

        register(new Command("edit-highlight", "eh", "Edits the highlight at the index provided as the first argument") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.editHighlight(args);
            }
        });

        register(new Command("filter-view-up", null, "Moves the filter focus up.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.moveFilterUp();
            }
        });

        register(new Command("filter-view-down", null, "Moves the filter focus down") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.moveFilterDown();
            }
        });

        register(new Command("find", null, "Starts a forward search. (Can also be started using the / key)") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.find(new Query(args, false));
            }
        });

        register(new Command("find-again", null, "Finds the next occurrence of the previous match.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.findAgain();
            }
        });

        register(new Command("find-again-backwards", null, "Finds the previous occurrence of the previous search.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.findAgainBackwards();
            }
        });

        register(new Command("find-backwards", null, "Starts a backwards search. (Can also be started using the ? key)") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.find(new Query(args, true));
            }
        });

        register(new Command("highlight", "h", "Adds a highlight to line parts that match the regex provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.addHighlight(args);
            }
        });

        register(new Command("move-focus", null, "Moves the focus by the increment provided as the first argument. (Negative values move the focus upwards)") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.moveFocus(args);
            }
        });

        register(new Command("quit", null, "Closes the application.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.quit();
            }
        });

        register(new Command("refresh", null, "Enforces a view update.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.refresh();
            }
        });

        register(new Command("hscroll", null, "Scroll horizontally by the increment provided as the first argument. Negative values scroll to the left.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.scrollHorizontally(args);
            }
        });

        register(new Command("scroll", null, "Scrolls vertically by the increment provided as the first argument. Negative values scroll upwards.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.scrollVertically(args);
            }
        });

        register(new Command("scroll-page", null, "Scrolls vertically. The line increment is calculated by multiplying the visible number of lines with the floating point value provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.scrollPage(args);
            }
        });

        register(new Command("goto", null, "Scrolls to the line number provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.scrollToLine(args);
            }
        });

        register(new Command("home", null, "Scrolls to the top.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.scrollToTop();
            }
        });

        register(new Command("end", null, "Scrolls to the bottom and sets \"follow-tail\" to true.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.scrollToBottom();
            }
        });

        register(new Command("toggle-bookmarks-view", null, "Toggles the bookmarks view visibility.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.toggleBookmarks();
            }
        });

        register(new Command("toggle-line-labels", null, "Toggles the full display of line labels on/off") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.toggleLineLabels();
            }
        });

        register(new Command("toggle-sidebar", null, "Toggles the sidebar visibility.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.toggleSidebar();
            }
        });

        register(new Command("toggle-follow-tail", "tail", "Toggles whether or not the current view's tail is followed.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.toggleFollowTail();
            }
        });
    }

    private void register(Command command) {
        this.commands.put(command.getCommandName(), command);
        command.getCommandShortname().ifPresent(shortName ->
                this.commands.put(shortName, command));
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * @return whether the ui should be updated as a result of this command execution
     */
    public ExecutionResult handle(String commandLine) {
        return handle(commandLine, false);
    }

    public ExecutionResult handle(String commandLine, boolean blocking) {
        List<String> words = Arrays.asList(commandLine.split("\\s+", 2));
        if (words.isEmpty()) {
            return new ExecutionResult(false);
        }
        String commandName = words.get(0);
        if (commandName.matches("^\\s*$")) {   // ignore blanks
            return new ExecutionResult(false);
        }
        Command command = commands.get(commandName);
        if (command == null) {
            return new ExecutionResult(false, commandName + ": Command not found.");
        }
        String args;
        if (words.size() < 2) {
            args = "";
        } else {
            args = words.get(1);
        }
        return command.execute(args, blocking);
    }


    public String getHelp(Map<KeyStroke, String> keyMap) {
        String sep = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();
        sb.append(sep);
        sb.append("Options that can be applied on the command line can also be provided through a configuration file located in the user's home directory.");
        sb.append(sep);
        sb.append("The name of this file should be .logriflerc");
        sb.append(sep);
        sb.append("Options can be specified in the format of java properties (i.e. key = value) using the long command line option name with dashes (-) replaced by underscores (_).");
        sb.append(sep);
        List<Command> values = new ArrayList<>(new HashSet<>(this.commands.values()));
        values.sort(Comparator.comparing(Command::getCommandName));
        List<HelpEntry> helpEntries = new ArrayList<>();
        for (Command command : values) {
            String commandName = command.getCommandName();
            String commandShortName = command.getCommandShortname().orElse(null);
            helpEntries.add(new HelpEntry(commandName, commandShortName, command.getDescription()));
        }
        int keyLength = helpEntries.stream().mapToInt(e -> e.getKey().length()).max().orElse(0);
        sb.append(sep);
        sb.append("List of available commands:"+sep);
        sb.append("==========================="+sep);
        for (HelpEntry helpEntry : helpEntries) {
            sb.append(helpEntry.render(keyLength)+sep);
        }
        sb.append("\n");
        sb.append("List of available keybinds:\n");
        sb.append("===========================\n");
        List<KeyBind> binds = new ArrayList<>();
        for (Map.Entry<KeyStroke, String> keyMapping : keyMap.entrySet()) {
            binds.add(new KeyBind(keyMapping.getKey(), keyMapping.getValue()));
        }
        binds.sort(Comparator.comparing(KeyBind::getKey));
        int bindLength = binds.stream().mapToInt(e -> e.getKey().length()).max().orElse(0);
        for (KeyBind bind : binds) {
            sb.append(bind.render(bindLength)+"\n");
        }
        return sb.toString();
    }
    private static class HelpEntry {
        private final String commandName;
        private final @Nullable String commandShortName;
        private final String description;

        private HelpEntry(String commandName, @Nullable String commandShortName, String description) {
            this.commandName = commandName;
            this.commandShortName = commandShortName;
            this.description = description;
        }

        String getKey() {
            return ":" + commandName + (commandShortName != null ? " | :" + commandShortName : "" );
        }

        String render(int keyLength) {
            return Strings.pad(getKey(), keyLength) + " => " + description;
        }
    }

    private static class KeyBind {
        private final String key;
        private final String mapping;

        private KeyBind(KeyStroke keyStroke, String mapping) {
            this.key = renderKeyStroke(keyStroke);
            this.mapping = mapping;
        }

        String getKey() {
            return key;
        }

        String render(int keyLength) {
            return Strings.pad(getKey(), keyLength) + " => :" + mapping;
        }

        private static String renderKeyStroke(KeyStroke keyStroke) {
            StringBuilder sb = new StringBuilder();
            if (keyStroke.isCtrlDown()) {
                sb.append("CTRL+");
            }
            if (keyStroke.isAltDown()) {
                sb.append("ALT+");
            }
            if (keyStroke.getKeyType() == KeyType.Character) {
                sb.append(keyStroke.getCharacter());
            } else {
                sb.append(keyStroke.getKeyType().name());
            }
            return sb.toString();
        }
    }
}

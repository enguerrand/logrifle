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
import java.util.stream.Collectors;

public class CommandHandler {
    private final Map<String, Command> commands;
    private MainController mainController;

    public CommandHandler() {
        commands = new HashMap<>();
        register(new Command("prepare", null, "Opens the command line and adds the provided arguments as a prepared command.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.prepareCommand(args);
            }
        });

        register(new Command("bookmark", "b", "Bookmarks the currently focused line.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.toggleBookmark();
            }
        });

        register(new Command("prev-bookmark", "pb", "Scrolls to the previous bookmark.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.scrollToPreviousBookmark();
            }
        });

        register(new Command("next-bookmark", "nb", "Scrolls to the next bookmark.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.scrollToNextBookmark();
            }
        });

        register(new Command("delete-filter", "df", "Deletes the currently focused filter.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.deleteFilter();
            }
        });

        register(new Command("delete-highlight", "dh", "Deletes the highlight at the index provided as the first argument") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.deleteHighlight(args);
            }
        });

        register(new Command("!filter", null, "Adds a filter that displays lines that do not match the regex provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.addFilter(args, true);
            }
        });

        register(new Command("filter", "f", "Adds a filter that displays lines that match the regex provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.addFilter(args, false);
            }
        });

        register(new Command("edit-filter", "ef", "Edits the currently focused filter.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.editFilter();
            }
        });

        register(new Command("edit-highlight", "eh", "Edits the highlight at the index provided as the first argument") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.editHighlight(args);
            }
        });

        register(new Command("filter-view-up", null, "Moves the filter focus up.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.moveFilterUp();
            }
        });

        register(new Command("filter-view-down", null, "Moves the filter focus down") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.moveFilterDown();
            }
        });

        register(new Command("find", null, "Starts a forward search. (Can also be started using the / key)") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.find(new Query(args, false));
            }
        });

        register(new Command("find-again", null, "Finds the next occurrence of the previous match.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.findAgain();
            }
        });

        register(new Command("find-again-backwards", null, "Finds the previous occurrence of the previous search.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.findAgainBackwards();
            }
        });

        register(new Command("find-backwards", null, "Starts a backwards search. (Can also be started using the ? key)") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.find(new Query(args, true));
            }
        });

        register(new Command("highlight", "h", "Adds a highlight to line parts that match the regex provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.addHighlight(args);
            }
        });

        register(new Command("move-focus", null, "Moves the focus by the increment provided as the first argument. (Negative values move the focus upwards)") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.moveFocus(args);
            }
        });

        register(new Command("quit", null, "Closes the application.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.quit();
            }
        });

        register(new Command("refresh", null, "Enforces a view update.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.refresh();
            }
        });

        register(new Command("hscroll", null, "Scroll horizontally by the increment provided as the first argument. Negative values scroll to the left.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.scrollHorizontally(args);
            }
        });

        register(new Command("scroll", null, "Scroll vertically by the increment provided as the first argument. Negative values scroll upwards.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.scrollVertically(args);
            }
        });

        register(new Command("scroll-page", null, "Scrolls vertically. The line increment is calculated by multiplying the visible number of lines with the floating point value provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.scrollPage(args);
            }
        });

        register(new Command("goto", null, "Scrolls to the line number provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.scrollToLine(args);
            }
        });

        register(new Command("pos1", null, "Scrolls to the top.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.scrollToTop();
            }
        });

        register(new Command("end", null, "Scrolls to the bottom.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.scrollToBottom();
            }
        });

        register(new Command("toggle-bookmarks-view", null, "Toggles the bookmarks view visibility.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.toggleBookmarks();
            }
        });

        register(new Command("toggle-line-labels", null, "Toggles the full display of line labels on/off") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.toggleLineLabels();
            }
        });

        register(new Command("toggle-sidebar", null, "Toggles the sidebar visibility.") {
            @Override
            protected ExecutionResult execute(String args) {
                return mainController.toggleSidebar();
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
        return command.execute(args);
    }


    public void printHelp(Map<KeyStroke, String> keyMap) {
        List<Command> values = new ArrayList<>(new HashSet<>(this.commands.values()));
        values.sort(Comparator.comparing(Command::getCommandName));
        List<HelpEntry> helpEntries = new ArrayList<>();
        for (Command command : values) {
            String commandName = command.getCommandName();
            String commandShortName = command.getCommandShortname().orElse(null);
            List<KeyStroke> keyStrokes = keyMap.entrySet().stream()
                    .filter(s -> s.getValue().equals(commandName))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            helpEntries.add(new HelpEntry(commandName, commandShortName, command.getDescription(), keyStrokes));
        }
        int keyLength = helpEntries.stream().mapToInt(e -> e.getKey().length()).max().orElse(0);
        int bindLength = helpEntries.stream().mapToInt(e -> e.getBinds().length()).max().orElse(0);
        System.out.println("Synopsis:");
        System.out.println("===========================");
        System.out.println("java -jar logrifle.jar -h | --help | logfile1 [ logfile2 [ ... logfileN ] ]");
        System.out.println("");
        System.out.println("List of available commands:");
        System.out.println("===========================");
        for (HelpEntry helpEntry : helpEntries) {
            System.out.println(helpEntry.render(keyLength, bindLength));
        }
    }
    private static class HelpEntry {
        private final String commandName;
        private final @Nullable String commandShortName;
        private final String description;
        private final List<KeyStroke> keyBinds;

        private HelpEntry(String commandName, @Nullable String commandShortName, String description, List<KeyStroke> keyBinds) {
            this.commandName = commandName;
            this.commandShortName = commandShortName;
            this.description = description;
            this.keyBinds = keyBinds;
        }

        String getKey() {
            return ":" + commandName + (commandShortName != null ? " | :" + commandShortName : "" );
        }

        String getBinds() {
            return "(" + keyBinds.stream().map(this::renderKeyStroke).collect(Collectors.joining(", ")) +")";
        }

        private String renderKeyStroke(KeyStroke keyStroke) {
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

        String getDescription() {
            return description;
        }

        String render(int keyLength, int bindLength) {
            return Strings.pad(getKey(), keyLength) + " " + Strings.pad(getBinds(), bindLength) + ": " + getDescription();
        }
    }
}

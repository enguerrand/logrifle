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

package de.logrifle.ui.cmd;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import de.logrifle.base.Strings;
import de.logrifle.ui.HighlightingTextColors;
import de.logrifle.ui.LineLabelDisplayMode;
import de.logrifle.ui.MainController;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

        register(new Command("clear-bookmarks", "cb", "Clears all bookmarks.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.clearBookmarks();
            }
        });

        register(new Command("write-bookmarks", "wb", "Writes all bookmarks to the file provided as the first parameter.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.writeBookmarks(args);
            }
        });

        register(new Command("toggle-forced-bookmarks-display", "tfb", "Toggles the forced display of bookmarked lines.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.toggleForceDisplayBookmarks();
            }
        });

        register(new Command("write-current-view", "wcv", "Writes all lines in the current view to the file provided as the first parameter.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.writeView(args);
            }
        });

        register(new Command("toggle-line-numbers", "tln", "Toggles display of line numbers on and off.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.toggleLineNumbers();
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

        register(new Command("ifilter!", "if!", "Adds a filter that displays lines that do not case-insensitively match the regex provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.addFilter(args, true, true, blocking);
            }
        });

        register(new Command("filter!", "f!", "Adds a filter that displays lines that do not match the regex provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.addFilter(args, true, false, blocking);
            }
        });

        register(new Command("filter", "f", "Adds a filter that displays lines that match the regex provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.addFilter(args, false, false, blocking);
            }
        });

        register(new Command("ifilter", "if", "Adds a filter that displays lines that case-insensitively match the regex provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.addFilter(args, false, true, blocking);
            }
        });

        register(new Command("filter-highlight", "fh", "Adds a filter that displays lines that match the regex provided as the first argument and highlights the matching text.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                ExecutionResult filterResult = mainController.addFilter(args, false, false, blocking);
                ExecutionResult highlightResult = mainController.addHighlight(args, false, null);
                return ExecutionResult.merged(filterResult, highlightResult);
            }
        });

        register(new Command("ifilter-highlight", "ifh", "Adds a filter that displays lines that case-insensitively match the regex provided as the first argument and highlights the matching text.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                ExecutionResult filterResult = mainController.addFilter(args, false, true, blocking);
                ExecutionResult highlightResult = mainController.addHighlight(args, true, null);
                return ExecutionResult.merged(filterResult, highlightResult);
            }
        });

        register(new Command("edit-filter", "ef", "Edits the currently focused filter.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.editFilter();
            }
        });

        register(new Command("replace-filter", "rf", "Replaces the currently focused filter's pattern with the pattern provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.replaceFilter(args, blocking);
            }
        });

        register(new Command("edit-highlight", "eh", "Edits the highlight at the index provided as the first argument") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.editHighlight(args);
            }
        });

        register(new Command("filter-view-prev", null, "Moves the filter focus to the previous view on the same level.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.moveFilterPrev();
            }
        });

        register(new Command("filter-view-next", null, "Moves the filter focus to the previous view on the same level.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.moveFilterNext();
            }
        });

        register(new Command("filter-view-up", null, "Moves the filter focus to the parent level.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.moveFilterParent();
            }
        });

        register(new Command("filter-view-down", null, "Moves the filter focus to the first child view") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.moveFilterFirstChild();
            }
        });

        register(new Command("jump", null, "Jumps to the view with the index given as the first argument.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.moveFilterTo(args);
            }
        });

        register(new Command("ifind", null, "Starts a case insensitive a forward search.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.find(new Query(args, false, true));
            }
        });

        register(new Command("find", null, "Starts a forward search. (Can also be started using the / key)") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.find(new Query(args, false, false));
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
                return mainController.find(new Query(args, true, false));
            }
        });

        register(new Command("ifind-backwards", null, "Starts a case-insensitive backwards search.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.find(new Query(args, true, true));
            }
        });

        register(new Command("highlight", "h", "Adds an auto-color highlight to line parts that match the regex provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.addHighlight(args, false, null);
            }
        });

        register(new Command("ihighlight", "ih", "Adds an auto-color highlight to line parts that case-insensitively match the regex provided as the first argument.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.addHighlight(args, true, null);
            }
        });

        for (HighlightingTextColors highlightColors : HighlightingTextColors.values()) {
            String highlightName = highlightColors.name().toLowerCase();
            register(new Command("highlight-" + highlightName, "h-"+highlightName, "Adds a " + highlightName + " highlight to line parts that match the regex provided as the first argument.") {
                @Override
                protected ExecutionResult execute(String args, boolean blocking) {
                    return mainController.addHighlight(args, false, highlightColors);
                }
            });

            register(new Command("ihighlight-" + highlightName, "ih-" + highlightName, "Adds a " +  highlightName + " highlight to line parts that case-insensitively match the regex provided as the first argument.") {
                @Override
                protected ExecutionResult execute(String args, boolean blocking) {
                    return mainController.addHighlight(args, true, highlightColors);
                }
            });
        }

        register(new Command("move-focus", null, "Moves the focus by the increment provided as the first argument. (Negative values move the focus upwards)") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.moveFocus(args);
            }
        });

        register(new Command("detail", "d", "Toggles complete display (including additional lines such as stack traces) for the currently focused line. Detailed display is deactivated upon focus loss.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.toggleDetail();
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

        register(new Command("lstart", null, "Scroll horizontally to the currently focused line's beginning.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.scrollToLineStart();
            }
        });

        register(new Command("lend", null, "Scroll horizontally to the currently focused line's end.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.scrollToLineEnd();
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
                return mainController.gotoLine(args);
            }
        });

        register(new Command("home", null, "Scrolls to the top.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.scrollToTop();
            }
        });

        register(new Command("end", null, "Moves the focus to the last line and sets \"follow-tail\" to true.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.moveFocusToEnd();
            }
        });

        register(new Command("toggle-bookmarks-view", null, "Toggles the bookmarks view visibility.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.toggleBookmarks();
            }
        });

        register(new Command("line-labels", null, "Cycles through line label display modes: " + Arrays.stream(LineLabelDisplayMode.values()).map(LineLabelDisplayMode::getDescription).collect(Collectors.joining(", "))) {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.cycleLineLabelDisplayMode();
            }
        });

        register(new Command("toggle-sidebar", null, "Toggles the sidebar visibility.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.toggleSidebar();
            }
        });

        register(new Command("sidebar-max-cols", null, "Sets the sidebar's maximum width in columns.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.setMaxSidebarWidthColums(args);
            }
        });

        register(new Command("sidebar-max-ratio", null, "Sets the sidebar's maximum width relative to the full window width.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.setMaxSidebarWidthRatio(args);
            }
        });

        register(new Command("toggle-file", "tf", "Toggles the visibility of a file. The file's index is specified as the first parameter.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.toggleViewVisible(args);
            }
        });

        register(new Command("open", "of", "Loads a logfile into the current view. The first parameter specifies the path to the file.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.openFile(args);
            }
        });

        register(new Command("close", "cf", "Closes the file at the index provided by the first parameter and removes it from the view.") {
            @Override
            protected ExecutionResult execute(String args, boolean blocking) {
                return mainController.closeFile(args);
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

    public List<String> getAvailableCommands() {
        List<String> commands = new ArrayList<>(this.commands.keySet());
        commands.sort(Comparator.naturalOrder());
        return commands;
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

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public String getHelp(Map<KeyStroke, String> keyMap, Collection<KeyBind> commandViewBinds) {
        String sep = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();
        sb.append(sep);
        sb.append("Options that can be applied on the command line can also be provided through a configuration file located in the user's home directory.");
        sb.append(sep);
        sb.append("The name of this file should be .logriflerc");
        sb.append(sep);
        sb.append("Options can be specified in the format of java properties (i.e. key = value) using the long command line option name with dashes (-) replaced by underscores (_).");
        sb.append(sep);
        sb.append("Boolean options must be assigned the desired value (as opposed to how it is done on the command line where you only specify the option without arguments).");
        sb.append(sep);
        sb.append("Example:");
        sb.append(sep);
        sb.append("    timestamp_format = HH:mm:ss");
        sb.append(sep);
        sb.append("    forced_bookmarks_display = true");
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
            KeyBind bind = new KeyBind(keyMapping.getKey(), keyMapping.getValue());
            boolean duplicate = binds.stream()
                    .map(KeyBind::renderKeyStroke)
                    .anyMatch(rendered ->
                            rendered.equals(bind.renderKeyStroke())
                    );
            if (!duplicate) {
                binds.add(bind);
            }
        }
        Comparator<KeyBind> typeComparator = Comparator.comparing(bind -> bind.getKeyStroke().getKeyType() != KeyType.Character);
        binds.sort(typeComparator
                .thenComparing(KeyBind::getModifiersCount)
                .thenComparing(keyBind -> {
                    KeyStroke keyStroke = keyBind.getKeyStroke();
                    String rendered = keyBind.renderKeyStroke();
                    if (keyStroke.getKeyType() == KeyType.Character) {
                        return rendered.toLowerCase();  // Lowercase first
                    }
                    rendered = Pattern.compile("(.*)F([0-9]$)").matcher(rendered).replaceFirst("$1F0$2");  // F10 after F9
                    return rendered;
                })
        );
        int bindLength = binds.stream().mapToInt(e -> e.renderKeyStroke().length()).max().orElse(0);
        for (KeyBind bind : binds) {
            sb.append(bind.render(bindLength, ":")).append("\n");
        }

        sb.append("\n");
        sb.append("Keybinds in command input mode:\n");
        sb.append("======================================\n");
        int commandViewBindLength = commandViewBinds.stream().mapToInt(e -> e.renderKeyStroke().length()).max().orElse(0);
        for (KeyBind commandViewBind : commandViewBinds) {
            sb.append(commandViewBind.render(commandViewBindLength, "")).append("\n");
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
            return Strings.pad(getKey(), keyLength, false) + " => " + description;
        }
    }

}

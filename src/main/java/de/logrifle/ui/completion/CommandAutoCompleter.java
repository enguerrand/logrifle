/*
 *  Copyright 2020, Enguerrand de Rochefort
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

package de.logrifle.ui.completion;

import de.logrifle.base.Strings;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandAutoCompleter {
    private final String prefix;
    private final List<String> allCommands;
    private final Map<String, AbstractArgumentCompleter> argumentCompletersLookup = new HashMap<>();

    public CommandAutoCompleter(String prefix, List<String> allCommands, AbstractArgumentCompleter... argumentCompleters) {
        this.prefix = prefix;
        this.allCommands = allCommands;
        for (AbstractArgumentCompleter argumentCompleter : argumentCompleters) {
            for (String commandName : argumentCompleter.getCommandNames()) {
                argumentCompletersLookup.put(commandName, argumentCompleter);
            }
        }
    }

    public int getMaximumCommandLength(String currentInput) {
        CompletionResult completion = getCompletion(currentInput);
        int longestCompletion = completion.getMatchingFullCompletions().stream()
                .map(String::length)
                .max(Comparator.naturalOrder())
                .orElse(0) + prefix.length();
        return Math.max(longestCompletion, currentInput.length());
    }

    public CompletionResult getCompletion(String currentInput) {
        if (!currentInput.startsWith(prefix)) {
            return CompletionResult.NO_MATCHES;
        }
        String currentCommand = currentInput.substring(prefix.length());

        String[] tokens = currentCommand.split("\\s+");
        if (tokens.length <= 1 && !Pattern.compile(".*\\s$").matcher(currentInput).matches()) {
            List<String> matches = allCommands.stream()
                    .filter(c -> c.startsWith(currentCommand))
                    .collect(Collectors.toList());
            return new CompletionResult(matches, matches);
        } else {
            String arguments = currentInput.substring(prefix.length() + tokens[0].length());
            String trimmedArguments = Strings.trimStart(arguments);
            String commandName = tokens[0];
            @Nullable AbstractArgumentCompleter argumentCompleter = this.argumentCompletersLookup.get(commandName);
            if (argumentCompleter != null) {
                String keptPart = currentCommand.substring(0, currentCommand.length() - trimmedArguments.length());
                List<String> matches = argumentCompleter.getCompletions(trimmedArguments);
                List<String> matchingCompletions = matches.stream()
                        .map(completion -> keptPart + completion)
                        .collect(Collectors.toList());
                return new CompletionResult(matches, matchingCompletions);
            } else {
                return CompletionResult.NO_MATCHES;
            }
        }
    }

    public String complete(String currentInput) {
        List<String> completions = getCompletion(currentInput).getMatchingFullCompletions();
        if (completions.isEmpty()) {
            return currentInput;
        }
        String currentInputStripped = currentInput.substring(prefix.length());

        List<String> stripped = completions.stream()
                .map(s -> s.replaceFirst(currentInputStripped, ""))
                .collect(Collectors.toList());
        int maxLength = stripped.stream()
                .map(String::length)
                .min(Comparator.naturalOrder())
                .orElse(0);

        StringBuilder toAppend = new StringBuilder();
        boolean charsMatching = true;
        for (int charIndex = 0; charIndex < maxLength && charsMatching; charIndex++) {
            Character c = null;
            for (int i = 0; i < stripped.size(); i++) {
                String s = stripped.get(i);
                char nextChar = s.charAt(charIndex);
                if (i == 0) {
                    c = nextChar;
                } else if (c != nextChar) {
                    charsMatching = false;
                    break;
                }
            }
            if (charsMatching) {
                toAppend.append(c);
            }
        }
        return currentInput + toAppend.toString();
    }
}

/*
 *  Copyright 2021, Enguerrand de Rochefort
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileArgumentsCompleter extends AbstractArgumentCompleter {
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final Pattern TRUNCATION_PATTERN = Pattern.compile(
                    "[^"
                    + Pattern.quote(FILE_SEPARATOR)
                    + "]*$"
    );
    private static final Pattern REMAINDER_PATTERN = Pattern.compile(
            "^.*" + Pattern.quote(FILE_SEPARATOR)
    );
    private final Path workingDirectory;

    public FileArgumentsCompleter(Path workingDirectory, String... commandNames) {
        super(commandNames);
        this.workingDirectory = workingDirectory;
    }

    @Override
    public List<String> getCompletions(String currentInput) {
        String truncated = TRUNCATION_PATTERN.matcher(currentInput).replaceAll("");
        String remainder = REMAINDER_PATTERN.matcher(currentInput).replaceAll("");
        Path lookingAt = workingDirectory.resolve(truncated);
        try {
            return Files.list(lookingAt)
                    .map(f -> f.getFileName().toString())
                    .filter(name -> name.startsWith(remainder))
                    .map(name -> Paths.get(truncated, name))
                    .map(path -> {
                        String stringified = path.toString();
                        if (Files.isDirectory(workingDirectory.resolve(path))) {
                            stringified = stringified + FILE_SEPARATOR;
                        }
                        return stringified;
                    })
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}

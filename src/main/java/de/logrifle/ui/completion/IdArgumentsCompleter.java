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

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class IdArgumentsCompleter extends AbstractArgumentCompleter {
    private final Supplier<Collection<Integer>> idsSupplier;

    public IdArgumentsCompleter(Supplier<Collection<Integer>> idsSupplier, String... commandNames) {
        super(commandNames);
        this.idsSupplier = idsSupplier;
    }

    @Override
    public CompletionResult getCompletions(String currentArgs) {
        List<String> options = idsSupplier.get().stream()
                .map(String::valueOf)
                .filter(s -> s.startsWith(currentArgs))
                .distinct()
                .collect(Collectors.toList());
        return new CompletionResult(options);
    }
}

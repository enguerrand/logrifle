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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class IndexArgumentsCompleter extends AbstractArgumentCompleter {
    private final Supplier<Integer> collectionSizeSupplier;

    public IndexArgumentsCompleter(Supplier<Integer> collectionSizeSupplier, String... commandNames) {
        super(commandNames);
        this.collectionSizeSupplier = collectionSizeSupplier;
    }

    @Override
    public List<String> getCompletions(String currentArgs) {
        List<String> options = new ArrayList<>();
        Integer size = collectionSizeSupplier.get();
        if (size == null)  {
            size = 0;
        }
        for (int i = 0; i < size; i++) {
            String index = String.valueOf(i);
            if (index.startsWith(currentArgs)) {
                options.add(index);
            }
        }
        return options;
    }
}

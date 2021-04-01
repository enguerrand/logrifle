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

package de.logrifle.data.parsing;

import de.logrifle.ui.LineLabelDisplayMode;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class Lines {
    private Lines() {
        throw new IllegalStateException("Do not instantiate!");
    }

    public static Collection<String> export(Collection<Line> lines, LineLabelDisplayMode lineLabelDisplayMode) {
        if (lines.isEmpty()) {
            return Collections.emptyList();
        }
        int maxLength = lineLabelDisplayMode.getMaxLength();
        int wantedLength = Math.min(
                lines.stream()
                .map(Line::getLineLabel)
                .map(String::length)
                .max(Comparator.naturalOrder())
                .get(),
                maxLength
        );
        return lines.stream()
                .map(l -> l.export(wantedLength))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}

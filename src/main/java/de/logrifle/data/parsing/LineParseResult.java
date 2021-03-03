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

package de.logrifle.data.parsing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LineParseResult {
    private final @Nullable Line parsedLine;
    private final @Nullable String text;
    private final boolean newLine;

    private LineParseResult(@Nullable Line parsedLine, @Nullable String text, boolean newLine) {
        this.parsedLine = parsedLine;
        this.text = text;
        this.newLine = newLine;
    }

    LineParseResult(@NotNull Line parsedLine) {
        this(parsedLine, null, true);
    }

    LineParseResult(@NotNull String text) {
        this(null, text, false);
    }

    public Line getParsedLine() {
        if(parsedLine == null) {
            throw new IllegalStateException("No parsed line available! Check whether isNewLine() is true before calling this method.");
        }
        return parsedLine;
    }

    public String getText() {
        if(text == null) {
            throw new IllegalStateException("No text available! Check whether isNewLine() is false before calling this method.");
        }
        return text;
    }

    public boolean isNewLine() {
        return newLine;
    }

    @Override
    public String toString() {
        return "LineParseResult{" +
                "parsedLine=" + parsedLine +
                ", text='" + text + '\'' +
                ", newLine=" + newLine +
                '}';
    }
}

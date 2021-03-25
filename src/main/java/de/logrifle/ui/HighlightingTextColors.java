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

package de.logrifle.ui;

import com.googlecode.lanterna.TextColor;

public enum HighlightingTextColors {
        YELLOW(TextColor.ANSI.BLACK, TextColor.ANSI.YELLOW),
        CYAN(TextColor.ANSI.BLACK, TextColor.ANSI.CYAN),
        MAGENTA(TextColor.ANSI.BLACK, TextColor.ANSI.MAGENTA),
        BLUE(TextColor.ANSI.WHITE, TextColor.ANSI.BLUE),
        RED(TextColor.ANSI.WHITE, TextColor.ANSI.RED),
        ;

        private final TextColor foreground;
        private final TextColor background;

    HighlightingTextColors(TextColor foreground, TextColor background) {
            this.foreground = foreground;
            this.background = background;
        }

    public TextColor getForeground() {
        return foreground;
    }

    public TextColor getBackground() {
        return background;
    }
}

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

public class TextColors {
    public enum Highlights {
        YELLOW(new TextColors(TextColor.ANSI.BLACK, TextColor.ANSI.YELLOW)),
        CYAN(new TextColors(TextColor.ANSI.BLACK, TextColor.ANSI.CYAN)),
        MAGENTA(new TextColors(TextColor.ANSI.BLACK, TextColor.ANSI.MAGENTA)),
        BLUE(new TextColors(TextColor.ANSI.WHITE, TextColor.ANSI.BLUE)),
        RED(new TextColors(TextColor.ANSI.WHITE, TextColor.ANSI.RED)),
        ;

        private final TextColors colors;

        Highlights(TextColors colors) {
            this.colors = colors;
        }

        public TextColors getColors() {
            return colors;
        }
    }

    private final TextColor foreground;
    private final TextColor background;

    public TextColors(TextColor foreground, TextColor background) {
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

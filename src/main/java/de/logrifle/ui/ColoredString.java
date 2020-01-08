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

package de.logrifle.ui;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ColoredString {
    private final String text;
    private final @Nullable TextColor fgColor;
    private final @Nullable TextColor bgColor;
    private final List<SGR> styles;

    public ColoredString(String text, @Nullable TextColor fgColor, @Nullable TextColor bgColor, SGR... styles) {
        this.text = text;
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        this.styles = styles != null ? Arrays.asList(styles) : Collections.emptyList();
    }

    public String getText() {
        return text;
    }

    public Optional<TextColor> getFgColor() {
        return Optional.ofNullable(fgColor);
    }

    public Optional<TextColor> getBgColor() {
        return Optional.ofNullable(bgColor);
    }

    public List<SGR> getStyles() {
        return styles;
    }
}

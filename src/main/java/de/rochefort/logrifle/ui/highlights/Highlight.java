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

package de.rochefort.logrifle.ui.highlights;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Highlight {
    private final String regex;
    private final Pattern pattern;
    private final @Nullable TextColor fgColor;
    private final @Nullable TextColor bgColor;
    private final List<SGR> styles;

    public Highlight(String regex, @Nullable TextColor fgColor, @Nullable TextColor bgColor, SGR... styles) {
        this.pattern = Pattern.compile(regex);
        this.regex = regex;
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        this.styles = styles != null ? Arrays.asList(styles) : Collections.emptyList();
    }

    public String getRegex() {
        return regex;
    }

    public TextColor getFgColor() {
        return fgColor;
    }

    public TextColor getBgColor() {
        return bgColor;
    }

    public List<SGR> getStyles() {
        return styles;
    }

    public List<MatchedSection> getMatches(String text) {
        List<MatchedSection> sections = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            sections.add(new MatchedSection(this, matcher.start(0), matcher.end(0)));
        }
        return sections;
    }

}

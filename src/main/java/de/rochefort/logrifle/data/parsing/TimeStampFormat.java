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

package de.rochefort.logrifle.data.parsing;

import org.jetbrains.annotations.Nullable;

public class TimeStampFormat {
    private static final String DEFAULT_TIME_MATCH_REGEX = ".*(\\d{2}:\\d{2}:\\d{2}\\.\\d{3}).*";
    private static final String DEFAULT_DATE_FORMAT = "HH:mm:ss.SSS";
    private final String regex;
    private final String format;

    public TimeStampFormat(@Nullable String regex, @Nullable String format) {
        this.regex = regex != null ? regex : DEFAULT_TIME_MATCH_REGEX;
        this.format = format != null ? format : DEFAULT_DATE_FORMAT;
    }

    public String getRegex() {
        return regex;
    }

    public String getFormat() {
        return format;
    }
}

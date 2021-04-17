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

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static de.logrifle.data.parsing.TimeStampFormats.MILLIS_TIME_MATCH_REGEX;
import static de.logrifle.data.parsing.TimeStampFormats.MILLIS_DATE_FORMAT;

public class TimeStampFormat {
    private final String regex;
    private final String format;

    public TimeStampFormat(@Nullable String regex, @Nullable String format) {
        this.regex = regex != null ? regex : MILLIS_TIME_MATCH_REGEX;
        this.format = format != null ? format : MILLIS_DATE_FORMAT;
    }

    public String getRegex() {
        return regex;
    }

    public String getFormat() {
        return format;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeStampFormat that = (TimeStampFormat) o;
        return Objects.equals(regex, that.regex) &&
                Objects.equals(format, that.format);
    }

    @Override
    public int hashCode() {
        return Objects.hash(regex, format);
    }

    @Override
    public String toString() {
        return "TimeStampFormat{" +
                "regex='" + regex + '\'' +
                ", format='" + format + '\'' +
                '}';
    }
}

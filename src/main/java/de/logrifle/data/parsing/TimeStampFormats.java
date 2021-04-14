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

public class TimeStampFormats {
    public static final String DEFAULT_TIME_MATCH_REGEX = ".*\\b(\\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\b.*";
    public static final String DEFAULT_DATE_FORMAT = "HH:mm:ss.SSS";
    public static final String SECONDS_TIME_MATCH_REGEX = ".*\\b(\\d{2}:\\d{2}:\\d{2})\\b.*";
    public static final String SECONDS_DATE_FORMAT = "HH:mm:ss";

    private TimeStampFormats() {
        throw new IllegalStateException("Do not instantiate");
    }
}

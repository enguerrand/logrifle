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

import de.logrifle.data.views.LineSource;

import java.util.concurrent.TimeUnit;

public abstract class LineParser {
    public static final long DATE_CHANGE_THRESHOLD_MILLIS = TimeUnit.SECONDS.toMillis(5L);
    private long lastParsedTimestamp = 0L;
    private long dateChangeCount = 0;

    public abstract LineParseResult parse(int index, String raw, LineSource source);

    protected long updateAndGetDateChangeCount(long parsedTimeStamp) {
        if (parsedTimeStamp + DATE_CHANGE_THRESHOLD_MILLIS < lastParsedTimestamp) {
            ++dateChangeCount;
        }
        lastParsedTimestamp = parsedTimeStamp;
        return dateChangeCount;
    }
}

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

package de.logrifle.data.io;

import java.util.List;

public class ZipEntryLines {
    private final String entryName;
    private final List<String> lines;

    public ZipEntryLines(String entryName, List<String> lines) {
        this.entryName = entryName;
        this.lines = lines;
    }

    public String getEntryName() {
        return entryName;
    }

    public List<String> getLines() {
        return lines;
    }
}

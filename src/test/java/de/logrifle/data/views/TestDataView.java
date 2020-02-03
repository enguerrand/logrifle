/*
 *  Copyright 2020, Enguerrand de Rochefort
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

package de.logrifle.data.views;

import com.googlecode.lanterna.TextColor;
import de.logrifle.base.LogDispatcher;
import de.logrifle.data.parsing.Line;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestDataView extends DataView {
    private final List<Line> lines = new ArrayList<>();

    public TestDataView(LogDispatcher dispatcher, String title) {
        super(title, TextColor.ANSI.DEFAULT, dispatcher, 1);
    }

    public TestDataView(LogDispatcher dispatcher, String title, Collection<Line> lines) {
        this(dispatcher, title);
        this.lines.addAll(lines);
    }

    @Override
    public List<Line> getAllLines() {
        return lines;
    }

    @Override
    public void onUpdated(DataView source) {
    }

    @Override
    protected void clearCacheImpl() {
    }
}

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

package de.logrifle.data.io;

import com.googlecode.lanterna.TextColor;
import de.logrifle.base.LogDispatcher;
import de.logrifle.data.parsing.LineParser;
import de.logrifle.data.views.DataView;
import de.logrifle.ui.RingIterator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class ZipFileOpenerImpl implements FileOpener {
    private final LineParser lineParser;
    private final RingIterator<TextColor> textColorIterator;
    private final LogDispatcher logDispatcher;

    ZipFileOpenerImpl(
            LineParser lineParser,
            RingIterator<TextColor> textColorIterator,
            LogDispatcher logDispatcher
    ) {
        this.lineParser = lineParser;
        this.textColorIterator = textColorIterator;
        this.logDispatcher = logDispatcher;
    }

    public Collection<DataView> open(Path path) throws IOException {
        List<DataView> dataViews = new ArrayList<>();
        for (ZipEntryLines zipEntryLines : ZipFiles.readAllLines(path)) {
            dataViews.add(
                    new StaticLogReader(zipEntryLines.getLines(), lineParser, textColorIterator.next(), logDispatcher, zipEntryLines.getEntryName())
            );
        }
        return dataViews;
    }
}

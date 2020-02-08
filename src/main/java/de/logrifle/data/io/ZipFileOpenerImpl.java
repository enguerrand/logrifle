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

import de.logrifle.base.LogDispatcher;
import de.logrifle.base.RateLimiterFactory;
import de.logrifle.data.parsing.LineParser;
import de.logrifle.data.views.DataView;
import de.logrifle.ui.TextColorIterator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

class ZipFileOpenerImpl implements FileOpener {
    private final LineParser lineParser;
    private final TextColorIterator textColorIterator;
    private final ExecutorService workerPool;
    private final LogDispatcher logDispatcher;
    private final RateLimiterFactory factory;

    ZipFileOpenerImpl(
            LineParser lineParser,
            TextColorIterator textColorIterator,
            ExecutorService workerPool,
            LogDispatcher logDispatcher,
            RateLimiterFactory factory) {
        this.lineParser = lineParser;
        this.textColorIterator = textColorIterator;
        this.workerPool = workerPool;
        this.logDispatcher = logDispatcher;
        this.factory = factory;
    }

    @Override
    public Collection<DataView> open(Path path) throws IOException {
        try {
            ZipFile zip = new ZipFile(path.toFile());
            List<DataView> views = new ArrayList<>();
            for (Enumeration e = zip.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                InputStream inputStream = zip.getInputStream(entry);
                views.add(new LogInputStreamReader(inputStream, lineParser, textColorIterator.next(), logDispatcher, entry.getName()));
            }
            return views;
        } catch (ZipException e) {
            throw new UnexpectedFileFormatException(e);
        }
    }
}

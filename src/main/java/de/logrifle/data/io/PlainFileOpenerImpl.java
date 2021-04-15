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
import de.logrifle.base.RateLimiterFactory;
import de.logrifle.data.parsing.LineParser;
import de.logrifle.data.parsing.LineParserProvider;
import de.logrifle.data.views.DataView;
import de.logrifle.ui.RingIterator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

class PlainFileOpenerImpl extends FileOpener {
    private final RingIterator<TextColor> textColorIterator;
    private final ExecutorService workerPool;
    private final LogDispatcher logDispatcher;
    private final RateLimiterFactory factory;
    private final Charset charset;

    PlainFileOpenerImpl(LineParserProvider lineParserProvider, RingIterator<TextColor> textColorIterator, ExecutorService workerPool, LogDispatcher logDispatcher, RateLimiterFactory factory, Charset charset) {
        super(lineParserProvider);
        this.textColorIterator = textColorIterator;
        this.workerPool = workerPool;
        this.logDispatcher = logDispatcher;
        this.factory = factory;
        this.charset = charset;
    }

    @Override
    public Collection<DataView> open(Path path) {
        LineParser lineParser = getParserFor((desiredLinesCount) -> {
            try {
                return Files.lines(path, charset)
                        .limit(desiredLinesCount)
                        .collect(Collectors.toList())
                ;
            } catch (IOException e) {
                return Collections.emptyList();
            }
        });

        return Collections.singleton(new LogReader(lineParser, path, textColorIterator.next(), workerPool, logDispatcher, factory, charset));
    }
}

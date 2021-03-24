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
import de.logrifle.data.views.DataView;
import de.logrifle.ui.RingIterator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

public class MainFileOpenerImpl implements FileOpener {
    private final Map<Pattern, FileOpener> fileOpeners = new LinkedHashMap<>();
    private final Charset charset;

    public MainFileOpenerImpl(
            LineParser lineParser,
            RingIterator<TextColor> textColorIterator,
            ExecutorService workerPool,
            LogDispatcher logDispatcher,
            RateLimiterFactory factory,
            Charset charset) {
        this.charset = charset;
        fileOpeners.put(Pattern.compile(".*\\.zip"), new ZipFileOpenerImpl(
                lineParser,
                textColorIterator,
                logDispatcher
        ));
        fileOpeners.put(Pattern.compile(".*"), new PlainFileOpenerImpl(
                lineParser,
                textColorIterator,
                workerPool,
                logDispatcher,
                factory,
                this.charset));
    }


    @Override
    public Collection<DataView> open(Path path) throws IOException {
        for (Map.Entry<Pattern, FileOpener> entry : fileOpeners.entrySet()) {
            Pattern pattern = entry.getKey();
            if (pattern.matcher(path.getFileName().toString()).matches()) {
                FileOpener fileOpener = entry.getValue();
                try {
                    return fileOpener.open(path);
                } catch (UnexpectedFileFormatException e) {
                    // The pattern matched but the file's format was not consistent with its name. Continue trying other file openers.
                    continue;
                }
            }
        }
        return Collections.emptyList();
    }
}

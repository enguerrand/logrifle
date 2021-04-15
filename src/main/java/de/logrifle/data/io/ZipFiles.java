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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZipFiles {
    public static List<ZipEntryLines> readAllLines(Path path, Charset charset) throws IOException {
        List<ZipEntryLines> result = new ArrayList<>();
        for (InputStreamAndName inputStreamAndName : open(path)) {
            try {
                result.add(
                        readAllLines(inputStreamAndName, charset)
                );
            } finally {
                inputStreamAndName.getInputStream().close();
            }
        }
        return result;
    }

    public static Collection<InputStreamAndName> open(Path path) throws IOException {
        List<InputStreamAndName> openedStreams = new ArrayList<>();
        try {
            ZipFile zip = new ZipFile(path.toFile());
            for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = e.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                InputStream inputStream = zip.getInputStream(entry);
                openedStreams.add(new InputStreamAndName(entry.getName(), inputStream));
            }
            return openedStreams;
        } catch (ZipException e) {
            throw new UnexpectedFileFormatException(e);
        }
    }

    public static ZipEntryLines readAllLines(InputStreamAndName inputStreamAndName, Charset charset) throws IOException {
        List<String> lines = new LinkedList<>();
        String raw;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStreamAndName.getInputStream(), charset));
        while ((raw = reader.readLine()) != null) {
            lines.add(raw);
        }
        reader.close();
        return new ZipEntryLines(inputStreamAndName.getName(), lines);
    }

}

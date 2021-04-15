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

package de.logrifle.data.parsing;

import de.logrifle.data.io.FormatDetectionFailedException;

import java.util.Collection;

public class LineParserProviderDynamicImpl implements LineParserProvider {
    private final int lineCount;
    private final TimeStampFormats timeStampFormats;

    public LineParserProviderDynamicImpl(int lineCount, TimeStampFormats timeStampFormats) {
        this.lineCount = lineCount;
        this.timeStampFormats = timeStampFormats;
    }

    @Override
    public LineParser getParserFor(SampleContentFetcher sampleContentFetcher) throws FormatDetectionFailedException {
        Collection<String> sampleContent = sampleContentFetcher.getSampleContent(lineCount);
        TimeStampFormat autoDetectedFormat = timeStampFormats.autoDetectFormat(sampleContent)
                .orElseThrow(() -> new FormatDetectionFailedException("Failed to auto-detect time stamp format. Please specify it manually"));
        return new LineParserTimestampedTextImpl(autoDetectedFormat);
    }
}
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
import de.logrifle.base.RateLimiterFactoryTestImpl;
import de.logrifle.data.parsing.Line;
import de.logrifle.data.parsing.LineParser;
import de.logrifle.data.parsing.LineParserTimestampedTextImpl;
import de.logrifle.data.parsing.TimeStampFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class DataViewTest {

    private final LineParser parser = new LineParserTimestampedTextImpl(
            new TimeStampFormat(TimeStampFormat.DEFAULT_TIME_MATCH_REGEX, TimeStampFormat.DEFAULT_DATE_FORMAT)
    );

    private final LogDispatcher dispatcher = new LogDispatcher();

    @Test
    void getAllLinesShouldBeSorted() throws InterruptedException {
        int jobCountMergedViewInstantiation = 1;
        int jobCountLineAddition = 6;
        int expectedJobCount = jobCountMergedViewInstantiation + jobCountLineAddition;
        RateLimiterFactoryTestImpl factory = new RateLimiterFactoryTestImpl(expectedJobCount);
        Line line1 = parser.parse(0, "15:24:01.038 line1", "line1", TextColor.ANSI.DEFAULT).getParsedLine();
        Line line2 = parser.parse(1, "15:24:02.038 line2", "line2", TextColor.ANSI.DEFAULT).getParsedLine();
        Line line3 = parser.parse(2, "15:24:03.038 line3", "line3", TextColor.ANSI.DEFAULT).getParsedLine();
        Line line4 = parser.parse(3, "15:24:04.038 line4", "line4", TextColor.ANSI.DEFAULT).getParsedLine();
        Line line5 = parser.parse(4, "15:24:05.038 line5", "line5", TextColor.ANSI.DEFAULT).getParsedLine();
        Line line6 = parser.parse(5, "15:24:06.038 line6", "line6", TextColor.ANSI.DEFAULT).getParsedLine();
        Line line7 = parser.parse(6, "15:24:06.038 line7", "line7", TextColor.ANSI.DEFAULT).getParsedLine();
        DataView viewOne = new TestDataView(dispatcher, "one");
        DataView viewTwo = new TestDataView(dispatcher, "two");
        DataViewMerged merged = new DataViewMerged(Arrays.asList(viewOne, viewTwo), dispatcher, factory);
        addAndFire(dispatcher, viewOne, line2);
        addAndFire(dispatcher, viewOne, line3);
        addAndFire(dispatcher, viewOne, line4);
        addAndFire(dispatcher, viewTwo, line1);
        addAndFire(dispatcher, viewOne, line6, line7);
        addAndFire(dispatcher, viewOne, line5);
        factory.awaitJobsDone();
        Assertions.assertEquals(expectedJobCount, factory.getExecutedJobCount());
        Assertions.assertEquals(7, merged.getLineCount());
        Assertions.assertEquals(Arrays.asList(
                line1, line2, line3, line4, line5, line6, line7
        ), merged.getAllLines());
    }

    private void addAndFire(LogDispatcher dispatcher, DataView view, Line... lines) {
        dispatcher.execute(() -> {
            for (Line line : lines) {
                view.getAllLines().add(line);
            }
            view.fireUpdated();
        });

    }
}
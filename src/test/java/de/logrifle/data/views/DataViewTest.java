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

import de.logrifle.base.DirectDispatcher;
import de.logrifle.base.LogDispatcher;
import de.logrifle.base.RateLimiterFactoryTestImpl;
import de.logrifle.data.parsing.Line;
import de.logrifle.data.parsing.LineParser;
import de.logrifle.data.parsing.LineParserTimestampedTextImpl;
import de.logrifle.data.parsing.TestLinesFactory;
import de.logrifle.data.parsing.TimeStampFormat;
import de.logrifle.data.parsing.TimeStampFormats;
import de.logrifle.ui.UI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class DataViewTest {

    private final LineParser parser = new LineParserTimestampedTextImpl(
            new TimeStampFormat(TimeStampFormats.DEFAULT_TIME_MATCH_REGEX, TimeStampFormats.DEFAULT_DATE_FORMAT)
    );

    private final LogDispatcher dispatcher = new DirectDispatcher();

    @Test
    void mergedLinesShouldBeSorted() throws InterruptedException, ViewCreationFailedException {
        int jobCountMergedViewInstantiation = 1;
        int jobCountLineAddition = 6;
        int expectedJobCount = jobCountMergedViewInstantiation + jobCountLineAddition;
        RateLimiterFactoryTestImpl factory = new RateLimiterFactoryTestImpl(expectedJobCount);
        Line line1 = parser.parse(0, "15:24:01.038 line1", TestLinesFactory.TEST_SOURCE).getParsedLine();
        Line line2 = parser.parse(1, "15:24:02.038 line2", TestLinesFactory.TEST_SOURCE).getParsedLine();
        Line line3 = parser.parse(2, "15:24:03.038 line3", TestLinesFactory.TEST_SOURCE).getParsedLine();
        Line line4 = parser.parse(3, "15:24:04.038 line4", TestLinesFactory.TEST_SOURCE).getParsedLine();
        Line line5 = parser.parse(4, "15:24:05.038 line5", TestLinesFactory.TEST_SOURCE).getParsedLine();
        Line line6 = parser.parse(5, "15:24:06.038 line6", TestLinesFactory.TEST_SOURCE).getParsedLine();
        Line line7 = parser.parse(6, "15:24:06.038 line7", TestLinesFactory.TEST_SOURCE).getParsedLine();
        DataView viewOne = new TestDataView(dispatcher, "one");
        DataView viewTwo = new TestDataView(dispatcher, "two");
        DataViewMerged merged = new DataViewMerged(Arrays.asList(viewOne, viewTwo), dispatcher, factory);
        DataViewFiltered filtered = new DataViewFiltered("line[3-5]", merged, false, dispatcher, l -> false);
        dispatcher.execute(() -> merged.addListener(filtered));
        addAndFire(dispatcher, viewOne, line2);
        addAndFire(dispatcher, viewOne, line3);
        addAndFire(dispatcher, viewTwo, line1);
        addAndFire(dispatcher, viewOne, line4);
        addAndFire(dispatcher, viewOne, line5);
        addAndFire(dispatcher, viewOne, line6, line7);
        factory.awaitJobsDone();
        Assertions.assertEquals(expectedJobCount, factory.getExecutedJobCount());
        Assertions.assertEquals(7, merged.getLineCount());
        Assertions.assertEquals(3, filtered.getLineCount());
        UI.setTestMode();
        Assertions.assertEquals(Arrays.asList(
                line1, line2, line3, line4, line5, line6, line7
        ), merged.getAllLines());
        Assertions.assertEquals(Arrays.asList(
                line3, line4, line5
        ), filtered.getAllLines());
    }

    @Test
    void mergedLinesShouldMaintainOrderFromSources() throws InterruptedException, ViewCreationFailedException {
        int jobCountMergedViewInstantiation = 1;
        int jobCountLineAddition = 6;
        int expectedJobCount = jobCountMergedViewInstantiation + jobCountLineAddition;
        RateLimiterFactoryTestImpl factory = new RateLimiterFactoryTestImpl(expectedJobCount);
        Line line1 = parser.parse(0, "17:24:01.038 line1", TestLinesFactory.TEST_SOURCE).getParsedLine();
        Line line2 = parser.parse(1, "15:24:02.038 line2", TestLinesFactory.TEST_SOURCE).getParsedLine();
        Line line3 = parser.parse(2, "15:24:03.038 line3", TestLinesFactory.TEST_SOURCE).getParsedLine();
        Line line4 = parser.parse(3, "16:24:04.038 line4", TestLinesFactory.TEST_SOURCE).getParsedLine();
        Line line5 = parser.parse(4, "15:24:05.038 line5", TestLinesFactory.TEST_SOURCE).getParsedLine();
        Line line6 = parser.parse(5, "15:24:06.038 line6", TestLinesFactory.TEST_SOURCE).getParsedLine();
        Line line7 = parser.parse(6, "15:24:06.038 line7", TestLinesFactory.TEST_SOURCE).getParsedLine();
        DataView viewOne = new TestDataView(dispatcher, "one");
        DataView viewTwo = new TestDataView(dispatcher, "two");
        DataViewMerged merged = new DataViewMerged(Arrays.asList(viewOne, viewTwo), dispatcher, factory);
        DataViewFiltered filtered = new DataViewFiltered("line[3-5]", merged, false, dispatcher, l -> false);
        dispatcher.execute(() -> merged.addListener(filtered));
        addAndFire(dispatcher, viewOne, line2);
        addAndFire(dispatcher, viewOne, line3);
        addAndFire(dispatcher, viewTwo, line1);
        addAndFire(dispatcher, viewOne, line6, line7);
        addAndFire(dispatcher, viewTwo, line5);
        addAndFire(dispatcher, viewOne, line4);
        factory.awaitJobsDone();
        Assertions.assertEquals(expectedJobCount, factory.getExecutedJobCount());
        Assertions.assertEquals(7, merged.getLineCount());
        Assertions.assertEquals(3, filtered.getLineCount());
        UI.setTestMode();
        Assertions.assertEquals(Arrays.asList(
                line1, line2, line3, line4, line5, line6, line7
        ), merged.getAllLines());
        Assertions.assertEquals(Arrays.asList(
                line3, line4, line5
        ), filtered.getAllLines());
    }

    @Test
    void invisibleLinesShouldNotBeReturned() throws ViewCreationFailedException {
        int jobCountMergedViewInstantiation = 1;
        int jobCountLineAddition = 2;
        int expectedJobCount = jobCountMergedViewInstantiation + jobCountLineAddition;
        RateLimiterFactoryTestImpl factory = new RateLimiterFactoryTestImpl(expectedJobCount);
        DataView viewOne = new TestDataView(dispatcher, "one");
        DataView viewTwo = new TestDataView(dispatcher, "two");
        Line line1 = parser.parse(0, "15:24:01.038 line1", viewOne).getParsedLine();
        Line line2 = parser.parse(1, "15:24:02.038 line2", viewTwo).getParsedLine();
        Line line3 = parser.parse(2, "15:24:03.038 line3", viewOne).getParsedLine();
        Line line4 = parser.parse(3, "15:24:04.038 line4", viewTwo).getParsedLine();
        Line line5 = parser.parse(4, "15:24:05.038 line5", viewTwo).getParsedLine();
        Line line6 = parser.parse(5, "15:24:06.038 line6", viewOne).getParsedLine();
        Line line7 = parser.parse(6, "15:24:06.038 line7", viewOne).getParsedLine();
        DataViewMerged merged = new DataViewMerged(Arrays.asList(viewOne, viewTwo), dispatcher, factory);
        DataViewFiltered filtered = new DataViewFiltered("line[3-5]", merged, false, dispatcher, l -> false);
        dispatcher.execute(() -> merged.addListener(filtered));
        addAndFire(dispatcher, viewOne, line1, line3, line6, line7);
        addAndFire(dispatcher, viewTwo, line2, line4, line5);
        Assertions.assertEquals(expectedJobCount, factory.getExecutedJobCount());
        Assertions.assertEquals(7, merged.getLineCount());
        Assertions.assertEquals(3, filtered.getLineCount());
        UI.setTestMode();
        Assertions.assertEquals(Arrays.asList(
                line1, line2, line3, line4, line5, line6, line7
        ), merged.getAllLines());
        Assertions.assertEquals(Arrays.asList(
                line3, line4, line5
        ), filtered.getAllLines());

        merged.toggleView(1);
        Assertions.assertEquals(4, merged.getLineCount());
        Assertions.assertEquals(1, filtered.getLineCount());
    }

    @Test
    void testForcedBookmarksDisplay() throws ViewCreationFailedException {
        AtomicBoolean forceDisplay = new AtomicBoolean(true);
        int jobCountMergedViewInstantiation = 1;
        int jobCountLineAddition = 2;
        int expectedJobCount = jobCountMergedViewInstantiation + jobCountLineAddition;
        RateLimiterFactoryTestImpl factory = new RateLimiterFactoryTestImpl(expectedJobCount);
        DataView viewOne = new TestDataView(dispatcher, "one");
        DataView viewTwo = new TestDataView(dispatcher, "two");
        Line line1 = parser.parse(0, "15:24:01.038 line1", viewOne).getParsedLine();
        Line line2 = parser.parse(1, "15:24:02.038 line2", viewTwo).getParsedLine();
        Line line3 = parser.parse(2, "15:24:03.038 line3", viewOne).getParsedLine();
        Line line4 = parser.parse(3, "15:24:04.038 line4", viewTwo).getParsedLine();
        Line line5 = parser.parse(4, "15:24:05.038 line5", viewTwo).getParsedLine();
        Line line6 = parser.parse(5, "15:24:06.038 line6", viewOne).getParsedLine();
        Line line7 = parser.parse(6, "15:24:06.038 line7", viewOne).getParsedLine();
        List<Line> bookmarked = Arrays.asList(line3, line7);
        DataViewMerged merged = new DataViewMerged(Arrays.asList(viewOne, viewTwo), dispatcher, factory);
        DataViewFiltered filtered = new DataViewFiltered("line[3-5]", merged, false, dispatcher, l -> forceDisplay.get() && bookmarked.contains(l));
        DataViewFiltered reFiltered = new DataViewFiltered("line7", filtered, false, dispatcher, l -> forceDisplay.get() && bookmarked.contains(l));

        dispatcher.execute(() -> merged.addListener(filtered));
        dispatcher.execute(() -> merged.addListener(reFiltered));
        addAndFire(dispatcher, viewOne, line1, line3, line6, line7);
        addAndFire(dispatcher, viewTwo, line2, line4, line5);
        Assertions.assertEquals(expectedJobCount, factory.getExecutedJobCount());
        Assertions.assertEquals(Arrays.asList(line3, line4, line5, line7), filtered.getAllLines());
        Assertions.assertEquals(bookmarked, reFiltered.getAllLines());
        UI.setTestMode();

        forceDisplay.set(false);
        merged.onLineVisibilityStateInvalidated(bookmarked, merged);
        Assertions.assertEquals(Arrays.asList(line3, line4, line5), filtered.getAllLines());
        Assertions.assertEquals(0, reFiltered.getLineCount());

        forceDisplay.set(true);
        merged.onLineVisibilityStateInvalidated(bookmarked, merged);
        Assertions.assertEquals(Arrays.asList(line3, line4, line5, line7), filtered.getAllLines());
        Assertions.assertEquals(bookmarked, reFiltered.getAllLines());
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
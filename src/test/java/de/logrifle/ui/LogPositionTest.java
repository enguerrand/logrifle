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

package de.logrifle.ui;

import de.logrifle.base.TestLogDispatcher;
import de.logrifle.data.parsing.TestLinesFactory;
import de.logrifle.data.views.DataView;
import de.logrifle.data.views.DataViewFiltered;
import de.logrifle.data.views.TestDataView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LogPositionTest {

    @ParameterizedTest
    @CsvSource({
            "0,0,10,0,0",
            "-1,0,10,0,0",
            "9,0,10,0,9",
            "10,0,10,0,9",
            "9,1,10,0,9",
            "5,5,10,4,5",
            "-1,1,1,0,0",
    })
    void ensureValid(int currentTopIndex, int currentFocusOffset, int totalLineCount, int expectedFocusOffset, int expectedTopIndex) {
        LogPosition current = new LogPosition(currentTopIndex, currentFocusOffset);
        LogPosition logPosition = current.ensureValid(totalLineCount);
        int focusOffset = logPosition.getFocusOffset();
        int topIndex = logPosition.getTopIndex();
        Assertions.assertEquals(expectedFocusOffset, focusOffset, "Wrong focus offset");
        Assertions.assertEquals(expectedTopIndex, topIndex, "Wrong top index");
    }

    @ParameterizedTest
    @CsvSource({
            "100,50,40,1000,111,39",
            "100,50,40,151,111,39",
            "100,50,40,150,110,39",
            "100,50,40,139,100,38",
            "10,-1,40,1000,9,0",
            "10,-10,40,1000,0,0",
            "10,-11,40,1000,0,0",
            "10,-20,40,1000,0,0",
    })
    void scrollIfRequiredByFocus(int currentTopIndex, int currentFocusOffset, int visibleRowCount, int totalLineCount, int expectedTopIndex, int expectedFocusOffset) {
        LogPosition current = new LogPosition(currentTopIndex, currentFocusOffset);
        LogPosition logPosition = current.scrollIfRequiredByFocus(visibleRowCount, totalLineCount);
        int focusOffset = logPosition.getFocusOffset();
        int topIndex = logPosition.getTopIndex();
        Assertions.assertEquals(expectedTopIndex, topIndex, "Wrong top index");
        Assertions.assertEquals(expectedFocusOffset, focusOffset, "Wrong focus offset");
    }

    @ParameterizedTest
    @CsvSource({
            "3,1,null,full,3,1",
            "1,0,full,filtered,0,0",
            "3,0,full,filtered,0,0",
            "3,1,full,filtered,0,1",
            "4,1,full,filtered,1,1",
            "4,0,full,filtered,1,0",
            "5,0,full,filtered,2,0",
            "5,1,full,filtered,1,1",
            "6,0,full,filtered,2,0",
            "6,1,full,filtered,1,1",
            "0,0,filtered,full,3,0",
            "1,0,filtered,full,4,0",
            "1,1,filtered,full,4,1",
            "2,0,filtered,full,5,0",
    })
    void transferIfNeeded(int currentTopIndex, int currentFocusOffset, String fromView, String toView, int expectedTopIndex, int expectedFocusOffset) throws InterruptedException {
        TestLogDispatcher dispatcher = new TestLogDispatcher();
        DataView full = new TestDataView(dispatcher, "foobar", TestLinesFactory.buildTestLines());
        DataView filtered = new DataViewFiltered("line content [3-5]", full, false, dispatcher);
        dispatcher.execute(() -> filtered.onUpdated(full));
        dispatcher.awaitJobsDone();
        DataView from = select(full, filtered, fromView);
        DataView to = select(full, filtered, toView);
        LogPosition current = new LogPosition(currentTopIndex, currentFocusOffset);
        LogPosition logPosition = current.transferIfNeeded(from, to);
        int focusOffset = logPosition.getFocusOffset();
        int topIndex = logPosition.getTopIndex();
        Assertions.assertEquals(expectedTopIndex, topIndex, "Wrong top index");
        Assertions.assertEquals(expectedFocusOffset, focusOffset, "Wrong focus offset");

    }

    private static DataView select(DataView full, DataView filtered, String viewName) {
        switch(viewName) {
            case("filtered"): return filtered;
            case("full"): return full;
            default: return null;
        }
    }

    @ParameterizedTest
    @CsvSource({
            "10,0,11,0,true",
            "10,2,11,0,true",
            "10,1,10,0,false",
            "10,0,10,1,true",
            "10,0,10,0,false",
    })
    void isBefore(int currentTopIndex, int currentFocusOffset, int otherTopIndex, int otherFocusOffset, boolean expectedBefore) {
        LogPosition current = new LogPosition(currentTopIndex, currentFocusOffset);
        LogPosition other = new LogPosition(otherTopIndex, otherFocusOffset);
        boolean before = current.isBefore(other);
        Assertions.assertEquals(expectedBefore, before);
    }
}
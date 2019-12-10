/*
 *  Copyright 2019, Enguerrand de Rochefort
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

package de.rochefort.logrifle.data.views;

import de.rochefort.logrifle.data.parsing.Line;

import java.util.Collections;
import java.util.List;

public interface DataView {
    default List<Line> getLines(int topIndex, int maxCount) {
        List<Line> snapshot = getAllLines();
        if (snapshot == null) {
            return Collections.emptyList();
        }
        int topIndexCorrected = Math.max(0, Math.min(topIndex, snapshot.size()-1));
        if (snapshot.size() <= topIndexCorrected || topIndex < 0) {
            return Collections.emptyList();
        } else if (snapshot.size() <= topIndexCorrected + maxCount) {
            return snapshot.subList(topIndexCorrected, snapshot.size());
        } else {
            return snapshot.subList(topIndexCorrected, topIndexCorrected + maxCount);
        }
    }
    int getLineCount();
    List<Line> getAllLines();
    String getTitle();
}

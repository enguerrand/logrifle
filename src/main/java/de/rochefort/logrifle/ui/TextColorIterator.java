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

package de.rochefort.logrifle.ui;

import com.googlecode.lanterna.TextColor;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TextColorIterator implements Iterator<TextColor> {
    private final List<TextColor> available;
    private final AtomicInteger index = new AtomicInteger(0);

    public TextColorIterator(List<TextColor> available) {
        this.available = available;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public TextColor next() {
        int nextIndex = index.getAndUpdate(prev -> {
            if (prev >= available.size() - 1) {
                return 0;
            } else {
                return prev + 1;
            }
        });
        return available.get(nextIndex);
    }
}

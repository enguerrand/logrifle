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

public enum LineLabelDisplayMode {
    NONE(0, 0, "none"),
    SHORT(1, 1, "short (label truncated to one character)"),
    LONG(2, Integer.MAX_VALUE, "full");

    private final int index;
    private final int maxLength;
    private final String description;

    LineLabelDisplayMode(int index, int maxLength, String description) {
        this.index = index;
        this.maxLength = maxLength;
        this.description = description;
    }

    public LineLabelDisplayMode next() {
        int nextIndex = index + 1;
        if (nextIndex >= values().length) {
            nextIndex = 0;
        }
        return fromIndex(nextIndex);
    }

    private LineLabelDisplayMode fromIndex(int index) {
        for (LineLabelDisplayMode mode : values()) {
            if (mode.index == index) {
                return mode;
            }
        }
        throw new IndexOutOfBoundsException("Invalid index "+index);
    }

    public int getMaxLength() {
        return maxLength;
    }

    public String getDescription() {
        return description;
    }
}

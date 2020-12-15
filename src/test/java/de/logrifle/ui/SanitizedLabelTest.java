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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SanitizedLabelTest {
    private static final char DIRTY_CHARACTER = 127;
    private static final String CLEAN = new String(new char[]{42, 100});
    private static final String DIRTY_1 = new String(new char[]{127, 42, 100});
    private static final String DIRTY_2 = new String(new char[]{42, DIRTY_CHARACTER, 100});
    private static final String DIRTY_3 = new String(new char[]{42, 100, 127});
    @Test
    void constructor() {
        SanitizedLabel sanitizedLabel = new SanitizedLabel(DIRTY_1);
        Assertions.assertEquals(CLEAN, sanitizedLabel.getText());
    }

    @Test
    void setLines() {
        SanitizedLabel sanitizedLabel = new SanitizedLabel("");
        sanitizedLabel.setLines(new String[]{DIRTY_1, DIRTY_2, DIRTY_3});
        StringBuilder bob = new StringBuilder(CLEAN);
        for(int i = 1; i < 3; ++i) {
            bob.append("\n").append(CLEAN);
        }
        String expected = bob.toString();
        Assertions.assertEquals(expected, sanitizedLabel.getText());
    }

    @Test
    void setText() {
        SanitizedLabel sanitizedLabel = new SanitizedLabel("");
        sanitizedLabel.setText(DIRTY_2);
        Assertions.assertEquals(CLEAN, sanitizedLabel.getText());
    }
}
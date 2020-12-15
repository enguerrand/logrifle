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

import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.gui2.Label;

public class SanitizedLabel extends Label {
    public SanitizedLabel(String text) {
        super(sanitize(text));
    }

    @Override
    protected void setLines(String[] lines) {
        String[] sanitized = new String[lines.length];
        for (int i = 0; i < lines.length; i++) {
            sanitized[i] = sanitize(lines[i]);
        }
        super.setLines(sanitized);
    }

    @Override
    public synchronized void setText(String text) {
        super.setText(sanitize(text));
    }

    private static String sanitize(String input) {
        char[] chars = input.toCharArray();
        char[] buffer = new char[chars.length];
        int bufferIndex = 0;
        for (char c : chars) {
            if (isAllowedCharacter(c)) {
                buffer[bufferIndex++] = c;
            }
        }
        char[] sanitized = new char[bufferIndex];
        System.arraycopy(buffer, 0, sanitized, 0, bufferIndex);
        return new String(sanitized);
    }

    /**
     * this checks the validity in the same way as {@link com.googlecode.lanterna.TextCharacter}
     */
    private static boolean isAllowedCharacter(char c) {
        return c == '\t' || !TerminalTextUtils.isControlCharacter(c);
    }
}

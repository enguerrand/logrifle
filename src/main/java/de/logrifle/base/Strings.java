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

package de.logrifle.base;

public class Strings {
    public static String pad(String s, int length, boolean beginning) {
        return pad(s, length, " ", beginning);
    }

    public static String pad(String s, int length, String paddingContent, boolean beginning) {
        StringBuilder sb = new StringBuilder();
        if (!beginning) {
            sb.append(s);
        }
        for (int i = s.length(); i<length ; i++) {
            sb.append(paddingContent);
        }
        if (beginning) {
            sb.append(s);
        }
        return sb.toString();
    }
}
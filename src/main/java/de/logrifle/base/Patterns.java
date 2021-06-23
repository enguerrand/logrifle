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

import de.logrifle.data.views.UserInputProcessingFailedException;

import java.util.regex.Pattern;

public class Patterns {
    private static final String CASE_INSENSITIVE_PREFIX = "(?i)";
    private Patterns() {
        throw new IllegalStateException("Do not instantiate!");
    }
    public static String makeCaseInsensitive(String regex) {
        return CASE_INSENSITIVE_PREFIX + regex;
    }
    public static Pattern compilePatternChecked(String regex) throws UserInputProcessingFailedException {
        try {
            return Pattern.compile(regex);
        } catch (RuntimeException e) {
            throw UserInputProcessingFailedException.from(e);
        }
    }
}

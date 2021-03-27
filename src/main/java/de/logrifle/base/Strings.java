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

import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {
    private static final Pattern WHITE_SPACE_AT_BEGINNING = Pattern.compile("^\\s.*");
    private static final Pattern WHITE_SPACE_AT_END = Pattern.compile("\\s.*");

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

    public static String truncateString(String s, int maxLength) {
        if (s.length() > maxLength) {
            String truncationPlaceholder = "...";
            int truncationPlaceholderLength = Math.min(truncationPlaceholder.length(), maxLength);
            s = s.substring(0, Math.max(0, maxLength - 3)) + truncationPlaceholder.substring(0, truncationPlaceholderLength);
        }
        return s;
    }

    public static boolean isBlank(@Nullable String s) {
        if (s == null) {
            return true;
        }
        return (s.matches("\\s*"));
    }

    public static String expandPathPlaceHolders(String input) {
        return input.replaceAll("~", System.getProperty("user.home"));
    }

    public static String trimStart(String input) {
        String result = input;
        while (result.length() > 0 && WHITE_SPACE_AT_BEGINNING.matcher(result).matches()) {
            result = result.substring(1);
        }
        return result;
    }

    public static String[] tokenizeAt(String text, int index) {
        String first = text.substring(0, index);
        String second = text.substring(index);
        return new String[] { first, second };
    }

    public static int findFirstWordStartOrEnd(String text) {
        String trimmed = trimStart(text);
        int trimOffset = text.length() - trimmed.length();
        if (trimOffset != 0) {
            return trimOffset;
        }
        Matcher matcher = Pattern.compile("\\s(\\S)").matcher(trimmed);
        if (matcher.find()) {
            return matcher.start(1);
        } else {
            return text.length();
        }
    }

    public static int findLastWordStartOrStart(String text) {
        String reversed = new StringBuilder(text).reverse().toString();
        String trimmed = trimStart(reversed);
        int trimOffset = reversed.length() - trimmed.length();
        int index;
        Matcher matcher = Pattern.compile("\\s").matcher(trimmed);
        if (matcher.find()) {
            index = matcher.start() + trimOffset;
        } else {
            index = reversed.length();
        }
        return reversed.length() - index;
    }

    public static int findNextWordStartOrEnd(String text, int currentIndex) {
        String[] strings = tokenizeAt(text, currentIndex);
        return findFirstWordStartOrEnd(strings[1]) + currentIndex;
    }

    public static int findPreviousWordStartOrStrt(String text, int currentIndex) {
        String[] strings = tokenizeAt(text, currentIndex);
        return findLastWordStartOrStart(strings[0]);
    }
}
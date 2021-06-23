/*
 *  Copyright 2021, Enguerrand de Rochefort
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

import java.util.regex.PatternSyntaxException;

public class UserInputProcessingFailedException extends Exception {
    public UserInputProcessingFailedException(String message) {
        super(message);
    }

    public static UserInputProcessingFailedException from(Exception cause) {
        String message;
        if (cause instanceof PatternSyntaxException) {
            PatternSyntaxException syntaxException = (PatternSyntaxException) cause;
            message = "Invalid regular expression: " + syntaxException.getDescription() + " near index " + syntaxException.getIndex();
        } else if (cause.getLocalizedMessage() != null) {
            message = cause.getLocalizedMessage();
        } else if (cause.getMessage() != null) {
            message = cause.getMessage();
        } else {
            message = cause.getClass().getSimpleName();
        }
        return new UserInputProcessingFailedException(message);
    }
}

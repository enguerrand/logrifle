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

package de.logrifle.data.io;

import java.io.IOException;

public class FormatDetectionFailedException extends IOException {
    public FormatDetectionFailedException() {
    }

    public FormatDetectionFailedException(String s) {
        super(s);
    }

    public FormatDetectionFailedException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public FormatDetectionFailedException(Throwable throwable) {
        super(throwable);
    }
}

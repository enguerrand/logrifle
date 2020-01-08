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

package de.logrifle.ui.cmd;

import de.logrifle.base.Patterns;

import java.util.Objects;

public class Query {
    private final String searchTerm;
    private final boolean backwards;

    public Query(String searchTerm, boolean backwards, boolean caseInsensitive) {
        this.searchTerm = caseInsensitive ? Patterns.makeCaseInsensitive(searchTerm) : searchTerm;
        this.backwards = backwards;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public boolean isBackwards() {
        return backwards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Query query = (Query) o;
        return backwards == query.backwards &&
                searchTerm.equals(query.searchTerm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchTerm, backwards);
    }
}

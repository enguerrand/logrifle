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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandAutoCompleterTest {
    private CommandAutoCompleter commandAutoCompleter;

    @BeforeEach
    void setUp() {
        commandAutoCompleter = new CommandAutoCompleter(Arrays.asList(
            "foo",
            "foobar",
            "foobas",
            "bar"
        ));
    }

    @ParameterizedTest
    @CsvSource({
            ":f,foo;foobar;foobas",
            ":foo,foo;foobar;foobas",
            ":foob,foobar;foobas",
            ":fooba,foobar;foobas",
            ":foobar,foobar",
            ":bar,bar",
            ":bar arg,",
            ":zzz,",
    })
    void getMatching(String currentInput, String matchingCsv) {
        List<String> expectedMatches = matchingCsv == null ? Collections.emptyList() : Arrays.asList(matchingCsv.split(";"));
        List<String> matches = commandAutoCompleter.getMatching(currentInput);
        Assertions.assertEquals(expectedMatches, matches);
    }

    @ParameterizedTest
    @CsvSource({
            ":f,:foo",
            ":fo,:foo",
            ":foo,:foo",
            ":foob,:fooba",
            ":fooba,:fooba",
            ":foobar,:foobar",
            ":foobar arg, :foobar arg",
            ":wups,:wups",
            ":b,:bar",
            ":ba,:bar",
            ":bar,:bar",
            ":zzz,:zzz",
            "zzz,zzz",
    })
    void complete(String currentInput, String expectedCompletion) {
        String completed = commandAutoCompleter.complete(currentInput);
        Assertions.assertEquals(expectedCompletion, completed, "Completion failed for current input "+currentInput);
    }
}
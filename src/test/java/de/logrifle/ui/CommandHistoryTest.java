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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CommandHistoryTest {

    private CommandHistory history;

    @BeforeEach
    void setUp() {
        history = new CommandHistory();
    }

    @Test
    void emptyHistory() {
        AtomicBoolean called = new AtomicBoolean(false);
        history.back("blubb", s -> called.set(true));
        history.forward(s -> called.set(true));
        Assertions.assertFalse(called.get(), "handler should not be called when history is empty");
    }

    @Test
    void reset() {
        history.append("foo");
        history.append("bar");
        history.back("blubb", s -> {});
        history.reset();
        AtomicBoolean called = new AtomicBoolean(false);
        history.forward(s -> called.set(true));
        AtomicReference<String> ref = new AtomicReference<>();
        Assertions.assertFalse(called.get(), "handler should not be called after reset");
        history.back("blubb", ref::set);
        Assertions.assertEquals("bar", ref.get(), "previous command after reset");
    }

    @Test
    void back() {
        history.append("foo");
        history.append("bar");
        AtomicReference<String> ref = new AtomicReference<>();
        history.back("blubb", ref::set);
        Assertions.assertEquals("bar", ref.get(), "previous command");
        history.back("bar", ref::set);
        Assertions.assertEquals("foo", ref.get(), "command before previous command");
    }

    @Test
    void forward() {
        history.append("foo");
        history.append("bar");
        history.back("blubb", s -> {});
        history.back("bar", s -> {});
        AtomicReference<String> ref = new AtomicReference<>();
        history.forward(ref::set);
        Assertions.assertEquals("bar", ref.get(), "previous command");
        history.forward(ref::set);
        Assertions.assertEquals("blubb", ref.get(), "uncommitted input");
    }
}
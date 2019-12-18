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

package de.rochefort.logrifle.ui.cmd;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.util.HashMap;
import java.util.Map;

public class KeyMapFactory {
    private Map<KeyStroke, String> keyMap = new HashMap<>();

    public KeyMapFactory() {
        keyMap.put(new KeyStroke(KeyType.ArrowUp), "move-focus -1");
        keyMap.put(new KeyStroke(KeyType.ArrowDown), "move-focus 1");
        keyMap.put(new KeyStroke(KeyType.ArrowLeft), "hscroll -5");
        keyMap.put(new KeyStroke(KeyType.ArrowRight), "hscroll 5");
        keyMap.put(new KeyStroke(KeyType.F5), "refresh");
        keyMap.put(new KeyStroke('y', true, false), "scroll -1");
        keyMap.put(new KeyStroke('e', true, false), "scroll 1");
        keyMap.put(new KeyStroke('l', false, false), "toggle-line-labels");
        keyMap.put(new KeyStroke('n', false, false), "find-again");
        keyMap.put(new KeyStroke('N', false, false, true), "find-again-backwards");
        keyMap.put(new KeyStroke('q', false, false), "quit");
        keyMap.put(new KeyStroke(KeyType.ArrowUp, false, true), "filter-view-up");
        keyMap.put(new KeyStroke(KeyType.ArrowDown, false, true), "filter-view-down");
        keyMap.put(new KeyStroke(KeyType.Delete, false, false), "delete-filter");
    }

    public Map<KeyStroke, String> get() {
        return keyMap;
    }
}
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

package de.logrifle.ui.cmd;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import de.logrifle.base.Strings;

public class KeyBind {
    private final KeyStroke keyStroke;
    private final String mapping;

    public KeyBind(KeyStroke keyStroke, String mapping) {
        this.keyStroke = keyStroke;
        this.mapping = mapping;
    }

    KeyStroke getKeyStroke() {
        return keyStroke;
    }

    int getModifiersCount() {
        int count = 0;
        if (keyStroke.isCtrlDown()) {
            ++count;
        }
        if (keyStroke.isAltDown()) {
            ++count;
        }
        if (keyStroke.getKeyType() != KeyType.Character && keyStroke.isShiftDown()) {
            ++count;
        }
        return count;
    }

    String render(int keyLength, String prefix) {
        return Strings.pad(renderKeyStroke(), keyLength, false) + " => " + prefix + mapping;
    }

    String renderKeyStroke() {
        StringBuilder sb = new StringBuilder();
        if (keyStroke.isCtrlDown()) {
            sb.append("CTRL+");
        }
        if (keyStroke.isAltDown()) {
            sb.append("ALT+");
        }
        if (keyStroke.getKeyType() == KeyType.Character) {
            sb.append(keyStroke.getCharacter());
        } else {
            if (keyStroke.isShiftDown()) {
                sb.append("SHIFT+");
            }
            sb.append(keyStroke.getKeyType().name());
        }
        return sb.toString();
    }
}

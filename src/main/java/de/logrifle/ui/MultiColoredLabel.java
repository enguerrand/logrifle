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

package de.logrifle.ui;

import com.googlecode.lanterna.gui2.AbstractComponent;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;

import java.util.Collection;

public class MultiColoredLabel extends Panel {
    private final Panel panel;

    public MultiColoredLabel(Collection<ColoredString> text) {
        LinearLayout layout = new LinearLayout(Direction.HORIZONTAL);
        layout.setSpacing(0);
        panel = new Panel(layout);
        for (ColoredString coloredString : text) {
            Label label = new Label(coloredString.getText());
            label.setLabelWidth(null); // prevents word wrap
            coloredString.getBgColor().ifPresent(label::setBackgroundColor);
            coloredString.getFgColor().ifPresent(label::setForegroundColor);
            coloredString.getStyles().forEach(label::addStyle);
            panel.addComponent(label);
        }
    }

    public AbstractComponent<?> asComponent() {
        return panel;
    }
}

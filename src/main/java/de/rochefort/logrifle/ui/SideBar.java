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

package de.rochefort.logrifle.ui;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import de.rochefort.logrifle.data.views.ViewsTree;

import java.util.ArrayList;
import java.util.List;

class SideBar {
    private final Panel panel;
    private final Panel viewsPanel;
    private final ViewsTree viewsTree;

    SideBar(ViewsTree viewsTree) {
        this.viewsTree = viewsTree;
        this.panel = new Panel(new BorderLayout());
        this.viewsPanel = new Panel(new GridLayout(1));
        this.panel.addComponent(this.viewsPanel);
        this.viewsPanel.setLayoutData(BorderLayout.Location.TOP);

    }

    Panel getPanel() {
        return panel;
    }

    int update() {
        UI.checkGuiThreadOrThrow();
        this.viewsPanel.removeAllComponents();
        final List<Label> labels = new ArrayList<>();
        this.viewsTree.walk((node, recursionDepth, focused) -> {
            String text = buildText(node.getTitle(), recursionDepth);
            Label label = new Label(text);
            labels.add(label);
            viewsPanel.addComponent(label);
            if (focused) {
                label.setForegroundColor(TextColor.ANSI.YELLOW);
                label.addStyle(SGR.BOLD);
            }
        });
        int maxLength = 0;
        for (Label label : labels) {
            maxLength = Math.max(maxLength, label.getText().length());
        }
        return maxLength;
    }

    private String buildText(String title, int recursionDepth) {
        title = truncateString(title);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recursionDepth; i++) {
            sb.append("  ");
        }
        if (recursionDepth > 0) {
            sb.append("-> ");
        }
        sb.append(title);
        return sb.toString();
    }

    private String truncateString(String s) {
        if (s.length() > 30) {
            s = s.substring(0, 28) + "...";
        }
        return s;
    }
}

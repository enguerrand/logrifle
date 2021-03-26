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

package de.logrifle.data.views;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ViewsTreeNode {
    private final int navIndex;
    public static final Map<Integer, ViewsTreeNode> NAV_INDEX_LOOKUP = new ConcurrentHashMap<>();
    @Nullable
    private final ViewsTreeNode parent;
    private final List<ViewsTreeNode> children;
    private final DataView dataView;

    public ViewsTreeNode(@Nullable ViewsTreeNode parent, DataView dataView) {
        this.navIndex = computeNavIndex(this);
        this.parent = parent;
        this.dataView = dataView;
        this.children = new ArrayList<>();
    }

    private static int computeNavIndex(ViewsTreeNode node) {
        boolean found = false;
        int i = 0;
        while (!found) {
            i++;
            ViewsTreeNode present = NAV_INDEX_LOOKUP.putIfAbsent(i, node);
            found = present == null;
        }
        return i;
    }

    public int getNavIndex() {
        return navIndex;
    }

    void addChild(ViewsTreeNode child) {
        this.children.add(child);
    }

    void removeChild(ViewsTreeNode child) {
        this.children.remove(child);
    }

    List<ViewsTreeNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public ViewsTreeNode getParent() {
        return parent;
    }

    public String getTitle(){
        return this.dataView.getTitle();
    }

    public DataView getDataView() {
        return dataView;
    }

    void destroy() {
        ViewsTreeNode.NAV_INDEX_LOOKUP.remove(getNavIndex());
        for (ViewsTreeNode child : children) {
            child.destroy();
        }
    }
}

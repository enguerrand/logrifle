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

package de.rochefort.logrifle.data.views;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewsTreeNode {
    @Nullable
    private final ViewsTreeNode parent;
    private final List<ViewsTreeNode> children;
    private final DataView dataView;

    public ViewsTreeNode(@Nullable ViewsTreeNode parent, DataView dataView) {
        this.parent = parent;
        this.dataView = dataView;
        this.children = new ArrayList<>();
    }

    void addChild(ViewsTreeNode child) {
        this.children.add(child);
    }

    public void removeChild(ViewsTreeNode child) {
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
}

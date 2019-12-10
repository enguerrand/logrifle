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

import java.util.List;

public class ViewsTree {
    private final ViewsTreeNode rootNode;
    private ViewsTreeNode focusedNode;

    public ViewsTree(DataView rootView) {
        this.rootNode = new ViewsTreeNode(null, rootView);
        this.focusedNode = rootNode;
    }

    public void addNodeAndSetFocus(ViewsTreeNode parent, ViewsTreeNode child) {
        parent.addChild(child);
        this.focusedNode = child;
    }

    public void removeNode(ViewsTreeNode node) {
        ViewsTreeNode parent = node.getParent();
        if (parent == null) {
            //TODO
            return;
        }
        moveFocusUp();
        parent.removeChild(node);
    }

    public ViewsTreeNode getFocusedNode() {
        return focusedNode;
    }

    public boolean moveFocusUp() {
        ViewsTreeNode parent = focusedNode.getParent();
        if (parent == null) {
            return false;
        }
        List<ViewsTreeNode> sameLevel = parent.getChildren();
        int index = sameLevel.indexOf(focusedNode);
        if (index > 0) {
            focusedNode = sameLevel.get(index -1);
        } else {
            focusedNode = parent;
        }
        return true;
    }

    public boolean moveFocusDown() {
        ViewsTreeNode current = this.focusedNode;
        List<ViewsTreeNode> children = current.getChildren();
        if (!children.isEmpty()) {
            focusedNode = children.get(0);
            return true;
        }
        ViewsTreeNode parent = current.getParent();
        while (parent != null) {
            List<ViewsTreeNode> sameLevel = parent.getChildren();
            int index = sameLevel.indexOf(current);
            if (sameLevel.size() > index + 1) {
                this.focusedNode = sameLevel.get(index + 1);
                return true;
            }
            current = parent;
            parent = current.getParent();
        }
        return false;
    }
}

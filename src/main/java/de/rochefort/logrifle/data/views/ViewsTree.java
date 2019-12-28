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

import de.rochefort.logrifle.ui.cmd.ExecutionResult;

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

    public ExecutionResult removeNode(ViewsTreeNode node) {
        ViewsTreeNode parent = node.getParent();
        if (parent == null) {
            return new ExecutionResult(false, "Cannot delete the root view");
        }
        moveFocusPrev();
        parent.removeChild(node);
        node.destroy();
        return new ExecutionResult(true);
    }

    public ViewsTreeNode getFocusedNode() {
        return focusedNode;
    }

    public boolean moveFocusParent() {
        ViewsTreeNode parent = focusedNode.getParent();
        if (parent == null) {
            return false;
        } else {
            focusedNode = parent;
            return true;
        }
    }

    public boolean moveFocusFirstChild() {
        ViewsTreeNode current = this.focusedNode;
        List<ViewsTreeNode> children = current.getChildren();
        if (children.isEmpty()) {
            return false;
        } else {
            focusedNode = children.get(0);
            return true;
        }
    }

    public boolean moveFocusPrev() {
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

    public boolean moveFocusNext() {
        ViewsTreeNode current = this.focusedNode;
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

    public boolean moveFocusTo(int navIndex) {
        ViewsTreeNode viewsTreeNode = ViewsTreeNode.NAV_INDEX_LOOKUP.get(navIndex);
        if (viewsTreeNode == null) {
            return false;
        } else {
            this.focusedNode = viewsTreeNode;
            return true;
        }
    }

    public void walk(Walker walker){
        walkImpl(walker, this.rootNode, 0);
    }

    private void walkImpl(Walker walker, ViewsTreeNode currentNode, int depth) {
        walker.handleNode(currentNode, depth, currentNode == focusedNode);
        for (ViewsTreeNode child : currentNode.getChildren()) {
            walkImpl(walker, child, depth + 1);
        }
    }

    public interface Walker {
        void handleNode(ViewsTreeNode node, int recursionDepth, boolean focused);
    }
}

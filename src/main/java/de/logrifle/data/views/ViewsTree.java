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

import de.logrifle.data.bookmarks.Bookmark;
import de.logrifle.data.bookmarks.Bookmarks;
import de.logrifle.data.bookmarks.BookmarksListener;
import de.logrifle.data.parsing.Line;
import de.logrifle.ui.cmd.ExecutionResult;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ViewsTree {
    private final ViewsTreeNode rootNode;
    private final DataViewMerged rootView;
    private final Bookmarks bookmarks;
    private ViewsTreeNode focusedNode;

    public ViewsTree(DataViewMerged rootView, Bookmarks bookmarks) {
        this.rootNode = new ViewsTreeNode(null, rootView);
        this.rootView = rootView;
        this.bookmarks = bookmarks;
        this.focusedNode = rootNode;
    }

    public void addNodeAndSetFocus(ViewsTreeNode parent, ViewsTreeNode child) {
        parent.addChild(child);
        this.focusedNode = child;
    }

    public ExecutionResult removeNode(ViewsTreeNode node) {
        @Nullable ViewsTreeNode parent = node.getParent();
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
        @Nullable ViewsTreeNode parent = focusedNode.getParent();
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
        @Nullable ViewsTreeNode parent = focusedNode.getParent();
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
        @Nullable ViewsTreeNode parent = current.getParent();
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
            setFocusedNode(viewsTreeNode);
            return true;
        }
    }

    public void setFocusedNode(ViewsTreeNode viewsTreeNode) {
        this.focusedNode = viewsTreeNode;
    }

    public List<DataView> getViews(){
        return this.rootView.getViews();
    }

    public ExecutionResult addView(DataView dataView){
        return this.rootView.addView(dataView);
    }

    /**
     * @throws IndexOutOfBoundsException if there is no View at the given index
     */
    public DataView removeView(int viewIndex){
        DataView removed = this.rootView.removeView(viewIndex);
        this.bookmarks.removeBookmarksOf(removed);
        return removed;
    }

    public ExecutionResult toggleView(int viewIndex){
        return this.rootView.toggleView(viewIndex);
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

    public void fireFullUpdate() {
        rootNode.getDataView().fireUpdated();
    }

    public void fireLinesVisiblityInvalidated(Collection<Line> invalidatedLines) {
        rootNode.getDataView().fireLineVisibilityInvalidatedLater(invalidatedLines);
    }

    public BookmarksListener buildBookmarksListener() {
        return new BookmarksListener() {
            @Override
            public void added(Bookmarks source, Collection<Bookmark> added) {
                fire(added);
            }

            @Override
            public void removed(Bookmarks source, Collection<Bookmark> removed) {
                fire(removed);
            }

            @Override
            public void forcedDisplayChanged(Bookmarks source) {
                fire(source.getAll());
            }

            private void fire(Collection<Bookmark> bookmarks) {
                fireLinesVisiblityInvalidated(bookmarks.stream().map(Bookmark::getLine).collect(Collectors.toList()));
            }
        };
    }

    public interface Walker {
        void handleNode(ViewsTreeNode node, int recursionDepth, boolean focused);
    }
}

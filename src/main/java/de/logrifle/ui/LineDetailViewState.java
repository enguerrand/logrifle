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

import de.logrifle.data.parsing.Line;
import de.logrifle.ui.cmd.ExecutionResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class LineDetailViewState {
    public static LineDetailViewState IGNORED = new LineDetailViewState();

    @Nullable
    private Line line;
    private int scrollPos = -1;

    void set(Line line) {
        this.line = line;
        this.scrollPos = -1;
    }

    void reset() {
        line = null;
        scrollPos = -1;
    }

    void updateScrollPosition(int rows) {
        Line currentLine = this.line;
        if (currentLine == null) {
            return;
        }
        if (this.scrollPos == -1) {
            return;
        }
        int maxScroll = currentLine.getAdditionalLines().size() - rows;
        this.scrollPos = Math.min(maxScroll, this.scrollPos);
    }

    boolean isMainLineVisible(Line line) {
        return !isActive(line) || scrollPos == -1;
    }

    boolean isActive() {
        return this.line != null;
    }

    boolean isActive(Line line) {
        return Objects.equals(this.line, line);
    }

    ExecutionResult scroll(int delta, int renderableLineCount) {
        Line currentLine = this.line;
        if (currentLine == null) {
            return new ExecutionResult(false);
        }
        if (this.scrollPos == -1 && delta < 0) {
            return new ExecutionResult(false);
        }
        int additionalLinesCount = currentLine.getAdditionalLines().size();
        if (additionalLinesCount - scrollPos <= renderableLineCount && delta > 0) {
            return new ExecutionResult(false);
        }
        if (delta < 0) {
            this.scrollPos = Math.max(-1, this.scrollPos + delta);
        } else {
            this.scrollPos = Math.min(additionalLinesCount - renderableLineCount, this.scrollPos + delta);
        }
        return new ExecutionResult(true);
    }

    List<String> getAdditionalLinesToRender(Line lineToRender, int maxRenderableLineCount) {
        Line currentLine = this.line;
        if (currentLine == null || !currentLine.equals(lineToRender)) {
            return Collections.emptyList();
        }
        List<String> additionalLines = currentLine.getAdditionalLines();
        ArrayList<String> linesToRender = new ArrayList<>();
        for (int i = Math.max(0, scrollPos); i < Math.min(additionalLines.size(), maxRenderableLineCount + scrollPos); i++) {
            linesToRender.add(additionalLines.get(i));
        }
        return linesToRender;
    }

    boolean toggle(Line focusedLine) {
        if (this.line != null) {
            this.reset();
            return false;
        } else {
            this.set(focusedLine);
            return true;
        }
    }

    int getTopLinesCountToSkip(List<Line> lines) {
        Line currentLine = this.line;
        if (currentLine == null) {
            return 0;
        }
        int linesAboveCurrentLine = lines.indexOf(currentLine);
        if (linesAboveCurrentLine <= 0) {
            return 0;
        }
        int linesCountInView = lines.size();
        int ownTotalLinesCount = currentLine.getAdditionalLines().size() + 1;
        if (ownTotalLinesCount <= linesCountInView - linesAboveCurrentLine) {
            return 0;
        }
        return Math.min(linesAboveCurrentLine, ownTotalLinesCount + linesAboveCurrentLine - linesCountInView);
    }

    private int getAdditionalLinesCountToRender() {
        Line currentLine = this.line;
        if (currentLine == null) {
            return 0;
        }
        return Math.max(0, currentLine.getAdditionalLines().size() - Math.max(0, scrollPos));
    }

    boolean needsScrolling(int rows) {
        Line currentLine = this.line;
        if (currentLine == null) {
            return false;
        }
        return rows < currentLine.getAdditionalLines().size() + 1;
    }

    int computeRenderableLineCount(List<Line> lines, int rows) {
        if (!isActive()){
            return lines.size();
        }
        int linesToSubtract = getAdditionalLinesCountToRender();
        int max = Math.max(1, lines.size() - linesToSubtract);
        if (linesToSubtract < rows - 1) {
            max ++;
        }
        return max;
    }
}

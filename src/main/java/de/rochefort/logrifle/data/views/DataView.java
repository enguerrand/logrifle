package de.rochefort.logrifle.data.views;

import de.rochefort.logrifle.data.parsing.Line;

import java.util.List;

public interface DataView {
    List<Line> getLines(int topIndex, int maxCount);
}

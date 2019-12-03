package de.rochefort.logrifle.ui;

import com.googlecode.lanterna.gui2.Label;
import de.rochefort.logrifle.data.parsing.Line;

public interface LogLineRenderer {
    Label render(Line line, int lineIndex, int visibleLineCount);
}

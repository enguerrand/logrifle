package de.rochefort.logrifle.ui;

import com.googlecode.lanterna.gui2.Label;
import de.rochefort.logrifle.data.parsing.Line;

public class DefaultLogLineRenderer implements LogLineRenderer {
    @Override
    public Label render(Line line, int lineIndex, int visibleLineCount) {
        int digitCount = getDigitCount(visibleLineCount);
        return new Label(String.format(" %"+digitCount+"d %s", lineIndex, line.getRaw()));
    }

    private int getDigitCount(int n) {
        int count = 0;
        while (n != 0) {
            n = n / 10;
            ++count;
        }
        return count;
    }
}

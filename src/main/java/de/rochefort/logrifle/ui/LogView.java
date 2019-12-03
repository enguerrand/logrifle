package de.rochefort.logrifle.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LayoutManager;
import com.googlecode.lanterna.gui2.Panel;
import de.rochefort.logrifle.data.parsing.Line;
import de.rochefort.logrifle.data.views.DataView;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class LogView {
    private final Panel panel;
    private final LogLineRenderer logLineRenderer = new DefaultLogLineRenderer();

    LogView() {
        LayoutManager layout = new GridLayout(1);
        panel = new Panel(layout);
    }

    Panel getPanel() {
        return panel;
    }

    void update(@Nullable TerminalSize newTerminalSize, DataView dataView) {
        TerminalSize size = newTerminalSize != null ? newTerminalSize : panel.getSize();
        int rows = size.getRows();

        panel.removeAllComponents();
        List<Line> lines = dataView.getLines(0, Math.max(0, rows));

        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            Label label = logLineRenderer.render(line, i+1, lines.size());
            panel.addComponent(label);
        }
    }
}

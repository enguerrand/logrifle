package de.rochefort.logrifle.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LayoutManager;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import de.rochefort.logrifle.data.parsing.Line;
import de.rochefort.logrifle.data.views.DataView;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainWindow {

    private DataView dataView = null;
    private final Window window;
    private Screen screen;
    private final Panel logPanel;

    public MainWindow() {

        window = new BasicWindow("logrifle");
        window.setHints(Arrays.asList(
                Window.Hint.FULL_SCREEN,
                Window.Hint.NO_DECORATIONS,
                Window.Hint.FIT_TERMINAL_WINDOW,
                Window.Hint.NO_POST_RENDERING
        ));
        BorderLayout layoutManager = new BorderLayout();
        Panel mainPanel = new Panel(layoutManager);
        LayoutManager logLayout = new GridLayout(1);
        logPanel = new Panel(logLayout);
        logPanel.setLayoutData(BorderLayout.Location.CENTER);
        mainPanel.addComponent(logPanel);
        window.setComponent(mainPanel);
    }

    public void setDataView(DataView dataView) {
        this.dataView = dataView;
        updateLogView();
    }

    private void updateLogView() {
        updateLogView(null);
    }

    private void updateLogView(@Nullable TerminalSize newSize) {
        if (screen == null) {
            return;
        }
        logPanel.removeAllComponents();
        TerminalSize size = newSize != null ? newSize : logPanel.getSize();

        int rows = size.getRows();
        List<Line> lines = dataView.getLines(0, Math.max(0, rows));
        int i = 0;
        for (Line line : lines) {
            Label label = new Label(++i + " " + line.getRaw());
            logPanel.addComponent(label);
        }
    }

    public void close() throws IOException {
        if(screen != null) {
            screen.stopScreen();
        }
    }

    public void start() throws IOException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        screen = terminalFactory.createScreen();
        screen.startScreen();
        final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);

        textGUI.setTheme(LanternaThemes.getRegisteredTheme("businessmachine"));
        textGUI.addListener((gui, keyStroke) -> {
            switch (keyStroke.getKeyType()) {
                case Escape:
                    try {
                        close();
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case F5:
                    updateLogView();
                    break;
                default:
                    break;
            }
            return false;
        });

        window.addWindowListener(new WindowListenerAdapter() {
            @Override
            public void onResized(Window window, TerminalSize previousSize, TerminalSize newSize) {
                updateLogView(newSize);
            }
        });

        textGUI.addWindowAndWait(window);
    }
}

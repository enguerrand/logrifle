package de.rochefort.logrifle.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextGUIThread;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import de.rochefort.logrifle.data.views.DataView;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executor;

public class MainWindow {

    private DataView dataView = null;
    private final Window window;
    private Screen screen;
    private TextGUIThread guiThread;
    private final LogView logView;

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
        logView = new LogView();
        mainPanel.addComponent(logView.getPanel());
        logView.getPanel().setLayoutData(BorderLayout.Location.CENTER);

        window.setComponent(mainPanel);
    }

    /**
     * Must be called on the gui thread
     */
    void setDataView(DataView dataView) {
        this.dataView = dataView;
        updateView();
    }

    /**
     * Must be called on the gui thread
     */
    void updateView() {
        updateView(null);
    }

    /**
     * Must be called on the gui thread
     */
    private void updateView(@Nullable TerminalSize newTerminalSize) {
        if (screen == null) {
            return;
        }
        checkGuiThreadOrThrow();
        logView.update(newTerminalSize, dataView);
    }

    void close() throws IOException {
        checkGuiThreadOrThrow();
        window.close();
        if(screen != null) {
            screen.stopScreen();
        }
    }

    public void start(Executor executor, MainWindowListener callback) {
        executor.execute(() -> {
            try {
                if (screen != null) {
                    throw new IllegalStateException("Already started!");
                }
                DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
                screen = terminalFactory.createScreen();
                screen.startScreen();
                final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
                guiThread = textGUI.getGUIThread();
                textGUI.setTheme(LanternaThemes.getRegisteredTheme("businessmachine"));
                textGUI.addListener(callback);

                window.addWindowListener(new WindowListenerAdapter() {
                    @Override
                    public void onResized(Window window, TerminalSize previousSize, TerminalSize newSize) {
                        // TODO: This is not the logview size!
                        updateView(newSize);
                    }
                });
                textGUI.addWindowAndWait(window);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                callback.onClosed();
            }
        });
    }

    private void checkGuiThreadOrThrow() {
        if (!Objects.equals(Thread.currentThread(), guiThread.getThread())) {
            throw new IllegalStateException("This method must be called on the gui thread!");
        }
    }
}

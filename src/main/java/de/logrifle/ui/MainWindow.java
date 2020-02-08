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

package de.logrifle.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import de.logrifle.base.DefaultUncaughtExceptionHandler;
import de.logrifle.base.LogDispatcher;
import de.logrifle.data.bookmarks.Bookmarks;
import de.logrifle.data.highlights.HighlightsData;
import de.logrifle.data.views.DataView;
import de.logrifle.data.views.ViewsTree;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executor;

public class MainWindow {

    private final KeyStrokeDispatchingWindow window;
    private Screen screen;
    private final LogView logView;
    private final CommandView commandView;
    private final SideBar sideBar;
    private boolean sidebarVisible = true;
    private final BookmarksView bookmarksView;
    private final Bookmarks bookmarks;
    private boolean bookmarksViewVisible = true;
    private final ViewsTree viewsTree;
    private final HighlightsData highlightsData;
    private final Panel mainPanel;

    public MainWindow(
            ViewsTree viewsTree,
            HighlightsData highlightsData,
            Bookmarks bookmarks,
            LogDispatcher logDispatcher,
            boolean followTail,
            int maxAbsoluteSidebarWidth,
            double maxRelativeSidebarWidth, LineLabelDisplayMode initialLineLabelDisplayMode
    ) {
        this.viewsTree = viewsTree;
        this.highlightsData = highlightsData;
        this.bookmarks = bookmarks;
        window = new KeyStrokeDispatchingWindow("logrifle", UI::runLater);
        window.setHints(Arrays.asList(
                Window.Hint.FULL_SCREEN,
                Window.Hint.NO_DECORATIONS,
                Window.Hint.FIT_TERMINAL_WINDOW,
                Window.Hint.NO_POST_RENDERING
        ));
        BorderLayout layoutManager = new BorderLayout();
        mainPanel = new Panel(layoutManager);
        BorderLayout logAreaLayout = new BorderLayout();
        Panel logArea = new Panel(logAreaLayout);
        DefaultLogLineRenderer logLineRenderer = new DefaultLogLineRenderer();
        logView = new LogView(logDispatcher, highlightsData, logLineRenderer, bookmarks, followTail, initialLineLabelDisplayMode);
        logArea.addComponent(logView.getPanel());
        logView.getPanel().setLayoutData(BorderLayout.Location.CENTER);
        bookmarksView = new BookmarksView(bookmarks, logLineRenderer);
        logArea.addComponent(bookmarksView.getPanel());
        bookmarksView.getPanel().setLayoutData(BorderLayout.Location.BOTTOM);
        mainPanel.addComponent(logArea);
        logArea.setLayoutData(BorderLayout.Location.CENTER);
        sideBar = new SideBar(viewsTree, highlightsData, maxAbsoluteSidebarWidth, maxRelativeSidebarWidth);
        mainPanel.addComponent(sideBar.getPanel());
        sideBar.getPanel().setLayoutData(BorderLayout.Location.LEFT);
        commandView = new CommandView();
        mainPanel.addComponent(commandView.getPanel());
        commandView.getPanel().setLayoutData(BorderLayout.Location.BOTTOM);
        window.addInteractableListener(commandView);

        window.setComponent(mainPanel);
    }

    void setCommandViewListener(CommandViewListener commandViewListener) {
        this.commandView.setListener(commandViewListener);
    }

    /**
     * Must be called on the gui thread
     */
    public void updateView() {
        updateView(null);
    }

    /**
     * Must be called on the gui thread
     */
    private void updateView(@Nullable TerminalSize newTerminalSize) {
        if (screen == null) {
            return;
        }
        UI.checkGuiThreadOrThrow();
        @Nullable MainWindowLayout mainWindowLayout;
        if (newTerminalSize != null) {
            int sideBarWidth = sideBar.update(sidebarVisible, newTerminalSize.getColumns());
            if (sideBarWidth >= newTerminalSize.getColumns()) {
                throw new IllegalStateException("Side bar is too large: "+sideBarWidth+" >= " + newTerminalSize.getColumns());
            }
            mainWindowLayout = MainWindowLayout.compute(newTerminalSize, commandView.getHeight(), sideBarWidth, bookmarks.count(), bookmarksViewVisible);
        } else {
            mainWindowLayout = null;
        }
        DataView dataView = viewsTree.getFocusedNode().getDataView();
        logView.update(mainWindowLayout != null ? mainWindowLayout.getLogViewSize() : null, dataView);
        commandView.update(mainWindowLayout != null ? mainWindowLayout.getCommandBarSize() : null);
        TerminalSize availableSpaceForBookmarks;
        if (mainWindowLayout != null) {
            availableSpaceForBookmarks = mainWindowLayout.getBookmarksSize();
        } else {
            availableSpaceForBookmarks = MainWindowLayout.computeBookmarksSizeFrom(logView.getPanel().getSize().getColumns(), mainPanel.getSize().getRows(), commandView.getHeight(), bookmarks.count());
        }
        bookmarksView.update(
                bookmarksViewVisible,
                dataView.getLineCount(),
                logView.getHorizontalScrollPosition(),
                highlightsData.getHighlights(),
                availableSpaceForBookmarks,
                logView.getFocusedLineIndexInView(),
                logView.getLineLabelLength(dataView.getMaxLineLabelLength()),
                logView.isShowLineNumbers()
        );
    }

    void close() throws IOException {
        UI.checkGuiThreadOrThrow();
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
                Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(screen));
                screen.startScreen();
                final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
                UI.initialize(textGUI.getGUIThread());

                textGUI.setTheme(LanternaThemes.getRegisteredTheme("businessmachine"));
                textGUI.addListener(callback);

                window.addWindowListener(new WindowListenerAdapter() {
                    @Override
                    public void onResized(Window window, TerminalSize previousSize, TerminalSize newSize) {
                        updateView(newSize);
                    }
                });
                textGUI.addWindowAndWait(window);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            } catch (RuntimeException e) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                throw e;
            } finally {
                callback.onClosed();
            }
        });
    }

    void openCommandBar(String initialText) {
        UI.checkGuiThreadOrThrow();
        this.commandView.show(initialText);
        updateView(screen.getTerminalSize());
    }

    void closeCommandBar() {
        this.commandView.hide();
        updateView(screen.getTerminalSize());
    }

    void showCommandViewMessage(String message, @Nullable TextColor textColor) {
        this.commandView.showMessage(message, textColor == null ? TextColor.ANSI.DEFAULT : textColor);
    }

    LogView getLogView() {
        return this.logView;
    }

    DataView getDataView() {
        return viewsTree.getFocusedNode().getDataView();
    }

    SideBar getSideBar() {
        return sideBar;
    }

    public ViewsTree getViewsTree() {
        return viewsTree;
    }

    void toggleSidebar() {
        this.sidebarVisible = !this.sidebarVisible;
    }

    void toggleBookmarksView(){
        this.bookmarksViewVisible = !this.bookmarksViewVisible;
    }
}

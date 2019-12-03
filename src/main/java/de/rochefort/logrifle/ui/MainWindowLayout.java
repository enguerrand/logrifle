package de.rochefort.logrifle.ui;

import com.googlecode.lanterna.TerminalSize;
import org.jetbrains.annotations.Nullable;

class MainWindowLayout {
    private final TerminalSize logViewSize;
    private final TerminalSize commandBarSize;

    MainWindowLayout(TerminalSize logViewSize, TerminalSize commandBarSize) {
        this.logViewSize = logViewSize;
        this.commandBarSize = commandBarSize;
    }

    public TerminalSize getLogViewSize() {
        return logViewSize;
    }

    public TerminalSize getCommandBarSize() {
        return commandBarSize;
    }

    static MainWindowLayout compute(@Nullable TerminalSize terminalSize, boolean commandBarVisible) {
        if(terminalSize == null) {
            return null;
        }
        TerminalSize cmd = new TerminalSize(terminalSize.getColumns(), commandBarVisible ? 1 : 0);
        TerminalSize log = new TerminalSize(terminalSize.getColumns(), terminalSize.getRows() - cmd.getRows());
        return new MainWindowLayout(
                log,
                cmd
        );
    }
}
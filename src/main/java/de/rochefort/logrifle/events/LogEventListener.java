package de.rochefort.logrifle.events;

import de.rochefort.logrifle.data.parsing.Line;

public interface LogEventListener {
    void newLogLine(Line line);
}

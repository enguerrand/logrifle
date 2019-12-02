package de.rochefort.logrifle.data.parsing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LineParseResult {
    private final @Nullable Line parsedLine;
    private final @Nullable String text;
    private final boolean newLine;

    private LineParseResult(@Nullable Line parsedLine, @Nullable String text, boolean newLine) {
        this.parsedLine = parsedLine;
        this.text = text;
        this.newLine = newLine;
    }

    public LineParseResult(@NotNull Line parsedLine) {
        this(parsedLine, null, true);
    }

    public LineParseResult(@NotNull String text) {
        this(null, text, false);
    }

    public Line getParsedLine() {
        if(parsedLine == null) {
            throw new IllegalStateException("No parsed line available! Check whether isNewLine() is true before calling this method.");
        }
        return parsedLine;
    }

    public String getText() {
        if(text == null) {
            throw new IllegalStateException("No text available! Check whether isNewLine() is false before calling this method.");
        }
        return text;
    }

    public boolean isNewLine() {
        return newLine;
    }
}

package de.rochefort.logrifle.data.parsing;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineParserTimestampedTextImpl implements LineParser {
    public static final String DEFAULT_TIME_MATCH_REGEX = ".*(\\d{2}:\\d{2}:\\d{2}\\.\\d{3}).*";
    public static final String DEFAULT_DATE_FORMAT = "HH:mm:ss.SSS";
    private final Pattern timeStampPattern;
    private final DateFormat dateFormat;

    public LineParserTimestampedTextImpl() {
        this(null, null);
    }

    public LineParserTimestampedTextImpl(String timestampRegex, String dateFormat) {
        this.timeStampPattern = Pattern.compile(timestampRegex != null ? timestampRegex : DEFAULT_TIME_MATCH_REGEX);
        this.dateFormat = new SimpleDateFormat(dateFormat != null ? dateFormat : DEFAULT_DATE_FORMAT);
    }

    @Override
    public LineParseResult parse(String raw) {
        long timestamp;
        Matcher matcher = timeStampPattern.matcher(raw);
        if (matcher.find()) {
            String dateString = matcher.group(1);
            Date parsed = null;
            try {
                parsed = this.dateFormat.parse(dateString);
                timestamp = parsed.getTime();
            } catch (ParseException e) {
                throw new IllegalStateException("Error while parsing datestring. \""+dateString+"\"." +
                        "The date string pattern matches but the matched string cannot be parsed with the given date format! " +
                        "Complete log line: \""+raw+"\"", e);
            }
        } else {
            return new LineParseResult(raw);
        }
        return new LineParseResult(new Line(raw, timestamp));
    }
}

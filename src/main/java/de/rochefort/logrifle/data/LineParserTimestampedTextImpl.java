package de.rochefort.logrifle.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineParserTimestampedTextImpl implements LineParser {
    private final Pattern timeStampPattern;
    private final DateFormat dateFormat;

    public LineParserTimestampedTextImpl(String timestampRegex, String dateFormatPattern) {
        this.timeStampPattern = Pattern.compile(timestampRegex);
        this.dateFormat = new SimpleDateFormat(dateFormatPattern);
    }

    @Override
    public Line parse(String raw) {

        long timestamp;
        Matcher matcher = timeStampPattern.matcher(raw);
        if (matcher.find()) {
            String dateString = matcher.group(1);
            Date parsed = null;
            try {
                parsed = this.dateFormat.parse(dateString);
                timestamp = parsed.getTime();
            } catch (ParseException e) {
                // TODO
                timestamp = System.currentTimeMillis();
            }
        } else {
            // TODO:
            timestamp = System.currentTimeMillis();
        }
        return new Line(raw, timestamp);
    }

    public static void main(String[] args) {
        LineParserTimestampedTextImpl parser = new LineParserTimestampedTextImpl(".*(\\d{2}:\\d{2}:\\d{2}\\.\\d{3}).*", "HH:mm:ss.SSS");
        String raw = "DEBUG 23:12:33.234 - asdiklajsdj";
        Line line = parser.parse(raw);


    }
}

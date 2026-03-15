package se.martinuhlen.fishbase.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * JUL formatter that produces ISO-8601 local timestamps and short class names.
 *
 * @author Martin
 */
public class LogFormatter extends Formatter
{
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    @Override
    public String format(LogRecord record)
    {
        String timestamp = TIMESTAMP.format(Instant.ofEpochMilli(record.getMillis()));
        String level = String.format("%-7s", record.getLevel().getName());
        String name = simpleName(record.getLoggerName());
        String message = formatMessage(record);
        String thrown = formatThrown(record);
        return timestamp + " " + level + " [" + name + "] " + message + thrown + "\n";
    }

    private static String simpleName(String loggerName)
    {
        if (loggerName == null)
        {
            return "";
        }
        int dot = loggerName.lastIndexOf('.');
        return dot >= 0 ? loggerName.substring(dot + 1) : loggerName;
    }

    private static String formatThrown(LogRecord record)
    {
        if (record.getThrown() == null)
        {
            return "";
        }
        StringWriter sw = new StringWriter();
        record.getThrown().printStackTrace(new PrintWriter(sw));
        return "\n" + sw;
    }
}

package se.martinuhlen.fishbase.javafx;

/**
 * Stupid main class wrapper workaround for {@link FishBaseApplication} which won't start in Java 11, see http://mail.openjdk.java.net/pipermail/openjfx-dev/2018-June/021977.html
 *
 * @author Martin
 */
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import se.martinuhlen.fishbase.utils.LogFormatter;

public class FishBaseMain
{
    public static void main(String[] args)
    {
        configureLogging();
        FishBaseApplication.main(args);
    }

    private static void configureLogging()
    {
        Logger rootLogger = Logger.getLogger("");
        for (Handler handler : rootLogger.getHandlers())
        {
            rootLogger.removeHandler(handler);
        }
        StreamHandler stdout = new StreamHandler(System.out, new LogFormatter())
        {
            @Override
            public synchronized void publish(LogRecord record)
            {
                super.publish(record);
                flush();
            }
        };
        stdout.setLevel(Level.ALL);
        rootLogger.addHandler(stdout);
    }
}

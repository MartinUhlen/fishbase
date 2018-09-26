package se.martinuhlen.fishbase.javafx;

/**
 * Stupid main class wrapper workaround for {@link FishBaseApplication} which won't start in Java 11, see http://mail.openjdk.java.net/pipermail/openjfx-dev/2018-June/021977.html
 *
 * @author Martin
 */
public class FishBaseMain
{
    public static void main(String[] args)
    {
        FishBaseApplication.main(args);
    }
}

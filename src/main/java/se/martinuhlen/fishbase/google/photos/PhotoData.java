package se.martinuhlen.fishbase.google.photos;

import java.io.InputStream;

/**
 * The actual (non meta) data (image or video) of a {@link Photo}.
 *
 * @author Martin
 */
public interface PhotoData
{
    /**
     * Gets an URL to where the photo is located.
     * 
     * @return photo URL
     */
    String getUrl();

    /**
     * Gets an input stream to read the photo data.
     * 
     * @return photo stream
     */
    InputStream getStream();
}

package se.martinuhlen.fishbase.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A storage where data is persisted.
 *
 * @author Martin
 */
public interface Persistence
{
    /**
     * Gets an input stream with given name.
     * 
     * @param name of the data to read
     * @return input stream with given name
     * @throws IOException if data can't be read
     */
	InputStream input(String name) throws IOException;

    /**
     * Gets an output stream with given name.
     * 
     * @param name of the data to write
     * @return output stream with given name
     * @throws IOException if data can't be written
     */
	OutputStream output(String name) throws IOException;
}

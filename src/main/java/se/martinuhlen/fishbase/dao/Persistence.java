package se.martinuhlen.fishbase.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A storage where data is persisted.
 *
 * @author Martin
 */
public interface Persistence {
    /**
     * Gets an input stream from a file with given name.
     *
     * @param dir directory where the file is located
     * @param name of the file to read
     * @return input stream for the given file
     * @throws IOException if the file can't be read
     */
    InputStream input(PersistenceDirectory dir, String name) throws IOException;

    /**
     * Gets an output stream to a file with given name.
     *
     * @param dir directory where the file is located
     * @param name of the file to write
     * @return output stream for the given file
     * @throws IOException if the file can't be written
     */
    OutputStream output(PersistenceDirectory dir, String name) throws IOException;
}

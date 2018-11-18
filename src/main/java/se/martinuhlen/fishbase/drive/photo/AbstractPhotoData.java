package se.martinuhlen.fishbase.drive.photo;

import static java.io.OutputStream.nullOutputStream;
import static java.util.UUID.randomUUID;
import static se.martinuhlen.fishbase.utils.Checked.get;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import se.martinuhlen.fishbase.utils.Checked;

/**
 * Abstract {@link PhotoData} implementation.
 * <p>
 * Initially all images are stored remote in Google Photos, but once downloaded they're cached locally.
 * 
 * @author Martin
 */
abstract class AbstractPhotoData implements PhotoData
{
    final File localFile;

    AbstractPhotoData(File localFile)
    {
        this.localFile = localFile;
    }

    @Override
    public final String getUrl()
    {
        if (localFile.exists())
        {
            return getLocalUrl();
        }
        else
        {
            return getUrlWhenLocalFileIsMissing();
        }
    }

    /**
     * Gets URL when local file is missing in {@link #getUrl()}.
     * 
     * @return URL to use when local file is missing
     */
    abstract String getUrlWhenLocalFileIsMissing();

    /**
     * Gets the URL of {@link #localFile}.
     * 
     * @return local URL
     */
    final String getLocalUrl()
    {
        return Checked.get(() -> localFile.toURI().toURL().toExternalForm());
    }

    /**
     * Downlaods the photo from {@link #getRemoteStream()} to {@link #localFile}.
     */
    final void download()
    {
        Checked.run(() ->
        {
            try (var in = createDownloadStream(); var out = nullOutputStream();)
            {
                in.transferTo(out);
            }
        });
    }

    /**
     * Gets an {@link InputStream} to the photo, either remote or locally cached.
     * <p>
     * Remote photos are written to cache when streamed.
     * 
     * @return photo stream
     */
    @Override
    public final InputStream getStream()
    {
        if (localFile.exists())
        {
            return get(() -> new FileInputStream(localFile));
        }
        else
        {
            return createDownloadStream();
        }
    }

    /**
     * 0ets an input stream that wraps {@link #getRemoteStream()} and while read writes to {@link #localFile}.
     * 
     * @return download stream
     */
    private InputStream createDownloadStream()
    {
        return new InputStream()
        {
            private InputStream inputStream;
            private OutputStream outputStream;
            private File tempFile;
            private boolean closed;

            @Override
            public int read() throws IOException
            {
                if (closed)
                {
                    return -1;
                }
                return readImpl();
            }

            private int readImpl() throws IOException, MalformedURLException, FileNotFoundException
            {
                if (inputStream == null)
                {
                    String tempFileName = localFile.getName() + "." + randomUUID() + ".temp";
                    tempFile = new File(localFile.getParentFile(), tempFileName);
                    tempFile.deleteOnExit();
                    outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
                    inputStream = getRemoteStream();
                }
                
                int read = inputStream.read();
                if (read != -1)
                {
                    outputStream.write(read);
                }
                else
                {
                    close();
                    tempFile.renameTo(localFile);
                }
                return read;
            }

            @Override
            public void close() throws IOException
            {
                if (!closed)
                {
                    closed = true;
                    super.close();
                    if (inputStream != null)
                    {
                        inputStream.close();
                        outputStream.close();
                    }
                }
            }
        };
    }

    /**
     * Gets in input stream to the remote photo.
     * 
     * @return remote stream
     */
    abstract InputStream getRemoteStream() throws IOException;
}

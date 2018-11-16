package se.martinuhlen.fishbase.drive.photo;

import static java.io.OutputStream.nullOutputStream;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static se.martinuhlen.fishbase.utils.Checked.$;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import se.martinuhlen.fishbase.utils.Checked;

/**
 * The source of a photo.
 * <p>
 * Initially all photos are stored remote in Google Photos, but once downloaded they're cached locally.
 * 
 * @author Martin
 */
class PhotoSource
{
    private static final File CACHE_DIR = new File(new java.io.File(System.getProperty("user.home"), ".fishbase"), "cache");
    static
    {
        CACHE_DIR.mkdirs();
    }

    private final File localFile;
    private final String remoteUrl;

    // TODO Add video support
    
    PhotoSource(com.google.api.services.drive.model.File remoteFile, String remoteUrl, String suffix)
    {
        String fileName = remoteFile.getName().replace(":", "");
        String extension = getExtension(fileName);
        fileName = fileName.replace("." + extension, "_" + remoteFile.getId() + suffix + "." + extension);
        this.localFile = new File(CACHE_DIR, fileName);
        this.remoteUrl = remoteUrl;
    }

    /**
     * Gets URL to the image, either remote or locally cached.
     * 
     * @return image URL
     */
    String getUrl()
    {
        return Checked.get(() ->
        {
            if (localFile.exists())
            {
                return localFile.toURI().toURL().toString();
            }
            else
            {
                // Download the image so it's available locally next time
                CompletableFuture.runAsync($(() ->
                {
                    try(var in = getInputStream(); var out = nullOutputStream();)
                    {
                        in.transferTo(out);
                    }
                }));
                // But for this time, use the remote URL right away
                return remoteUrl;
            }
        });
    }

    /**
     * Gets an {@link InputStream} to the image, either remote or locally cached.
     * <p>
     * Remote images are written to cache when streamed.
     * <p>
     * Both remote and local streams are wrappers, that delay initialization until first read (which hopefully is done outside of UI thread).
     * 
     * @return image input stream
     */
    InputStream getInputStream()
    {
        if (localFile.exists())
        {
            return new InputStream()
            {
                private InputStream inputStream;
                private boolean closed;

                @Override
                public int read() throws IOException
                {
                    if (closed)
                    {
                        throw new IOException("InputStream of local file '" + localFile.getName() + "' is closed.");
                    }
                    if (inputStream == null)
                    {
                        inputStream = new FileInputStream(localFile);
                    }
                    int read = inputStream.read();
                    if (read == -1)
                    {
                        close();
                    }
                    return read;
                }

                @Override
                public void close() throws IOException
                {
                    super.close();
                    if (inputStream != null)
                    {
                        inputStream.close();
                        closed = true;
                    }
                }
            };
        }
        else
        {
            return new InputStream()
            {
                private InputStream inputStream;
                private OutputStream outputStream;

                @Override
                public int read() throws IOException
                {
                    if (inputStream == null)
                    {
                        inputStream = new URL(remoteUrl).openStream();
                        outputStream = new BufferedOutputStream(new FileOutputStream(localFile));
                    }

                    int read = inputStream.read();
                    if (read != -1)
                    {
                        outputStream.write(read);
                    }
                    else
                    {
                        close();
                    }
                    return read;
                }

                @Override
                public void close() throws IOException
                {
                    super.close();
                    if (inputStream != null)
                    {
                        inputStream.close();
                        outputStream.close();
                    }
                }
            };
        }
    }
}

package se.martinuhlen.fishbase.google.photos;

import static java.util.UUID.randomUUID;
import static se.martinuhlen.fishbase.utils.Checked.get;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.function.Supplier;

/**
 * {@link PhotoData} implementation of locally stored data.
 * 
 * @author Martin
 */
class LocalPhotoData implements PhotoData
{
	private File localFile;
	private Supplier<PhotoData> remote;	// FIXME What if photo is removed from Google Photos but remains in FishBase?

	LocalPhotoData(File localFile, Supplier<PhotoData> remote)
	{
		this.localFile = localFile;
		this.remote = remote;
	}

	@Override
	public String getUrl()
	{
		if (localFile.exists())
		{
			return get(() -> localFile.toURI().toURL().toExternalForm());
		}
		else
		{
			// FIXME Start download in separate thread?
			return remote.get().getUrl();
		}
	}

	@Override
	public InputStream getStream()
	{
		if (localFile.exists())
		{
			return get(() -> new BufferedInputStream(new FileInputStream(localFile)));
		}
		else
		{
			return new DownloadingInputStream();
		}
	}

	/**
	 * An input stream that reads from a remote stream and meanwhile writes to a local file.
	 * 
	 * @author Martin
	 */
	private class DownloadingInputStream extends InputStream
	{
		private InputStream remoteStream;
	    private OutputStream localStream;
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
	        if (remoteStream == null)
	        {
	            String tempFileName = localFile.getName() + "." + randomUUID() + ".temp";
	            tempFile = new File(localFile.getParentFile(), tempFileName);
	            tempFile.deleteOnExit();
	            localStream = new BufferedOutputStream(new FileOutputStream(tempFile));
	            remoteStream = remote.get().getStream();
	        }

	        int read = remoteStream.read();
	        if (read != -1)
	        {
	            localStream.write(read);
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
	            if (remoteStream != null)
	            {
	                remoteStream.close();
	                localStream.close();
	            }
	        }
	    }
	}
}

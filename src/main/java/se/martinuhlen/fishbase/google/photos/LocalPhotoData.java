package se.martinuhlen.fishbase.google.photos;

import static java.util.UUID.randomUUID;
import static se.martinuhlen.fishbase.utils.Checked.get;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.function.Supplier;

/**
 * {@link PhotoData} implementation of locally stored data.
 * 
 * @author Martin
 */
class LocalPhotoData implements PhotoData
{
	private static final URL PHOTO_NOT_FOUND = LocalPhotoData.class.getResource("/images/PhotoNotFound.png");

	private File localFile;
	private Supplier<PhotoData> remote;

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
			try
			{
				return remote.get().getUrl();
			}
			catch (Exception e)
			{
				return PHOTO_NOT_FOUND.toExternalForm();
			}
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
			return new RemoteOrNotFoundInputStream();
		}
	}
	
	/**
	 * Input stream for the remote photo, or fallback to "photo not found".
	 * 
	 * @author Martin
	 */
	private class RemoteOrNotFoundInputStream extends InputStream
	{
		private InputStream inputStream;

		@Override
		public int read() throws IOException
		{
			if (inputStream == null)
			{
				createInputStream();
			}
			return inputStream.read();
		}

		private void createInputStream() throws IOException
		{
			try
			{
				inputStream = new DownloadingInputStream(remote.get().getStream());
			}
			catch (Exception e)
			{
				inputStream = PHOTO_NOT_FOUND.openStream();
			}
		}

		@Override
		public void close() throws IOException
		{
			if (inputStream != null)
			{
				inputStream.close();
				inputStream = null;
			}
		}
	}

	/**
	 * An input stream that reads from a remote stream and meanwhile writes to a local file.
	 * 
	 * @author Martin
	 */
	private class DownloadingInputStream extends InputStream
	{
		private final InputStream remoteStream;
	    private OutputStream localStream;
	    private File tempFile;
	    private boolean closed;

	    DownloadingInputStream(InputStream remoteStream) throws IOException
	    {
			this.remoteStream = remoteStream;
			String tempFileName = localFile.getName() + "." + randomUUID() + ".temp";
			tempFile = new File(localFile.getParentFile(), tempFileName);
			tempFile.deleteOnExit();
			localStream = new BufferedOutputStream(new FileOutputStream(tempFile));
		}

		@Override
	    public int read() throws IOException
	    {
	        if (closed)
	        {
	            return -1;
	        }
	        else
	        {
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
	    }

	    @Override
	    public void close() throws IOException
	    {
	        if (!closed)
	        {
	            closed = true;
	            super.close();
	            remoteStream.close();
	            if (localStream != null)
	            {
	                localStream.close();
	            }
	        }
	    }
	}
}

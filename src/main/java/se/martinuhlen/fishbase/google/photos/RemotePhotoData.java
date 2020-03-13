package se.martinuhlen.fishbase.google.photos;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

import se.martinuhlen.fishbase.utils.Checked;

/**
 * {@link PhotoData} implementation of remotely stored data, eg. Google.
 * 
 * @author Martin
 */
class RemotePhotoData implements PhotoData
{
	private String url;

	RemotePhotoData(String url)
	{
		this.url = url;
	}

	@Override
	public String getUrl()
	{
		return url;
	}

	@Override
	public InputStream getStream()
	{
		return Checked.get(() -> new BufferedInputStream(new URL(url).openStream()));
	}
}

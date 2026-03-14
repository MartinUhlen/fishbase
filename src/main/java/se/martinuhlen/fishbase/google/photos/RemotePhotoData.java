package se.martinuhlen.fishbase.google.photos;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Supplier;

import se.martinuhlen.fishbase.utils.Checked;

/**
 * {@link PhotoData} implementation of remotely stored data, eg. Google.
 *
 * @author Martin
 */
class RemotePhotoData implements PhotoData
{
	private String url;
	private Supplier<String> accessToken;

	RemotePhotoData(String url)
	{
		this.url = url;
		this.accessToken = null;
	}

	RemotePhotoData(String url, Supplier<String> accessToken)
	{
		this.url = url;
		this.accessToken = accessToken;
	}

	@Override
	public String getUrl()
	{
		return url;
	}

	@Override
	public InputStream getStream()
	{
		return Checked.get(() ->
		{
			if (accessToken != null)
			{
				HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
				conn.setRequestProperty("Authorization", "Bearer " + accessToken.get());
				return new BufferedInputStream(conn.getInputStream());
			}
			else
			{
				return new BufferedInputStream(new URL(url).openStream());
			}
		});
	}
}

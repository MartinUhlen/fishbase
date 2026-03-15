package se.martinuhlen.fishbase.google.photos;

import java.time.LocalDateTime;
import java.util.function.Supplier;

/**
 * {@link GooglePhoto} implementation based on the Google Photos Picker API.
 *
 * @author Martin
 */
class PickerGooglePhoto implements GooglePhoto
{
	private final String id;
	private final String filename;
	private final LocalDateTime createTime;
	private final boolean video;
	private final String baseUrl;
	private final Supplier<String> accessToken;

	PickerGooglePhoto(String id, String filename, LocalDateTime createTime, boolean video, String baseUrl, Supplier<String> accessToken)
	{
		this.id = id;
		this.filename = filename;
		this.createTime = createTime;
		this.video = video;
		this.baseUrl = baseUrl;
		this.accessToken = accessToken;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public String getName()
	{
		return filename;
	}

	@Override
	public LocalDateTime getTime()
	{
		return createTime;
	}

	@Override
	public boolean isVideo()
	{
		return video;
	}

	@Override
	public PhotoData getThumbnail()
	{
		return new RemotePhotoData(baseUrl + "=w512-h512", accessToken);
	}

	@Override
	public PhotoData getContent()
	{
		return new RemotePhotoData(baseUrl + (video ? "=dv" : "=d"), accessToken);
	}
}

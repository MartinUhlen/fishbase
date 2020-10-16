package se.martinuhlen.fishbase.google.photos;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.google.photos.types.proto.MediaItem;
import com.google.protobuf.Timestamp;

/**
 * Default implementation of {@link GooglePhoto}.
 * 
 * @author martin
 */
final class GooglePhotoImpl implements GooglePhoto
{
	private final MediaItem item;

	GooglePhotoImpl(MediaItem item)
	{
		this.item = item;
	}

	@Override
	public String getId()
	{
		return item.getId();
	}

	@Override
	public String getName()
	{
		return item.getFilename();
	}

	@Override
	public LocalDateTime getTime()
	{
		Timestamp time = item.getMediaMetadata().getCreationTime();
		return Instant.ofEpochSecond(time.getSeconds())
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
	}

	@Override
	public boolean isVideo()
	{
		return item.getMimeType().startsWith("video");
	}

	@Override
	public PhotoData getThumbnail()
	{
		return new RemotePhotoData(item.getBaseUrl());
	}

	@Override
	public PhotoData getContent()
	{
		// See https://developers.google.com/photos/library/guides/access-media-items#base-urls
		if (isImage())
		{
			return new RemotePhotoData(item.getBaseUrl() + "=d");
		}
		else // Video
		{
			return new RemotePhotoData(item.getBaseUrl() + "=dv");
		}
	}
}

package se.martinuhlen.fishbase.drive.photo;

import java.io.InputStream;
import java.time.LocalDateTime;

public interface Photo
{
	String getId();

	String getName();

	LocalDateTime getTime();

	boolean isVideo();

	default boolean isImage()
	{
		return !isVideo();
	}

	// Initially we had only URLS, then refactored to streams.
	// But JavaFX was dead slow with loading larger images with InputStream, so decided to keep both variants.
	String getThumbnailUrl();
	InputStream getThumbnailStream();
	String getImageUrl();
	InputStream getImageStream();

	InputStream getVideoStream();
}

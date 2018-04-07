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

	String getThumbnailUrl();

	String getContentUrl();

	InputStream getContentStream();
}

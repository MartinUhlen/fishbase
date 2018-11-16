package se.martinuhlen.fishbase.drive.photo;

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

	PhotoData getThumbnail();

	PhotoData getContent();
}

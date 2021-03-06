package se.martinuhlen.fishbase.google.photos;

import java.time.LocalDateTime;

/**
 * A photo in "Google Photos".
 * 
 * @author Martin
 */
public interface GooglePhoto
{
    /**
     * Gets the unique ID of this photo.
     * 
     * @return unique photo ID
     */
	String getId();

	/**
	 * Gets the name of this photo.
	 * 
	 * @return photo name
	 */
	String getName();

	/**
	 * Gets the time this photo was taken.
	 * 
	 * @return photo time
	 */
	LocalDateTime getTime();

	/**
	 * Gets if this photo is a video (rather than an image).
	 * 
	 * @return {@code true} if this photo is a video, {@code false} for image
	 */
	boolean isVideo();

    /**
     * Gets if this photo is an image (rather than a video).
     * 
     * @return {@code true} if this photo is an image, {@code false} for video
     */
	default boolean isImage()
	{
		return !isVideo();
	}

	/**
	 * Gets the thumbnail data of this photo.
	 * 
	 * @return thumbnail data
	 */
	PhotoData getThumbnail();

	/**
	 * Gets the content data of this photo.
	 * 
	 * @return content data
	 */
	PhotoData getContent();
}

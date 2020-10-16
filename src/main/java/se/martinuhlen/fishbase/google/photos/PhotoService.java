package se.martinuhlen.fishbase.google.photos;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import com.google.photos.library.v1.PhotosLibraryClient;

import se.martinuhlen.fishbase.domain.Photo;
import se.martinuhlen.fishbase.domain.Trip;

/**
 * A service to work against Google Photos API.
 * 
 * @author martin
 */
public interface PhotoService
{
	/**
	 * Loads a list of photos.
	 * 
	 * @param photos to be loaded
	 * @return loaded photos
	 */
	List<FishingPhoto> load(List<Photo> photos);

	/**
	 * Searches all photos taken taken between a period of dates.
	 * 
	 * @param from the start date (inclusive)
	 * @param to the end date (inclusive)
	 * @return a list of all photos taken between {@code from} and {@code to}
	 */
	List<GooglePhoto> search(LocalDate from, LocalDate to);

	/**
	 * Creates a fishing photo of a taken photo.
	 * 
	 * @param photo taken on fishing {@link Trip trip}
	 * @param tripId identifies the fishing trip
	 * @return new fishing photo
	 */
	FishingPhoto create(GooglePhoto photo, String tripId);

	/**
	 * Adds photos to the album containing all {@link FishingPhoto fishing photos}.
	 *
	 * @param photoIds a collection of IDs identifying the photos to add
	 */
	void addToAlbum(Collection<String> photoIds);

	/**
	 * Removes photos from the album containing all {@link FishingPhoto fishing photos}.
	 *
	 * @param photoIds a collection of IDs identifying the photos to remove
	 */
	void removeFromAlbum(Collection<String> photoIds);

	/**
   * Creates a new {@link PhotoService}.
	 * 
	 * @param client used by the service
	 * @return new photo service
	 */
	public static PhotoService create(PhotosLibraryClient client)
	{
		return new PhotoServiceImpl(client);
	}
}

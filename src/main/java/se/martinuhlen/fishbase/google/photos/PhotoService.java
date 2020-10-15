package se.martinuhlen.fishbase.google.photos;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import com.google.photos.library.v1.PhotosLibraryClient;

import se.martinuhlen.fishbase.domain.Photo;
import se.martinuhlen.fishbase.domain.Trip;

public interface PhotoService
{
	List<FishingPhoto> load(List<Photo> photos);

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

	public static PhotoService create(PhotosLibraryClient client)
	{
		return new PhotoServiceImpl(client);
	}
}

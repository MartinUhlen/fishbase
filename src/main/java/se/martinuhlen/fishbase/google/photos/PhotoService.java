package se.martinuhlen.fishbase.google.photos;

import java.time.LocalDate;
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

	public static PhotoService create(PhotosLibraryClient client)
	{
		return new PhotoServiceImpl(client);
	}
}

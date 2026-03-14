package se.martinuhlen.fishbase.google.photos;

import java.util.List;

import se.martinuhlen.fishbase.domain.Photo;
import se.martinuhlen.fishbase.domain.Trip;
import se.martinuhlen.fishbase.google.drive.DriveService;

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
	 * Opens the Google Photos Picker in the browser and waits for the user to
	 * select photos. Blocks until the user has finished picking or the operation
	 * times out / is interrupted.
	 *
	 * @return a list of all photos picked by the user
	 */
	List<GooglePhoto> pick();

	/**
	 * Creates a fishing photo of a taken photo.
	 *
	 * @param photo taken on fishing {@link Trip trip}
	 * @param tripId identifies the fishing trip
	 * @return new fishing photo
	 */
	FishingPhoto create(GooglePhoto photo, String tripId);

	/**
	 * Creates a new {@link PhotoService}.
	 *
	 * @param pickerClient to let user pick photos
	 * @param driveService to upload picked photos
	 * @return new photo service
	 */
	public static PhotoService create(PickerClient pickerClient, DriveService driveService)
	{
		return new PhotoServiceImpl(pickerClient, driveService);
	}
}

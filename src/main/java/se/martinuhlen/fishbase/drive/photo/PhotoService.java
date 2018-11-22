package se.martinuhlen.fishbase.drive.photo;

import java.util.List;

import com.google.api.services.drive.Drive;

/**
 * Service API for {@link Photo}.
 * 
 * @author Martin
 */
public interface PhotoService
{
    /**
     * Gets all {@link FishingPhoto fishing photos}, ever taken.
     * 
     * @return all fishing photos
     */
	List<FishingPhoto> getFishingPhotos();

	/**
	 * Gets all {@link FishingPhoto fishing photos}, from a certain trip.
	 * 
	 * @param id of the trip to query
	 * @return all photos from the trip
	 */
	List<FishingPhoto> getTripPhotos(String id);

	/**
	 * Gets all {@link FishingPhoto fishing photos}, from a certain trip showing a certain specimen.
	 * 
	 * @param tripId id of the trip the specimen was caught
	 * @param specimenId id of the specimen to query
	 * @return all photos of the specimen
	 */
	List<FishingPhoto> getSpecimenPhotos(String tripId, String specimenId);

	/**
	 * Searches photo by a search term.
	 * 
	 * @param term to search with
	 * @return photos matching the search term
	 */
	List<Photo> searchPhotos(String term);

	/**
	 * Saves a new or existing photo.
	 * 
	 * @param photo to save
	 */
	void savePhoto(FishingPhoto photo);

	/**
	 * Removes a photo.
	 * 
	 * @param photo to remove
	 */
	void removePhoto(FishingPhoto photo);

	/**
	 * Creates a new photo service.
	 * 
	 * @param drive which the service should work against
	 * @return a new photo service
	 */
	public static PhotoService create(Drive drive)
	{
		return new PhotoServiceImpl(drive);
	}
}

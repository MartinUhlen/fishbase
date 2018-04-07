package se.martinuhlen.fishbase.drive.photo;

import java.util.List;

import com.google.api.services.drive.Drive;

public interface PhotoService
{
	List<FishingPhoto> getFishingPhotos();

	List<FishingPhoto> getTripPhotos(String id);

	List<FishingPhoto> getSpecimenPhotos(String tripId, String specimenId);

	List<Photo> searchPhotos(String search);

	void savePhoto(FishingPhoto photo);

	void removePhoto(FishingPhoto photo);

	public static PhotoService create(Drive drive)
	{
		return new PhotoServiceImpl(drive);
	}
}

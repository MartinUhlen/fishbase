package se.martinuhlen.fishbase.google.photos;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;

import se.martinuhlen.fishbase.domain.Photo;
import se.martinuhlen.fishbase.google.photos.PickerClient.PickerSession;
import se.martinuhlen.fishbase.utils.Logger;

/**
 * Default implementation of {@link PhotoService}.
 *
 * @author martin
 */
class PhotoServiceImpl implements PhotoService
{
	private static final Logger LOGGER = Logger.getLogger(PhotoServiceImpl.class);
	private static final long TIMEOUT_MS = 30L * 60L * 1000L; // 30 minutes

	private final PickerClient pickerClient;

	PhotoServiceImpl(PickerClient pickerClient)
	{
		this.pickerClient = pickerClient;
	}

	@Override
	public List<FishingPhoto> load(List<Photo> photos)
	{
		requireNonNull(photos, "photos cannot be null");
		log("Loading " + photos.size() + " photos");
		return photos
				.stream()
				.map(photo -> new FishingPhotoImpl(photo, () -> { throw new IllegalStateException("Photo not in local cache"); }))
				.collect(toList());
	}

	@Override
	public List<GooglePhoto> pick()
	{
		try
		{
			log("Creating picker session");
			PickerSession session = pickerClient.createSession();
			log("Picker session created: " + session.id() + ", opening browser at " + session.pickerUri());

			Desktop.getDesktop().browse(URI.create(session.pickerUri()));

			long pollIntervalMs = session.pollIntervalMs();
			long deadline = System.currentTimeMillis() + TIMEOUT_MS;

			while (System.currentTimeMillis() < deadline)
			{
				Thread.sleep(pollIntervalMs);
				log("Polling picker session " + session.id());
				if (pickerClient.isSelectionDone(session.id()))
				{
					log("Selection done, listing media items");
					List<PickerGooglePhoto> items = pickerClient.listMediaItems(session.id());
					log("Got " + items.size() + " media items");
					try
					{
						pickerClient.deleteSession(session.id());
						log("Picker session deleted");
					}
					catch (Exception e)
					{
						log("Failed to delete picker session: " + e);
					}
					return List.copyOf(items);
				}
			}
			log("Picker session timed out after 30 minutes");
			try
			{
				pickerClient.deleteSession(session.id());
			}
			catch (Exception e)
			{
				log("Failed to delete timed-out picker session: " + e);
			}
			return emptyList();
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			log("Picker interrupted");
			return emptyList();
		}
		catch (Exception e)
		{
			log("Picker failed: " + e);
			throw new RuntimeException("Failed to pick photos", e);
		}
	}

	@Override
	public FishingPhoto create(GooglePhoto photo, String tripId)
	{
		Photo domain = Photo.asNew(photo.getId())
				.tripId(tripId)
				.specimens(emptySet())
				.fileName(photo.getName())
				.time(photo.getTime())
				.starred(false);

		return new FishingPhotoImpl(domain, () -> photo);
	}

	private void log(String message)
	{
		LOGGER.log(message);
	}
}

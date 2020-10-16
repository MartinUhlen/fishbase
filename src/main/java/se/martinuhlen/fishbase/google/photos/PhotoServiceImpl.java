package se.martinuhlen.fishbase.google.photos;

import static com.google.common.base.Suppliers.memoizeWithExpiration;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;

import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.proto.DateFilter;
import com.google.photos.library.v1.proto.Filters;
import com.google.photos.library.v1.proto.SearchMediaItemsRequest;
import com.google.photos.types.proto.Album;
import com.google.photos.types.proto.DateRange;
import com.google.photos.types.proto.MediaItem;
import com.google.type.Date;

import se.martinuhlen.fishbase.domain.Photo;
import se.martinuhlen.fishbase.utils.Logger;

/**
 * Default implementation of {@link PhotoService}.
 * 
 * @author martin
 */
class PhotoServiceImpl implements PhotoService
{
	private static final Logger LOGGER = Logger.getLogger(PhotoServiceImpl.class);
	
	private PhotosLibraryClient client;

	PhotoServiceImpl(PhotosLibraryClient client)
	{
		this.client = client;
	}

	@Override
	public List<FishingPhoto> load(List<Photo> photos)
	{
		requireNonNull(photos, "photos cannot be null");
		log("Loading " + photos.size() + " photos");
		return photos
				.stream()
				.map(photo -> new FishingPhotoImpl(photo, memoizeWithExpiration(() -> getPhoto(photo.getId()), 10, MINUTES)))
				.collect(toList());
	}

	private GooglePhoto getPhoto(String id)
	{
		log("Starting downloading photo " + id);
		MediaItem item = client.getMediaItem(id);
		log("Finished downloading photo " + id);
		return new GooglePhotoImpl(item);
	}

	@Override
	public List<GooglePhoto> search(LocalDate from, LocalDate to)
	{
		requireNonNull(from, "from cannot be null");
		requireNonNull(to, "to cannot be null");

		log("Searching photos " + from + " - " + to);
		Iterable<MediaItem> items = client.searchMediaItems(SearchMediaItemsRequest.newBuilder()
				.setPageSize(100)
				.setFilters(Filters.newBuilder()
						.setDateFilter(DateFilter.newBuilder()
								.addRanges(DateRange.newBuilder()
										.setStartDate(toDate(from))
										.setEndDate(toDate(to)).build())
								.build())
						.build())
				.build())
		.iterateAll();

		return StreamSupport.stream(items.spliterator(), false)
				.map(GooglePhotoImpl::new)
				.sorted(comparing(GooglePhoto::getTime))
				.collect(toList());
	}

	private Date toDate(LocalDate date)
	{
		return Date.newBuilder()
				.setYear(date.getYear())
				.setMonth(date.getMonthValue())
				.setDay(date.getDayOfMonth())
				.build();
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

	@Override
	public void addToAlbum(Collection<String> photoIds)
	{
		requireNonNull(photoIds, "photoIds cannot be null");
		if (photoIds.isEmpty())
		{
			return;
		}
		try
		{
			// FIXME This doesn't work, because album and/or photos are not created by FishBase.
			// https://issuetracker.google.com/issues/109505022
			// https://issuetracker.google.com/issues/132274769
			client.batchAddMediaItemsToAlbum(getAlbumId(), new ArrayList<>(photoIds));
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	private String getAlbumId()
	{
		Iterable<Album> albums = client.listAlbums().iterateAll();
		return stream(albums.spliterator(), false)
				.filter(album -> album.getTitle().equals("FishBase"))
				.map(album -> album.getId())
				.findFirst()
				.orElseGet(() -> createAlbum());
	}

	private String createAlbum()
	{
		return client.createAlbum("FishBase").getId();
	}

	@Override
	public void removeFromAlbum(Collection<String> photoIds)
	{
		requireNonNull(photoIds, "photoIds cannot be null");
		LOGGER.log("#removeFromAlbum not (yet) implemented... sleeping some, just for fun...");
		sleep(photoIds.size() * 250);
	}

	private void sleep(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private void log(String message)
	{
		LOGGER.log(message);
	}
}

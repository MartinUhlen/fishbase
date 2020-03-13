package se.martinuhlen.fishbase.google.photos;

import static com.google.common.base.Suppliers.memoizeWithExpiration;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.StreamSupport;

import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.proto.DateFilter;
import com.google.photos.library.v1.proto.Filters;
import com.google.photos.library.v1.proto.SearchMediaItemsRequest;
import com.google.photos.types.proto.DateRange;
import com.google.photos.types.proto.MediaItem;
import com.google.type.Date;

import se.martinuhlen.fishbase.domain.Photo;
import se.martinuhlen.fishbase.utils.Logger;

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

	private void log(String message)
	{
		LOGGER.log(message);
	}
}

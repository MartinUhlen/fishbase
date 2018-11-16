package se.martinuhlen.fishbase.drive.photo;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static se.martinuhlen.fishbase.utils.Checked.$;
import static se.martinuhlen.fishbase.utils.Checked.get;
import static se.martinuhlen.fishbase.utils.Checked.run;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.File.ImageMediaMetadata;
import com.google.api.services.drive.model.FileList;

import se.martinuhlen.fishbase.utils.Logger;

class PhotoServiceImpl implements PhotoService
{
	private static final Logger LOGGER = Logger.getLogger(PhotoService.class);
	private static final List<DateTimeFormatter> EXIF_TIME_FORMATS = List.of("yyyy:MM:dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss")
	        .stream()
	        .map(DateTimeFormatter::ofPattern)
	        .collect(toUnmodifiableList());

    static final java.io.File CACHE_DIR = new java.io.File(new java.io.File(System.getProperty("user.home"), ".fishbase"), "cache");
    static
    {
        CACHE_DIR.mkdirs();
    }

	private static final String FISHING_KEY = "fishing";
	private static final String TRIP_KEY = "trip";
	private static final String SPECIMENS_KEY = "specimens";
	private static final String STARRED_KEY = "starred";

	private static final String PHOTO_FIELDS = "nextPageToken, files(id, name, spaces, mimeType, thumbnailLink, webContentLink, createdTime, modifiedTime, imageMediaMetadata(time), appProperties)";
	private static final String BASE_PHOTO_QUERY =
			"trashed = false "
		+	"and "
		+ 	"("
				+ "mimeType contains 'image'"
				+ " or "
				+ "mimeType contains 'video'"
		+	")";

	private final Drive drive;

	PhotoServiceImpl(Drive drive)
	{
		this.drive = drive;
	}

	@Override
	public List<FishingPhoto> getFishingPhotos()
	{
		return getFishingPhotos(FISHING_KEY, "true");
	}

	@Override
	public List<FishingPhoto> getTripPhotos(String tripId)
	{
		return getFishingPhotos(TRIP_KEY, tripId);
	}

	@Override
	public List<FishingPhoto> getSpecimenPhotos(String tripId, String specimenId)
	{
		return getTripPhotos(tripId)
			.stream()
			.filter(photo -> photo.containsSpecimen(specimenId))
			.collect(toList());
	}

	private List<FishingPhoto> getFishingPhotos(String key, String value)
	{
		String query = String.format("appProperties has {key='%s' and value='%s'}", key, value);
		return listPhotos(query, this::toFishingPhoto);
	}

	@Override
	public List<Photo> searchPhotos(String searchString)
	{
		String search = trimToEmpty(searchString);
		if (search.length() < 4) // Require at least YYYY
		{
			return emptyList();
		}
		else
		{
			String searchQuery =
					  "("
							+     "name contains '" + search + "'"
							+ " or name contains '" + search.replace("-", "") + "'"
							+ " or name contains '" + "IMG_" + search + "'" // "IMG_" is common prefix for photos from iPhone and various digital cameras.
							+ " or name contains '" + "IMG_" + search.replace("-", "") + "'"
                            + " or name contains '" + "IMG-" + search + "'"
                            + " or name contains '" + "IMG-" + search.replace("-", "") + "'"
					+ ")";

			return listPhotos(searchQuery, this::toPhoto);
		}
	}

	private <P extends Photo> List<P> listPhotos(String subQuery, Function<File, P> mapper)
	{
		return get(() -> listPhotosImpl(subQuery, mapper));
	}

	private <P extends Photo> List<P> listPhotosImpl(String subQuery, Function<File, P> mapper) throws IOException
	{
		String query = BASE_PHOTO_QUERY + " and " + subQuery;
		log("Querying photos: " + query);

		List<File> photoFiles = new ArrayList<>();
		String pageToken = null;
		do
		{
			FileList result = drive.files().list()
				.setPageSize(1000)
				.setFields(PHOTO_FIELDS)
				.setQ(query)
				.setPageToken(pageToken)
				//.setSpaces("photos")
				.execute();

			log("Found " + result.getFiles().size() + " photos");

			photoFiles.addAll(result.getFiles());
			pageToken = result.getNextPageToken();
		}
		while (pageToken != null);

		return photoFiles
				.stream()
				.map(p -> mapper.apply(p))
				.collect(toList());
	}

	private FishingPhoto toFishingPhoto(File f)
	{
		return new FishingPhoto(
				toPhoto(f),
				f.getAppProperties().get(TRIP_KEY),
				f.getAppProperties().getOrDefault(SPECIMENS_KEY, ""),
				Boolean.valueOf(f.getAppProperties().get(STARRED_KEY)));
	}

	/**
	 * Creates a {@link Photo} from given {@link File}.
	 * <p>
	 * {@link File#getWebContentLink()} has the following problems:
	 * <ul>
	 *   <li>Requires auth (possible to solve)
	 *   <li>Some portrait photos are landscaped
	 *   <li>Is really slooooooow (needs to be confirmed, might be due to piped streams)
	 * </ul>
	 * So instead we use thumbnailLink and increase size with the {@code + "0"} hack.
	 */
	private Photo toPhoto(File f)
	{
	    boolean video = f.getMimeType().startsWith("video");
		return new PhotoImpl(
			f.getId(),
			f.getName(),
			timeOf(f),
			video,
			new ImageData(localFileOf(f, "_thumb"), f.getThumbnailLink()),
			contentSourceOf(f, video));
	}

    private PhotoData contentSourceOf(File f, boolean video)
    {
        if (video)
        {
            return new VideoData(localFileOf(f, ""), $(() -> drive.files().get(f.getId()).executeMediaAsInputStream()));
        }
        else
        {
            return new ImageData(localFileOf(f, ""), f.getThumbnailLink() + "0"); // HACK!
        }
    }

    private java.io.File localFileOf(File remoteFile, String suffix)
    {
        String fileName = remoteFile.getName().replace(":", "");
        String extension = getExtension(fileName);
        fileName = fileName.replace("." + extension, "_" + remoteFile.getId() + suffix + "." + extension);
        return new java.io.File(CACHE_DIR, fileName);
    }

	private LocalDateTime timeOf(File f)
	{
		return requireNonNullElseGet(
				timeOf(f.getImageMediaMetadata()),
				() -> toLocalDateTime(f.getCreatedTime()));
	}

	private LocalDateTime timeOf(ImageMediaMetadata meta)
	{
		return meta == null || meta.getTime() == null
				? null
				: timeOf(meta.getTime());
	}

	private LocalDateTime timeOf(String exifTime)
	{
	    LocalDateTime parsedTime = parseOrNull(exifTime, EXIF_TIME_FORMATS.get(0));
	    if (parsedTime != null)
	    {
	        return parsedTime;
	    }
	    else
	    {
    	    return EXIF_TIME_FORMATS
    	        .stream()
    	        .skip(1)
    	        .map(format -> parseOrNull(exifTime, format))
        	    .filter(time -> time != null)
        	    .findAny()
        	    .orElseThrow(() -> new IllegalArgumentException("Unsupported EXIF date/time format: " + exifTime));
	    }
	}

    private LocalDateTime parseOrNull(String exifTime, DateTimeFormatter format)
    {
        try
        {
            return LocalDateTime.parse(exifTime, format);	            
        }
        catch (DateTimeParseException e)
        {
            return null;
        }
    }

	/**
	 * Converts UTC date "2016-06-13T16:51:25.000Z" to local date "2016-06-13T18:51:25".
	 * <p>
	 * {@link DateTime#getTimeZoneShift()} seems always to be 0...
	 */
	private LocalDateTime toLocalDateTime(DateTime time)
	{
		return Instant.ofEpochSecond(time.getValue() / 1_000)
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
	}

	@Override
	public void savePhoto(FishingPhoto photo)
	{
		run(() -> savePhotoImpl(photo, false));
	}

	@Override
	public void removePhoto(FishingPhoto photo)
	{
		run(() -> savePhotoImpl(photo, true));
	}

	private void savePhotoImpl(FishingPhoto photo, boolean remove) throws IOException
	{
		// According to docs, an entry with null value will be removed.
		// But it doesn't work, so we use "false" and "null" instead.
	    Map<String, String> appProperties = Map.of(
                FISHING_KEY, remove ? "false" : "true",
                TRIP_KEY, remove ? "null" : photo.getTripId(),
                SPECIMENS_KEY, remove ? "null" : photo.getSpecimenIds(),
                STARRED_KEY, remove ? "false" : Boolean.toString(photo.isStarred()));

		log("Updating photo " + photo.getName() + "(" + photo.getId() + ") with " + appProperties);

		File update = new File().setAppProperties(appProperties);
		drive.files().update(photo.getId(), update).execute();
	}

	private void log(String message)
	{
		LOGGER.log(message);
	}
}

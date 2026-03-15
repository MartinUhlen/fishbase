package se.martinuhlen.fishbase.google.photos;

import static org.apache.commons.io.FilenameUtils.getExtension;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import se.martinuhlen.fishbase.domain.Photo;

/**
 * Default implementation of {@link FishingPhoto}.
 *
 * @author Martin
 */
class FishingPhotoImpl implements FishingPhoto
{
	private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "mpg");
	private static final java.io.File CACHE_DIR = new java.io.File(new java.io.File(System.getProperty("user.home"), ".fishbase"), "cache");
	static
	{
		CACHE_DIR.mkdirs();
	}

	private final Set<Consumer<? super FishingPhoto>> listeners = new LinkedHashSet<>();
    private final Function<String, PhotoData> remoteContent;
    private final Function<String, PhotoData> remoteThumbnail;
    private Photo domain;

	private FishingPhotoImpl(Photo domain, Function<String, PhotoData> remoteContent, Function<String, PhotoData> remoteThumbnail)
    {
        this.domain = domain;
        this.remoteContent = remoteContent;
        this.remoteThumbnail = remoteThumbnail;
    }

    FishingPhotoImpl(Photo domain, Function<String, PhotoData> remote)
    {
        this(domain, remote, remote);
    }

	FishingPhotoImpl(Photo domain, GooglePhoto googlePhoto)
	{
	    this(domain, _ -> googlePhoto.getContent(), _ -> googlePhoto.getThumbnail());
	}

	@Override
	public String getId()
	{
		return domain.getId();
	}

	@Override
	public String getName()
	{
		return domain.getFileName();
	}

	@Override
	public LocalDateTime getTime()
	{
		return domain.getTime();
	}

	@Override
	public boolean isVideo()
	{
		// FIXME Add mimeType to Photo?
		String extension = getExtension(getName()).toLowerCase();
		return VIDEO_EXTENSIONS.contains(extension);
	}

	@Override
	public PhotoData getThumbnail()
	{
	    return getPhotoData(getThumbnailFileName(), remoteThumbnail);
	}

	@Override
	public PhotoData getContent()
	{
	    return getPhotoData(getContentFileName(), remoteContent);
	}

	private PhotoData getPhotoData(String fileName, Function<String, PhotoData> remote) {
	    File localFile = new File(CACHE_DIR, fileName);
	    return new LocalPhotoData(localFile, () -> remote.apply(fileName));
	}

	@Override
	public String getTripId()
	{
		return domain.getTripId();
	}

	@Override
	public boolean containsSpecimen(String specimenId)
	{
		return domain.getSpecimens().contains(specimenId);
	}

	@Override
	public void addSpecimen(String specimenId)
	{
		domain = domain.addSpecimen(specimenId);
		notifyListeners();
	}

	@Override
	public void removeSpecimen(String specimenId)
	{
		domain = domain.removeSpecimen(specimenId);
		notifyListeners();
	}

	@Override
	public void removeSpecimens(Collection<String> specimenIds)
	{
		domain = domain.removeSpecimens(specimenIds);
		notifyListeners();
	}

	@Override
	public boolean isStarred()
	{
		return domain.isStarred();
	}

	@Override
	public void setStarred(boolean starred)
	{
		domain = domain.withStarred(starred);
		notifyListeners();
	}

	@Override
	public Photo getDomain()
	{
		return domain;
	}

	@Override
	public void addListener(Consumer<? super FishingPhoto> listener)
	{
		this.listeners.add(listener);
	}

	private void notifyListeners()
	{
		listeners.forEach(l -> l.accept(this));
	}
}

package se.martinuhlen.fishbase.google.photos;

import static org.apache.commons.io.FilenameUtils.getExtension;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
	private final Supplier<GooglePhoto> googlePhoto;
	private Photo domain;

	FishingPhotoImpl(Photo domain, Supplier<GooglePhoto> googlePhoto)
	{
		this.domain = domain;
		this.googlePhoto = googlePhoto;
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
		return new LocalPhotoData(getLocalFile("_thumb"), () -> googlePhoto.get().getThumbnail());
	}

	@Override
	public PhotoData getContent()
	{
		return new LocalPhotoData(getLocalFile(""), () -> googlePhoto.get().getContent());
	}

	private File getLocalFile(String suffix)
	{
        String fileName = getName().replace(":", "");
        String extension = getExtension(fileName);
        fileName = fileName.replace("." + extension, "_" + getId() + suffix + "." + extension);
        return new java.io.File(CACHE_DIR, fileName);
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

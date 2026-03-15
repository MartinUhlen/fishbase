package se.martinuhlen.fishbase.google.photos;

import static org.apache.commons.io.FilenameUtils.getExtension;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import se.martinuhlen.fishbase.domain.Photo;
import se.martinuhlen.fishbase.google.photos.data.PhotoData;

/**
 * Default implementation of {@link FishingPhoto}.
 *
 * @author Martin
 */
class FishingPhotoImpl implements FishingPhoto {
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "mpg");

    private final Set<Consumer<? super FishingPhoto>> listeners = new LinkedHashSet<>();
    private final Function<String, PhotoData> content;
    private final Function<String, PhotoData> thumbnail;
    private Photo domain;

    FishingPhotoImpl(Photo domain, Function<String, PhotoData> content, Function<String, PhotoData> thumbnail) {
        this.domain = domain;
        this.content = content;
        this.thumbnail = thumbnail;
    }

    FishingPhotoImpl(Photo domain, Function<String, PhotoData> photoData) {
        this(domain, photoData, photoData);
    }

    @Override
    public String getId() {
        return domain.getId();
    }

    @Override
    public String getName() {
        return domain.getFileName();
    }

    @Override
    public LocalDateTime getTime() {
        return domain.getTime();
    }

    @Override
    public boolean isVideo() {
        // FIXME Add mimeType to Photo?
        String extension = getExtension(getName()).toLowerCase();
        return VIDEO_EXTENSIONS.contains(extension);
    }

    @Override
    public PhotoData getThumbnail() {
        return thumbnail.apply(getThumbnailFileName());
    }

    @Override
    public PhotoData getContent() {
        return content.apply(getContentFileName());
    }

    @Override
    public String getTripId() {
        return domain.getTripId();
    }

    @Override
    public boolean containsSpecimen(String specimenId) {
        return domain.getSpecimens().contains(specimenId);
    }

    @Override
    public void addSpecimen(String specimenId) {
        domain = domain.addSpecimen(specimenId);
        notifyListeners();
    }

    @Override
    public void removeSpecimen(String specimenId) {
        domain = domain.removeSpecimen(specimenId);
        notifyListeners();
    }

    @Override
    public void removeSpecimens(Collection<String> specimenIds) {
        domain = domain.removeSpecimens(specimenIds);
        notifyListeners();
    }

    @Override
    public boolean isStarred() {
        return domain.isStarred();
    }

    @Override
    public void setStarred(boolean starred) {
        domain = domain.withStarred(starred);
        notifyListeners();
    }

    @Override
    public Photo getDomain() {
        return domain;
    }

    @Override
    public void addListener(Consumer<? super FishingPhoto> listener) {
        this.listeners.add(listener);
    }

    private void notifyListeners() {
        listeners.forEach(l -> l.accept(this));
    }
}

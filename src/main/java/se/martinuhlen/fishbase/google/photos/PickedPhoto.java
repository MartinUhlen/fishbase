package se.martinuhlen.fishbase.google.photos;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import se.martinuhlen.fishbase.google.photos.data.PhotoData;
import se.martinuhlen.fishbase.google.photos.data.RemotePhotoData;

/**
 * A {@link GooglePhoto} picked in the Google Photos Picker API.
 */
final class PickedPhoto implements GooglePhoto {
    private final String id;
    private final String filename;
    private final LocalDateTime time;
    private final boolean video;
    private final String baseUrl;
    private final Supplier<String> accessToken;

    PickedPhoto(String id, String filename, LocalDateTime time, boolean video, String baseUrl, Supplier<String> accessToken) {
        this.id = id;
        this.filename = filename;
        this.time = time;
        this.video = video;
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return filename;
    }

    @Override
    public LocalDateTime getTime() {
        return time;
    }

    @Override
    public boolean isVideo() {
        return video;
    }

    @Override
    public PhotoData getThumbnail() {
        return new RemotePhotoData(baseUrl + "=w512-h512", accessToken);
    }

    @Override
    public PhotoData getContent() {
        return new RemotePhotoData(baseUrl + (video ? "=dv" : "=d"), accessToken);
    }
}

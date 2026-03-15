package se.martinuhlen.fishbase.google.photos;

import java.time.LocalDateTime;

/**
 * Default implementation of {@link GooglePhoto}.
 *
 * @author martin
 */
final class GooglePhotoImpl implements GooglePhoto {
    private final String id;
    private final String filename;
    private final LocalDateTime time;
    private final boolean video;
    private final String baseUrl;

    GooglePhotoImpl(String id, String filename, LocalDateTime time, boolean video, String baseUrl) {
        this.id = id;
        this.filename = filename;
        this.time = time;
        this.video = video;
        this.baseUrl = baseUrl;
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
        return new RemotePhotoData(baseUrl);
    }

    @Override
    public PhotoData getContent() {
        // See https://developers.google.com/photos/library/guides/access-media-items#base-urls
        if (isImage()) {
            return new RemotePhotoData(baseUrl + "=d");
        }
        else { // Video
            return new RemotePhotoData(baseUrl + "=dv");
        }
    }
}

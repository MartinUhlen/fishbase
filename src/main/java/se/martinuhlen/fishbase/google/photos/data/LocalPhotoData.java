package se.martinuhlen.fishbase.google.photos.data;

import java.io.InputStream;

/**
 * {@link PhotoData} backed by a {@link LocalPhoto}.
 */
class LocalPhotoData implements PhotoData {

    private final LocalPhoto photo;

    LocalPhotoData(LocalPhoto photo) {
        this.photo = photo;
    }

    @Override
    public String getUrl() {
        return photo.getUrl();
    }

    @Override
    public InputStream getStream() {
        return photo.getInputStream();
    }
}

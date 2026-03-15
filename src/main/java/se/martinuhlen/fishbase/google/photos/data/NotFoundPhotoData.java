package se.martinuhlen.fishbase.google.photos.data;

import static se.martinuhlen.fishbase.utils.Checked.get;

import java.io.InputStream;
import java.net.URL;

enum NotFoundPhotoData implements PhotoData {
    INSTANCE;

    private static final URL PHOTO_NOT_FOUND = NotFoundPhotoData.class.getResource("/images/PhotoNotFound.png");

    @Override
    public String getUrl() {
        return PHOTO_NOT_FOUND.toExternalForm();
    }

    @Override
    public InputStream getStream() {
        return get(() -> PHOTO_NOT_FOUND.openStream());
    }
}

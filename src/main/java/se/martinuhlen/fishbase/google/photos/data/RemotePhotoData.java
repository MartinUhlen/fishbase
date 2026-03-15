package se.martinuhlen.fishbase.google.photos.data;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.function.Supplier;

import se.martinuhlen.fishbase.utils.Checked;

/**
 * {@link PhotoData} implementation of remotely stored data, eg. Google.
 *
 * @author Martin
 */
public class RemotePhotoData implements PhotoData {
    private final String url;
    private final Supplier<String> accessToken;

    public RemotePhotoData(String url, Supplier<String> accessToken) {
        this.url = url;
        this.accessToken = accessToken;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public InputStream getStream() {
        return Checked.get(() -> {
            URL urlToStream = URI.create(url).toURL();
            HttpURLConnection conn = (HttpURLConnection) urlToStream.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + accessToken.get());
            return new BufferedInputStream(conn.getInputStream());
        });
    }
}

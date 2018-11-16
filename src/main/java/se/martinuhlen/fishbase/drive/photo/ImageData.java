package se.martinuhlen.fishbase.drive.photo;

import static se.martinuhlen.fishbase.utils.Checked.$;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * The data of an image.
 *
 * @author Martin
 */
class ImageData extends AbstractPhotoData
{
    private final String remoteUrl;

    ImageData(File localFile, String remoteUrl)
    {
        super(localFile);
        this.remoteUrl = remoteUrl;
    }

    @Override
    String getUrlWhenLocalFileIsMissing()
    {
        // Download the image so it's available locally next time
        CompletableFuture.runAsync($(() -> download()));

        // But for this time, use the remote URL right away
        return remoteUrl;
    }

    @Override
    InputStream getRemoteStream() throws IOException
    {
        return new URL(remoteUrl).openStream();
    }
}

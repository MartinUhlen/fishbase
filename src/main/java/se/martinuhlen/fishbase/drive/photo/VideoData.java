package se.martinuhlen.fishbase.drive.photo;

import java.io.File;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * The data of a video.
 *
 * @author Martin
 */
class VideoData extends AbstractPhotoData
{
    private final Supplier<InputStream> inputStreamSupplier;

    VideoData(File localFile, Supplier<InputStream> inputStreamSupplier)
    {
        super(localFile);
        this.inputStreamSupplier = inputStreamSupplier;
    }

    @Override
    String getUrlWhenLocalFileIsMissing()
    {
        download();
        return getLocalUrl();
    }

    @Override
    InputStream getRemoteStream()
    {
        return inputStreamSupplier.get();
    }
}

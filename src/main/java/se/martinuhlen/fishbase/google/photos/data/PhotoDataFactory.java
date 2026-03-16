package se.martinuhlen.fishbase.google.photos.data;

import com.google.common.flogger.FluentLogger;

import se.martinuhlen.fishbase.google.drive.DriveService;

/**
 * A factory to get hold of {@link PhotoData} instances.
 */
public final class PhotoDataFactory {

    private static final FluentLogger LOG = FluentLogger.forEnclosingClass();

    private final DriveService driveService;

    public PhotoDataFactory(DriveService driveService) {
        this.driveService = driveService;
    }

    public PhotoData get(String fileName) {
        LocalPhoto localPhoto = new LocalPhoto(fileName);
        if (!localPhoto.exists()) {
            LOG.atInfo().log("File not found in local cache, will try to download %s", localPhoto.getFileName());
            downloadFromDrive(localPhoto);
        }
        if (localPhoto.exists()) {
            return new LocalPhotoData(localPhoto);
        } else {
            return NotFoundPhotoData.INSTANCE;
        }
    }

    private void downloadFromDrive(LocalPhoto localPhoto) {
        driveService.download("photos", localPhoto.getFileName(), () -> localPhoto.getOutputStream());
    }
}

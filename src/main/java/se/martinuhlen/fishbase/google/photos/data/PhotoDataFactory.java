package se.martinuhlen.fishbase.google.photos.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import se.martinuhlen.fishbase.google.drive.DriveService;

public final class PhotoDataFactory {

    private final DriveService driveService;

    public PhotoDataFactory(DriveService driveService) {
        this.driveService = driveService;
    }

    public PhotoData get(String fileName) {
        LocalPhoto localPhoto = new LocalPhoto(fileName);
        if (!localPhoto.exists()) {
            downloadFromDrive(localPhoto);
        }
        if (localPhoto.exists()) {
            return new LocalPhotoData(localPhoto.getFile(), null);
        } else {
            return NotFoundPhotoData.INSTANCE;
        }
    }

    private void downloadFromDrive(LocalPhoto localPhoto) {
        File file = localPhoto.getFile();
        driveService.download(file.getName(), () -> new BufferedOutputStream(new FileOutputStream(file)));
    }
}

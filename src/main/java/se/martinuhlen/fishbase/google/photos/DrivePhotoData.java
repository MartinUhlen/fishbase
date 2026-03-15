package se.martinuhlen.fishbase.google.photos;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import se.martinuhlen.fishbase.google.drive.DriveService;

/**
 * A photo stored on Google Drive.
 */
class DrivePhotoData implements PhotoData {

    private final DriveService service;
    private final LocalPhoto localPhoto;

    DrivePhotoData(String fileName, DriveService service) {
        localPhoto = new LocalPhoto(fileName);
        this.service = service;
    }

    @Override
    public String getUrl() {
        getStream();
        File file = localPhoto.getFile();
        return file.toURI().toString();
    }

    @Override
    public InputStream getStream() {
        File file = localPhoto.getFile();
        try {
            if (!file.exists()) {
                service.download(file.getName(), new BufferedOutputStream(new FileOutputStream(file)));
            }
            return new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

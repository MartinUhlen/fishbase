package se.martinuhlen.fishbase.google.photos.data;

import static se.martinuhlen.fishbase.utils.Checked.get;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

class LocalPhoto {
    private static final java.io.File CACHE_DIR = new java.io.File(new java.io.File(System.getProperty("user.home"), ".fishbase"), "cache");
    static    {
        CACHE_DIR.mkdirs();
    }

    private final File file;

    LocalPhoto(String fileName) {
        this.file = new File(CACHE_DIR, fileName);
    }

    boolean exists() {
        return file.exists();
    }

    String getFileName() {
        return file.getName();
    }

    File getFile() {
        return file;
    }

    String getUrl() {
        return get(() -> file.toURI().toURL().toExternalForm());
    }

    InputStream getInputStream() {
        return get(() -> new BufferedInputStream(new FileInputStream(file)));
    }

    OutputStream getOutputStream() {
        return get(() -> new BufferedOutputStream(new FileOutputStream(file)));
    }
}

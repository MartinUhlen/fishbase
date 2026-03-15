package se.martinuhlen.fishbase.google.photos.data;

import static se.martinuhlen.fishbase.utils.Checked.get;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class LocalPhoto {
    private static final java.io.File CACHE_DIR = new java.io.File(new java.io.File(System.getProperty("user.home"), ".fishbase"), "cache");
    static    {
        CACHE_DIR.mkdirs();
    }

    private final File file;

    public LocalPhoto(String fileName) {
        this.file = new File(CACHE_DIR, fileName);
    }

    boolean exists() {
        return file.exists();
    }

    public String getFileName() {
        return file.getName();
    }

    public File getFile() {
        return file;
    }

    public String getUrl() {
        return get(() -> file.toURI().toURL().toExternalForm());
    }

    public InputStream getInputStream() {
        return get(() -> new BufferedInputStream(new FileInputStream(file)));
    }

    public OutputStream getOutputStream() {
        return get(() -> new BufferedOutputStream(new FileOutputStream(file)));
    }
}

package se.martinuhlen.fishbase.javafx.photo;

import static java.util.Objects.requireNonNull;

import java.io.InputStream;
import java.util.function.Supplier;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.Image;

/**
 * Loads {@link Image} in background thread.
 *
 * @author Martin
 */
class ImageLoader extends Service<Image> implements Supplier<Image>
{
    private final String url;
    private final InputStream inputStream;

    private ImageLoader(String url, InputStream inputStream, boolean start)
    {
        this.url = url;
        this.inputStream = inputStream;
        if (start)
        {
            start();
        }
    }

    /**
     * Creates a new loader instance.
     * 
     * @param url to read the image from
     * @param start {@code true} to {@link #start()} this loader immediately
     */
    ImageLoader(String url, boolean start)
    {
        this(requireNonNull(url, "url can't be null"), null, start);
    }

    /**
     * Creates a new loader instance.
     * 
     * @param inputStream to read the image from
     * @param start {@code true} to {@link #start()} this loader immediately
     */
    ImageLoader(InputStream inputStream, boolean start)
    {
        this(null, requireNonNull(inputStream, "inputStream can't be null"), start);
    }

    @Override
    protected Task<Image> createTask()
    {
        return new Task<>()
        {
            @Override
            protected Image call() throws Exception
            {
                return url != null 
                        ? new Image(url) 
                        : new Image(inputStream);
            }
        };
    }

    @Override
    public Image get()
    {
        return getValue();
    }
}


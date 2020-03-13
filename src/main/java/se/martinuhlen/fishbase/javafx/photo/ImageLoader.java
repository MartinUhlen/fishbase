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
    private final Supplier<InputStream> inputStream;

    /**
     * Creates a new loader instance.
     * 
     * @param inputStream to read the image from
     * @param start {@code true} to {@link #start()} this loader immediately
     */
    ImageLoader(Supplier<InputStream> inputStream, boolean start)
    {
        this.inputStream = requireNonNull(inputStream, "inputStream cannott be null");
        if (start)
        {
            start();
        }
    }

    @Override
    protected Task<Image> createTask()
    {
        return new Task<>()
        {
            @Override
            protected Image call() throws Exception
            {
            	return new Image(requireNonNull(inputStream.get(), "inputStream cannott be null"));
            }
        };
    }

    @Override
    public Image get()
    {
        return getValue();
    }
}


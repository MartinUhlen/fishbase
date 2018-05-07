package se.martinuhlen.fishbase.javafx.photo;

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

    /**
     * Creates a new loader instance.
     * 
     * @param url of the image to load
     * @param start {@code true} to {@link #start()} this loader
     */
    ImageLoader(String url, boolean start)
    {
        this.url = url;
    }

    @Override
    protected Task<Image> createTask()
    {
        return new Task<>()
        {
            @Override
            protected Image call() throws Exception
            {
                return new Image(url);
            }
        };
    }

    @Override
    public Image get()
    {
        return getValue();
    }
}


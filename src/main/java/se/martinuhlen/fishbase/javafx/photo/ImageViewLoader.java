package se.martinuhlen.fishbase.javafx.photo;

import java.io.InputStream;
import java.util.function.Supplier;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Loads {@link Image} in background thread and {@link ImageView#setImage(Image) sets} it to {@link ImageView} on JavaFX thread when done.
 *
 * @author Martin
 */
class ImageViewLoader extends ImageLoader
{
    private final ImageView view;

    /**
     * Creates a new loader instance.
     * 
     * @param view whose image to load
     * @param inputStream to read the image from
     */
    ImageViewLoader(ImageView view, Supplier<InputStream> inputStream)
    {
        super(inputStream, false);
        this.view = view;
    }

    @Override
    protected void succeeded()
    {
        view.setImage(getValue());
    }
}

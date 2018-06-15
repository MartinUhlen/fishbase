package se.martinuhlen.fishbase.javafx.controls;

import javafx.scene.image.ImageView;

/**
 * A resizable {@link ImageView} which adapts it size to the size of its parent.
 * <p>
 * Code taken from:<br/>
 * {@code https://stackoverflow.com/questions/12630296/resizing-images-to-fit-the-parent-node}
 *
 * @author Martin
 */
public class ResizableImageView extends ImageView
{
    private static final double MIN_SIZE = 40;
    private static final double MAX_SIZE = 16384;

    @Override
    public double minWidth(double height)
    {
        return MIN_SIZE;
    }

    @Override
    public double prefWidth(double height)
    {
        return getImage() != null
                ? getImage().getWidth()
                : minWidth(height);
    }

    @Override
    public double maxWidth(double height)
    {
        return MAX_SIZE;
    }

    @Override
    public double minHeight(double width)
    {
        return MIN_SIZE;
    }

    @Override
    public double prefHeight(double width)
    {
        return getImage() != null
                ? getImage().getHeight()
                : minHeight(width);
    }

    @Override
    public double maxHeight(double width)
    {
        return MAX_SIZE;
    }

    @Override
    public boolean isResizable()
    {
        return true;
    }

    @Override
    public void resize(double width, double height)
    {
        setFitWidth(width);
        setFitHeight(height);
    }
}

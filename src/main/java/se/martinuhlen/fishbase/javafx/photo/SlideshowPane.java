package se.martinuhlen.fishbase.javafx.photo;

import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static javafx.geometry.Pos.CENTER;
import static se.martinuhlen.fishbase.javafx.utils.ImageSize.SIZE_16;
import static se.martinuhlen.fishbase.javafx.utils.ImageSize.SIZE_32;

import java.io.File;
import java.io.FileOutputStream;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import se.martinuhlen.fishbase.drive.photo.Photo;
import se.martinuhlen.fishbase.javafx.controls.ResizableImageView;
import se.martinuhlen.fishbase.javafx.utils.ImageSize;
import se.martinuhlen.fishbase.javafx.utils.Images;
import se.martinuhlen.fishbase.utils.Cursor;

public class SlideshowPane extends BorderPane
{
	private static final Supplier<Image> NULL_SUPPLIER = () -> null;

    private final ImageView imageView;
	private Supplier<Image> previousImage;
	private Supplier<Image> nextImage;
	private ImageViewLoader loader;

	private final VideoPane videoPane;

	private final Label statusLabel;
	private final Button firstButton;
	private final Button previousButton;
	private final Button nextButton;
	private final Button lastButton;
	private final VBox bottomBox;

	private Cursor<Photo> photos;
	private ImageSize buttonSize;

	private ObservableValue<? extends Number> fitWidthValue;
	private ObservableValue<? extends Number> fitHeightValue;

	/**
	 * Creates a new slideshow pane.
	 * 
	 * @param openStageOnClick {@code true} to open a new slideshow in a modal stage when this slideshow is clicked
	 */
	public SlideshowPane(boolean openStageOnClick)
	{
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        setCenter(imageView);

        videoPane = new VideoPane();

        firstButton = button("Show first photo", () -> showFirstPhoto());
        previousButton = button("Show previous photo", () -> showPreviousPhoto());
        nextButton = button("Show next photo", () -> showNextPhoto());
        lastButton = button("Show last photo", () -> showLastPhoto());
        heightProperty().addListener(obs -> setButtonIcons());
        HBox buttons = new HBox(firstButton, previousButton, nextButton, lastButton);
        buttons.setAlignment(CENTER);

        statusLabel = new Label();
        statusLabel.setAlignment(CENTER);
        statusLabel.prefWidthProperty().bind(widthProperty());

        bottomBox = new VBox(statusLabel, buttons);
        setBottom(bottomBox);

        if (openStageOnClick)
        {
            imageView.setOnMouseClicked(SlideshowStage.openOnClick(hasPhoto -> photos.copy()));
        }
	}

	/**
	 * Creates a new slideshow pane, passing {@code true} to {@link SlideshowPane#SlideshowPane(boolean)}.
	 */
	public SlideshowPane()
	{
	    this(true);
	}

	public void setButtonSize(ImageSize size)
	{
		this.buttonSize = size;
		setButtonIcons();
	}

	private void setButtonIcons()
	{
		ImageSize size = buttonSize != null ? buttonSize : (getHeight() < 600 ? SIZE_16 : SIZE_32);
		firstButton.setGraphic(getImage("navigate_beginning.png", size));
		previousButton.setGraphic(getImage("navigate_left.png", size));
		nextButton.setGraphic(getImage("navigate_right.png", size));
		lastButton.setGraphic(getImage("navigate_end.png", size));
	}

	private Node getImage(String name, ImageSize size)
	{
		return Images.getImageView(name, size);
	}

	private Button button(String tooltip, Runnable action)
	{
		Button button = new Button();
		button.setTooltip(new Tooltip(tooltip));
		button.setDisable(true);
		button.onActionProperty().set(e -> action.run());
		return button;
	}

	public void setPhotos(Cursor<Photo> photos)
	{
		this.photos = requireNonNull(photos);
        previousImage = NULL_SUPPLIER;
        nextImage = NULL_SUPPLIER;
        imageView.setImage(null);
        videoPane.disposeCurrentVideo();

		if (photos.hasCurrent())
		{
			showPhoto(photos.current(), NULL_SUPPLIER);
			preloadPrevious();
			preloadNext();
		}
		else
		{
			setCenter(null);
			updateState(null);
		}
	}

	private void showFirstPhoto()
	{
		previousImage = NULL_SUPPLIER;
		showPhoto(photos.first(), NULL_SUPPLIER);
		preloadNext();
	}

	private void showPreviousPhoto()
	{
		nextImage = currentImage();
		showPhoto(photos.previous(), previousImage);
		preloadPrevious();
	}

	private void showNextPhoto()
	{
		previousImage = currentImage();
		showPhoto(photos.next(), nextImage);
		preloadNext();
	}

	private Supplier<Image> currentImage()
	{
	    if (photos.current().isVideo())
	    {
	        return NULL_SUPPLIER;
	    }
	    else
	    {
	        return ofNullable(imageView.getImage())
	                .map(img -> (Supplier<Image>) () -> img)
	                .orElseGet(() -> new ImageLoader(photos.current().getContentUrl(), true));
	    }
	}

	private void showLastPhoto()
	{
		nextImage = NULL_SUPPLIER;
		showPhoto(photos.last(), NULL_SUPPLIER);
		preloadPrevious();
	}

	private void preloadPrevious()
	{
		previousImage = NULL_SUPPLIER;
		if (photos.hasPrevious())
		{
			previousImage = preload(photos.peekPrevious());
		}
	}

	private void preloadNext()
	{
		nextImage = NULL_SUPPLIER;
		if (photos.hasNext())
		{
			nextImage = preload(photos.peekNext());
		}
	}

    private Supplier<Image> preload(Photo photo)
    {
        if (photo.isVideo())
        {
            return NULL_SUPPLIER;
        }
        else
        {
            return new ImageLoader(photo.getContentUrl(), true);
        }
    }

	private void showPhoto(Photo photo, Supplier<Image> supplier)
	{
		if (photo.isVideo())
		{
			showVideo(photo);
		}
		else
		{
			videoPane.disposeCurrentVideo();
			showImage(photo, supplier);
		}
		updateState(photo);
	}

	private void showVideo(Photo photo)
	{
		videoPane.setVideo(null, false);
		setCenter(videoPane);
		new Service<Media>()
		{
			@Override
			protected Task<Media> createTask()
			{
				return new Task<>()
				{
					@Override
					protected Media call() throws Exception
					{
						File cacheDir = new File("/home/martin/.fishbase/cache/");
						cacheDir.mkdirs();
						File file = new File(cacheDir, photo.getName());
						if (!file.exists())
						{
							IOUtils.copy(photo.getContentStream(), new FileOutputStream(file));
						}
						return new Media(file.toURI().toURL().toExternalForm());
					}
				};
			}

			@Override
			protected void succeeded()
			{
				if (photos.current().equals(photo))
				{
					boolean autoPlay = getParent() != null;
					videoPane.setVideo(getValue(), autoPlay);
				}
			}
		}.start();
	}

	private void showImage(Photo photo, Supplier<Image> supplier)
	{
		setCenter(imageView);
		ofNullable(supplier.get())
		        .ifPresentOrElse(
		                img -> imageView.setImage(img),
		                () -> loadImage(photo));
	}

    private void loadImage(Photo photo)
    {
        if (loader != null)
        {
            loader.cancel();
        }
        loader = new ImageViewLoader(imageView, photo.getContentUrl());
        loader.start();
    }

	private void updateState(Photo photo)
	{
		statusLabel.setText(photo == null ? "" : getStatusText(photo));
		firstButton.setDisable(photos.isFirst() || photos.isEmpty());
		previousButton.setDisable(!photos.hasPrevious());
		nextButton.setDisable(!photos.hasNext());
		lastButton.setDisable(photos.isLast() || photos.isEmpty());
	}

	private String getStatusText(Photo photo)
	{
		return String.format("%s (%s/%s)",
				photo.getName(),
				(photos.currentIndex() + 1), photos.size());
	}

	/**
	 * Binds the size of this slideshow to fit within the bounds of given observable values.
	 *
	 * @param fitWidthValue maximum width to fit within
	 * @param fitHeightValue maximum height to fit within
	 */
	public void bindSizeToFitWithin(ObservableValue<? extends Number> fitWidthValue, ObservableValue<? extends Number> fitHeightValue)
	{
		this.fitWidthValue = fitWidthValue;
		this.fitHeightValue = fitHeightValue;
		imageView.fitWidthProperty().unbind();
		imageView.fitHeightProperty().unbind();
		minWidthProperty().unbind();
		minHeightProperty().unbind();
		prefWidthProperty().unbind();
		prefHeightProperty().unbind();
		maxWidthProperty().unbind();
		maxHeightProperty().unbind();
		setMinWidth(USE_PREF_SIZE);
		setMinHeight(USE_PREF_SIZE);
		setMaxWidth(USE_PREF_SIZE);
		setMaxHeight(USE_PREF_SIZE);

		InvalidationListener listener = obs -> fitSize();
		fitWidthValue.addListener(listener);
		fitHeightValue.addListener(listener);
		imageView.imageProperty().addListener(listener);
	}

	private void fitSize()
	{
		Image image = imageView.getImage();
		if (image == null)
		{
			return;
		}

		double imageWidth = image.getWidth();
		double imageHeight = image.getHeight();
		if (imageWidth <= 0 || imageHeight <= 0)
		{
			return;
		}

		double maxWidth = fitWidthValue.getValue().doubleValue();
		double maxHeight = fitHeightValue.getValue().doubleValue();
		double ratio = imageWidth / imageHeight;
		double minLength = min(maxWidth, maxHeight);
		double width = minLength * ratio;
		double height = minLength;
		setPrefWidth(width);
		setPrefHeight(height + bottomBox.getHeight());
		imageView.setFitWidth(width); // FIXME Needed with ResizableImageView?
		imageView.setFitHeight(0);    // FIXME Needed with ResizableImageView?
	}

	private class ImageView extends ResizableImageView implements HasPhoto
	{
        @Override
        public Photo getPhoto()
        {
            return photos.hasCurrent() ? photos.current() : null;
        }
    }
}

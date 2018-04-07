package se.martinuhlen.fishbase.javafx.photo;

import static java.util.Objects.requireNonNull;
import static javafx.geometry.Pos.CENTER;
import static se.martinuhlen.fishbase.javafx.utils.ImageSize.SIZE_16;
import static se.martinuhlen.fishbase.javafx.utils.ImageSize.SIZE_32;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.IOUtils;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import se.martinuhlen.fishbase.drive.photo.Photo;
import se.martinuhlen.fishbase.javafx.utils.ImageSize;
import se.martinuhlen.fishbase.javafx.utils.Images;
import se.martinuhlen.fishbase.utils.Cursor;

public class SlideshowPane extends BorderPane
{
	private final ImageView imageView;
	private Image previousImage;
	private Image nextImage;

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

	public SlideshowPane()
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

		imageView.fitHeightProperty().bind(heightProperty().subtract(bottomBox.heightProperty()));
		imageView.fitWidthProperty().bind(widthProperty());
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
		if (photos.hasCurrent())
		{
			showPhoto(photos.current(), null);
			preloadPrevious();
			preloadNext();
		}
		else
		{
			previousImage = null;
			nextImage = null;
			setCenter(null);
			updateState(null);
		}
	}

	private void showFirstPhoto()
	{
		previousImage = null;
		showPhoto(photos.first(), null);
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

	private Image currentImage()
	{
		return photos.current().isVideo() ? null : imageView.getImage();
	}

	private void showLastPhoto()
	{
		nextImage = null;
		showPhoto(photos.last(), null);
		preloadPrevious();
	}

	private void preloadPrevious()
	{
		previousImage = null;
		if (photos.hasPrevious())
		{
			previousImage = toImage(photos.peekPrevious());
		}
	}

	private void preloadNext()
	{
		nextImage = null;
		if (photos.hasNext())
		{
			nextImage = toImage(photos.peekNext());
		}
	}

	private void showPhoto(Photo photo, Image cachedImage)
	{
		if (photo.isVideo())
		{
			showVideo(photo);
		}
		else
		{
			videoPane.disposeCurrentVideo();
			showImage(photo, cachedImage);
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

	private void showImage(Photo photo, Image cachedImage)
	{
		setCenter(imageView);
		if (cachedImage == null)
		{
			imageView.setImage(toImage(photo));
		}
		else
		{
			imageView.setImage(cachedImage);
		}
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

	private Image toImage(Photo photo)
	{
		return photo.isVideo() ? null : new Image(photo.getContentUrl(), true);
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

		if (image.getProgress() < 1.0 && !image.isError())
		{
			InvalidationListener listener = new ImageLoadListener(image);
			image.progressProperty().addListener(listener);
			image.errorProperty().addListener(listener);
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
		double minLength = Math.min(maxWidth, maxHeight);
		double width = minLength * ratio;
		double height = minLength;
		setPrefWidth(width);
		setPrefHeight(height + bottomBox.getHeight());
		imageView.setFitWidth(width);
		imageView.setFitHeight(0);
	}

	private class ImageLoadListener implements InvalidationListener
	{
		private final Image image;

		ImageLoadListener(Image image)
		{
			this.image = image;
		}

		@Override
		public void invalidated(Observable obs)
		{
			if (image.getProgress() >= 1.0)
			{
				fitSize();
				removeListener();
			}
			else if (image.isError())
			{
				removeListener();
			}
		}

		private void removeListener()
		{
			image.progressProperty().removeListener(this);
			image.errorProperty().removeListener(this);
		}
	}
}

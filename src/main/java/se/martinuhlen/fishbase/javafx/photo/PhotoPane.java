package se.martinuhlen.fishbase.javafx.photo;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static javafx.scene.control.ButtonBar.setButtonUniformSize;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.OK;
import static se.martinuhlen.fishbase.javafx.utils.Images.getImageView16;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.SetChangeListener.Change;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;
import se.martinuhlen.fishbase.google.photos.FishingPhoto;
import se.martinuhlen.fishbase.google.photos.GooglePhoto;
import se.martinuhlen.fishbase.google.photos.PhotoService;
import se.martinuhlen.fishbase.utils.Cursor;

/**
 * A pane where photos of a {@link Trip} is managed.
 *
 * @author Martin
 */
public class PhotoPane extends BorderPane
{
	private final PhotoService service;
	private final ObservableValue<LocalDate> startDate;
	private final ObservableValue<LocalDate> endDate;
	private final ObservableValue<String> tripId;
	private final ObservableValue<List<Specimen>> specimens;
	private final ObservableList<FishingPhoto> fishingPhotos;

	private final Button addButton;
	private final Button removeButton;
	private final ToggleButton thumbnailToggle;
	private final ToggleButton slideShowToggle;

	private final ThumbnailPane thumbnailPane;
	private final SlideshowPane slideshowPane;
	private final AtomicBoolean syncing = new AtomicBoolean(false);

	public PhotoPane(
			PhotoService service, ObservableValue<LocalDate> startDate, ObservableValue<LocalDate> endDate,
			ObservableValue<String> tripId, ObservableValue<List<Specimen>> specimens, ObservableList<FishingPhoto> fishingPhotos)
	{
		this.service= service;
		this.startDate = startDate;
		this.endDate = endDate;
		this.tripId = tripId;
		this.specimens = specimens;
		this.fishingPhotos = fishingPhotos;

		thumbnailPane = ThumbnailPane.forTrip(() -> specimens.getValue().stream());
		slideshowPane = new SlideshowPane();
		slideshowPane.prefWidthProperty().bind(widthProperty());

		addButton = new Button("", getImageView16("add.png"));
		addButton.onActionProperty().set(e -> addPhotos());
		removeButton = new Button("", getImageView16("delete.png"));
		removeButton.disableProperty().bind(thumbnailPane.hasSelectedPhotos().not());
		removeButton.onActionProperty().set(e -> thumbnailPane.removeSelectedPhotos());

		thumbnailToggle = new ToggleButton("", getImageView16("photos.png"));
		slideShowToggle = new ToggleButton("", getImageView16("photo.png"));
		ToggleGroup toggleGroup = new ToggleGroup();
		thumbnailToggle.setToggleGroup(toggleGroup);
		slideShowToggle.setToggleGroup(toggleGroup);
		toggleGroup.selectToggle(thumbnailToggle);
		toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) ->
		{
			boolean thumbnail = toggleGroup.getSelectedToggle() == thumbnailToggle;
			Pane pane = thumbnail ? thumbnailPane : slideshowPane;
			if (pane != getCenter())
			{
    			setCenter(pane);
    			if (!thumbnail)
    			{
    			    setPhotosToSlideshow();
    			}
			}
		});

		thumbnailPane.prefWidthProperty().bind(widthProperty());
		thumbnailPane.prefHeightProperty().bind(heightProperty());
		slideshowPane.prefWidthProperty().bind(thumbnailPane.prefWidthProperty());
		slideshowPane.prefHeightProperty().bind(thumbnailPane.prefHeightProperty());
		slideshowPane.setMaxWidth(USE_PREF_SIZE);
		slideshowPane.setMaxHeight(USE_PREF_SIZE);

		initPhotoSync();
		initContextMenu();

		setTop(new HBox(addButton, removeButton, new Label("     "), thumbnailToggle, slideShowToggle));
		setCenter(thumbnailPane);
		setCenter(slideshowPane);
		setCenter(thumbnailPane);
	}

	// FIXME Such sophisticated sync is not needed anymore when FishingPhoto(Impl) is mutable?
	private void initPhotoSync()
	{
		fishingPhotos.addListener((Observable obs) ->
		{
			if (!syncing.get())
			{
				syncing.set(true);
				thumbnailPane.setPhotos(fishingPhotos);
				setPhotosToSlideshow();
				syncing.set(false);
			}
		});
		thumbnailPane.getPhotos().addListener((Observable obs) ->
		{
			if (!syncing.get())
			{
				syncing.set(true);
				List<FishingPhoto> photos = thumbnailPane.getPhotos().stream().map(FishingPhoto.class::cast).collect(toList());
				fishingPhotos.setAll(photos);
				setPhotosToSlideshow();
				syncing.set(false);
			}
		});
	}

	private void setPhotosToSlideshow()
    {
        if (getCenter() == slideshowPane)
        {
            slideshowPane.setPhotos(Cursor.of(thumbnailPane.getPhotos(), 0)); // FIXME Remember previous index, in case of specimen editing
        }
    }

    private void addPhotos()
	{
		// Step 1 — Waiting dialog: open browser + show progress while user picks in Google Photos
		Service<List<GooglePhoto>> pickService = new Service<>()
		{
			@Override
			protected Task<List<GooglePhoto>> createTask()
			{
				return new Task<>()
				{
					@Override
					protected List<GooglePhoto> call() throws Exception
					{
						return service.pick();
					}
				};
			}
		};

		DialogPane waitingPane = new DialogPane();
		waitingPane.getButtonTypes().setAll(CANCEL);
		Label waitLabel = new Label("Select photos in the Google Photos browser window that just opened.");
		ProgressIndicator waitProgress = new ProgressIndicator();
		waitingPane.setContent(new VBox(10, waitLabel, waitProgress));

		Dialog<ButtonType> waitDialog = new Dialog<>();
		waitDialog.setTitle("Add photos");
		waitDialog.setDialogPane(waitingPane);
		waitDialog.setResizable(false);

		pickService.setOnSucceeded(e ->
		{
			waitDialog.setResult(ButtonType.OK);
			waitDialog.close();
		});
		pickService.setOnFailed(e ->
		{
			waitDialog.setResult(ButtonType.CANCEL);
			waitDialog.close();
		});

		pickService.start();
		waitDialog.showAndWait();

		// If user cancelled, or pick failed/was interrupted, do nothing
		if (waitDialog.getResult() != ButtonType.OK)
		{
			pickService.cancel();
			return;
		}

		List<GooglePhoto> pickedPhotos = pickService.getValue();
		if (pickedPhotos == null || pickedPhotos.isEmpty())
		{
			return;
		}

		// Step 2 — Review dialog: show picked photos and let user confirm
		ThumbnailPane reviewPane = ThumbnailPane.forPicked(pickedPhotos);

		DialogPane reviewDialogPane = new DialogPane();
		reviewDialogPane.getButtonTypes().setAll(CANCEL, OK);
		reviewDialogPane.setContent(reviewPane);
		reviewDialogPane.setPrefWidth(Region.USE_COMPUTED_SIZE);

		SetChangeListener<GooglePhoto> listener = (Change<? extends GooglePhoto> change) ->
		{
			Button button = (Button) reviewDialogPane.lookupButton(OK);
			ObservableSet<GooglePhoto> photos = reviewPane.getSelectedPhotos();
			button.setDisable(photos.isEmpty());
			button.setText("Add " + (photos.isEmpty() ? "" : photos.size() + " ") + (photos.size() == 1 ? "photo" : "photos"));
			setButtonUniformSize(button, false);
		};
		reviewPane.getSelectedPhotos().addListener(listener);
		listener.onChanged(null);

		Dialog<ButtonType> reviewDialog = new Dialog<>();
		reviewDialog.setTitle("Add photos");
		reviewDialog.setDialogPane(reviewDialogPane);
		reviewDialog.setResizable(true);
		Rectangle2D screenSize = Screen.getPrimary().getBounds();
		reviewDialog.setWidth(screenSize.getWidth() * 0.90);
		reviewDialog.setHeight(screenSize.getHeight() * 0.90);
		reviewDialogPane.setPrefWidth(reviewDialog.getWidth());
		reviewDialogPane.setPrefHeight(reviewDialog.getHeight());

		reviewDialog.showAndWait()
			.filter(b -> b == OK)
			.ifPresent(b ->
			{
				List<FishingPhoto> newPhotos = reviewPane.getSelectedPhotos()
						.stream()
						.map(photo -> service.create(photo, tripId.getValue()))
						.collect(toList());
				thumbnailPane.addPhotos(newPhotos);
			});
	}

	private void initContextMenu()
	{
		ContextMenu contextMenu = new ContextMenu();
		RemovePhotoMenuItem removeItem = new RemovePhotoMenuItem();
		StarPhotoMenuItem starItem = new StarPhotoMenuItem();
		Menu specimenMenu = new Menu("Specimens");
		contextMenu.getItems().addAll(removeItem, starItem, specimenMenu);
		specimens.addListener((Observable obs) ->
		{
			specimenMenu.getItems().setAll(specimens.getValue()
					.stream()
					.map(specimen -> new SpecimenMenuItem(specimen))
					.collect(toList()));
		});

		thumbnailPane.setPhotoContextMenuHandler(e ->
		{
			HasPhoto hasPhoto = (HasPhoto) e.getSource();
			specimenMenu.setVisible(!specimenMenu.getItems().isEmpty());
			specimenMenu.getItems().stream().map(SpecimenMenuItem.class::cast).forEach(item -> item.setPhotoSource(hasPhoto));
			removeItem.setPhotoSource(hasPhoto);
			starItem.setPhotoSource(hasPhoto);
			contextMenu.show((Node) e.getSource(), e.getScreenX(), e.getScreenY());
		});
	}

	private class RemovePhotoMenuItem extends MenuItem implements PhotoMenuItem
	{
		private HasPhoto photoSource;

		RemovePhotoMenuItem()
		{
			super("Remove photo", getImageView16("delete.png"));
			setOnAction(e -> thumbnailPane.removePhoto(photoSource.getPhoto()));
		}

		@Override
		public void setPhotoSource(HasPhoto photoSource)
		{
			this.photoSource = photoSource;
		}
	}

    private class StarPhotoMenuItem extends MenuItem implements PhotoMenuItem
    {
        private HasPhoto photoSource;

        StarPhotoMenuItem()
        {
            super("Starred");
            setOnAction(e -> toggle());
        }

        @Override
        public void setPhotoSource(HasPhoto photoSource)
        {
            this.photoSource = photoSource;
            updateIcon();
        }

        private FishingPhoto getPhoto()
        {
            requireNonNull(photoSource, "photoSource must be set");
            return (FishingPhoto) requireNonNull(photoSource.getPhoto(), "photo cant be null");
        }

        private void toggle()
        {
        	FishingPhoto photo = getPhoto();
        	photo.setStarred(!photo.isStarred());
        	updateIcon();
        }

        private void updateIcon()
        {
            boolean starred = getPhoto().isStarred();
            setGraphic(getImageView16(starred ? "star_yellow.png" : "star_grey.png"));
        }
    }

	private class SpecimenMenuItem extends CheckMenuItem implements PhotoMenuItem
	{
		private final Specimen specimen;
		private HasPhoto photoSource;

		SpecimenMenuItem(Specimen specimen)
		{
			super(specimen.getLabel());
			this.specimen = specimen;
			setOnAction(e -> toggle());
		}

		@Override
		public void setPhotoSource(HasPhoto photoSource)
		{
			this.photoSource = photoSource;
			setSelected(getPhoto().containsSpecimen(specimen.getId()));
		}

		private FishingPhoto getPhoto()
		{
			requireNonNull(photoSource, "photoSource must be set");
			return (FishingPhoto) requireNonNull(photoSource.getPhoto(), "photo cant be null");
		}

		private void toggle()
		{
			if (isSelected())
			{
				getPhoto().addSpecimen(specimen.getId());
			}
			else
			{
				getPhoto().removeSpecimen(specimen.getId());
			}
		}
	}

	private interface PhotoMenuItem
	{
		void setPhotoSource(HasPhoto photoSource);
	}
}

package se.martinuhlen.fishbase.javafx.photo;

import static java.time.Month.JANUARY;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableSet;
import static javafx.collections.FXCollections.unmodifiableObservableSet;
import static javafx.concurrent.Worker.State.RUNNING;
import static javafx.geometry.NodeOrientation.LEFT_TO_RIGHT;
import static javafx.geometry.NodeOrientation.RIGHT_TO_LEFT;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.geometry.Pos.TOP_LEFT;
import static javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED;
import static javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static se.martinuhlen.fishbase.utils.Constants.DATE_TIME_FORMAT;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener.Change;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.util.StringConverter;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;
import se.martinuhlen.fishbase.drive.photo.FishingPhoto;
import se.martinuhlen.fishbase.drive.photo.Photo;
import se.martinuhlen.fishbase.utils.Cursor;

public class ThumbnailPane extends BorderPane
{
	public static ThumbnailPane forTimeline(Function<String, Trip> tripLoader)
	{
		Function<Photo, String> tooltipFunction = photo ->
		{
            FishingPhoto p = (FishingPhoto) photo;
            Trip trip = tripLoader.apply(p.getTripId());
            return trip.getDescription()
                +  trip.getSpecimens()
                        .stream()
                        .filter(s -> p.containsSpecimen(s.getId()))
                        .map(s -> s.getLabel() + " " + s.getMethod() + "/" + s.getBait() + " " + s.getLocation())
                        .collect(joining("\n", "\n", ""));
		};
		return new ThumbnailPane(false, false, true, tooltipFunction);
	}

	public static ThumbnailPane forTrip(Supplier<Stream<Specimen>> specimens)
	{
		Function<Photo, String> tooltipFunction = photo ->
		{
            FishingPhoto p = (FishingPhoto) photo;
            return specimens.get()
                        .filter(s -> p.containsSpecimen(s.getId()))
                        .map(Specimen::getLabel)
                        .collect(joining("\n", "", ""));
		};
		return new ThumbnailPane(true, true, false, tooltipFunction);
	}

	public static ThumbnailPane forAdding(String initialSearch, Function<String, Collection<Photo>> searcher)
	{
		ThumbnailPane pane = new ThumbnailPane(true, false, false, null);
		TextField searchField = new TextField();
		ProgressIndicator progress = new ProgressIndicator();
		progress.setVisible(false);
		progress.managedProperty().bind(progress.visibleProperty());
		progress.maxHeightProperty().bind(searchField.heightProperty());
		progress.maxWidthProperty().bind(progress.maxHeightProperty());

		Service<Collection<Photo>> service = new Service<>()
		{
			@Override
			protected Task<Collection<Photo>> createTask()
			{
				String searchText = searchField.getText();
				return new Task<>()
				{
					@Override
					protected Collection<Photo> call() throws Exception
					{
						return searcher.apply(searchText);
					}
				};
			}
		};
		service.setOnScheduled(e -> pane.setPhotos(emptySet()));
		service.stateProperty().addListener(obs -> progress.setVisible(service.getState() == RUNNING));
		service.setOnSucceeded(e ->
		{
		    pane.setPhotos(service.getValue());
		    pane.selectAll(true);
		});

		Label infoLabel = new Label("");
		infoLabel.setAlignment(Pos.CENTER_LEFT);
		infoLabel.visibleProperty().bind(progress.visibleProperty().not());
		infoLabel.managedProperty().bind(infoLabel.visibleProperty());
		pane.photos.addListener((Observable obs) -> infoLabel.setText("Found " + pane.photos.size() + " photos"));

		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(250), e -> service.restart()));
		searchField.textProperty().addListener(obs -> timeline.playFromStart());
		searchField.setText(initialSearch);

		pane.setTop(new FlowPane(10, 10, searchField, infoLabel, progress));
		return pane;
	}

	private final ScrollPane scroll;
	private final FlowPane photoPane;
	private final ThumbnailLoader imageLoader;
	private final boolean thumbnailsAreSelectable;
	private final Comparator<LocalDateTime> photoTimeComparator;
	private final Comparator<Photo> photoComparator;

	private final ObservableSet<Photo> photos;
	private final ObservableSet<Photo> unmodifiablePhotos;

	private final ObservableSet<Photo> selectedPhotos = observableSet();
	private final ObservableSet<Photo> unmodifiableSelectedPhotos = unmodifiableObservableSet(selectedPhotos);
	private ReadOnlyBooleanProperty hasSelectedPhotos;

	private final EventHandler<MouseEvent> slideshowHandler = SlideshowStage.openOnClick(this::createCursor);

	private EventHandler<? super ContextMenuEvent> contextMenuHandler;
	private Function<Photo, String> tooltipFunction;

	private ThumbnailPane(boolean thumbnailsAreSelectable, boolean ascendingOrder, boolean showDateSlider, Function<Photo, String> tooltipFunction)
	{
		this.thumbnailsAreSelectable = thumbnailsAreSelectable;
		this.tooltipFunction = tooltipFunction;
		photoTimeComparator = ascendingOrder ? naturalOrder() : reverseOrder();
		photoComparator = comparing(Photo::getTime, photoTimeComparator).thenComparing(Photo::getId); // For the rare event that two photos has exactly the same time
		photos = observableSet(new TreeSet<>(photoComparator));
		unmodifiablePhotos = unmodifiableObservableSet(photos);
		photoPane = new FlowPane();
		photoPane.setHgap(20);
		photoPane.setVgap(0);

		scroll = new ScrollPane(photoPane);
		scroll.setVbarPolicy(showDateSlider ? NEVER : AS_NEEDED);

		photoPane.prefWidthProperty().bind(scroll.widthProperty().subtract(20));

		imageLoader = new ThumbnailLoader();
		scroll.vvalueProperty().addListener(imageLoader);
		photoPane.heightProperty().addListener(imageLoader);

		setCenter(scroll);
		setRight(createSlider(ascendingOrder, showDateSlider));
	}

	private Node createSlider(boolean ascendingOrder, boolean showDateSlider)
	{
		if (!showDateSlider)
		{
			return null;
		}

		int currentYear = Year.now().getValue();
		int nextYear = currentYear + 1;
		Slider slider = new Slider(nextYear - 5, nextYear, nextYear);
		slider.setOrientation(VERTICAL);
		slider.setShowTickMarks(true);
		slider.setShowTickLabels(true);
		slider.setMajorTickUnit(1);
		slider.setMinorTickCount(12);
		slider.setNodeOrientation(ascendingOrder ? LEFT_TO_RIGHT : RIGHT_TO_LEFT);
		slider.setVisible(false);
		slider.managedProperty().bind(slider.visibleProperty());
		slider.setLabelFormatter(new StringConverter<>()
		{
			@Override
			public String toString(Double val)
			{
				int year = val.intValue();
				return String.valueOf(year);
			}

			@Override
			public Double fromString(String arg0)
			{
				throw new IllegalStateException("");
			}
		});

		photos.addListener((Observable obs) ->
		{
			slider.setVisible(!photos.isEmpty());
			if (!photos.isEmpty())
			{
				LongSummaryStatistics stats = photos.stream()
					.map(Photo::getTime)
					.map(LocalDateTime::toLocalDate)
					.mapToLong(LocalDate::toEpochDay)
					.summaryStatistics();
				LocalDate minDate = LocalDate.ofEpochDay(stats.getMin());
				LocalDate maxDate = LocalDate.ofEpochDay(stats.getMax());
				slider.setMin(minDate.getYear());
				slider.setMax(maxDate.getYear() + 1);
			}
		});

		AtomicBoolean syncing = new AtomicBoolean(false);
		Timeline sliderToScroll = new Timeline(new KeyFrame(Duration.millis(50), e ->
		{
			if (!syncing.get())
			{
				syncing.set(true);
				double value = slider.getValue();
				int year = (int) value;
				double remainder = value - year;
				int days = (int) (365.0 * remainder);
				LocalDateTime date = LocalDate.of(year, JANUARY, 1).plusDays(days).atStartOfDay();
				photoPane.getChildren().stream()
					.map(Thumbnail.class::cast)
					.dropWhile(t -> photoTimeComparator.compare(date, t.photo.getTime()) >= 0)
					.findFirst()
					.map(t -> t.getBoundsInParent().getMinY() / photoPane.getHeight())
					.or(() -> Optional.of(1.0))
					.ifPresent(val -> scroll.vvalueProperty().set(val));
				syncing.set(false);
			}
		}));
		slider.valueProperty().addListener(obs ->
		{
			if (!syncing.get())
			{
				sliderToScroll.playFromStart();
			}
		});

		Timeline scrollToSlider = new Timeline(new KeyFrame(Duration.millis(50), e ->
		{
			if (!syncing.get())
			{
				syncing.set(true);
				streamVisibleThumbnails()
					.findFirst()
					.map(t -> t.photo)
					.ifPresent(photo ->
					{
						double year = photo.getTime().getYear();
						double days = photo.getTime().getDayOfYear() / 365.0;
						slider.setValue(year + days);
					});
				syncing.set(false);
			}
		}));
		InvalidationListener scrollToSliderListener = obs ->
		{
			if (!syncing.get())
			{
				scrollToSlider.playFromStart();
			}
		};
		scroll.vvalueProperty().addListener(scrollToSliderListener);
		photoPane.heightProperty().addListener(scrollToSliderListener);

		return slider;
	}

	/**
	 * Gets the comparator in which photos of this pane is sorted.
	 * 
	 * @return photo comparator
	 */
	public Comparator<Photo> getPhotoComparator()
	{
	    return photoComparator;
	}

	/**
	 * Adds new photos to this pane.
	 * 
	 * @param photos to add
	 */
	public void addPhotos(Collection<? extends Photo> photos)
	{
        requireNonNull(photos, "photos cannot be null");

        NavigableSet<Photo> sortedPhotos = new TreeSet<>(photoComparator);
        sortedPhotos.addAll(photos);
        sortedPhotos.removeAll(this.photos);
        this.photos.addAll(photos);

        while (!sortedPhotos.isEmpty())
        {
            Optional<Thumbnail> insertionThumbnail = streamThumbnails().filter(t -> photoComparator.compare(t.photo, sortedPhotos.first()) > 0).findFirst();
            List<Photo> photosToAdd = new ArrayList<>(photos.size());
            Iterator<Photo> iterator = sortedPhotos.iterator();
            while(iterator.hasNext() && (!insertionThumbnail.isPresent() || photoComparator.compare(insertionThumbnail.get().photo, sortedPhotos.first()) > 0))
            {
                photosToAdd.add(iterator.next());
                iterator.remove();
            }
            int index = insertionThumbnail.map(t -> photoPane.getChildren().indexOf(t)).orElse(photoPane.getChildren().size());
            addThumbnails(index, photosToAdd);
        }
        runLater(() -> imageLoader.loadVisibleThumbnails());
	}

	public void setPhotos(Collection<? extends Photo> photos)
	{
		requireNonNull(photos, "photos cannot be null");
		this.photos.clear();
		this.photos.addAll(photos);
		selectedPhotos.clear();
		photoPane.getChildren().clear();
		addThumbnails(0, this.photos);
	}

	public void updatePhoto(Photo oldPhoto, Photo newPhoto)
	{
		if (!oldPhoto.getId().equals(newPhoto.getId()))
		{
			throw new IllegalArgumentException("New photo must have equal ID to old photo");
		}
		Thumbnail thumbnail = streamThumbnails()
				.filter(t -> t.getPhoto().equals(oldPhoto))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("Old photo not found"));

		thumbnail.setPhoto(newPhoto);
		photos.remove(oldPhoto);
		photos.add(newPhoto);
	}

	ObservableSet<Photo> getPhotos()
	{
		return unmodifiablePhotos;
	}

	ObservableSet<Photo> getSelectedPhotos()
	{
		return unmodifiableSelectedPhotos;
	}

	ReadOnlyBooleanProperty hasSelectedPhotos()
	{
		if (hasSelectedPhotos == null)
		{
			ReadOnlyBooleanWrapper wrapper = new ReadOnlyBooleanWrapper(!getSelectedPhotos().isEmpty());
			selectedPhotos.addListener((Change<?> change) -> wrapper.set(!getSelectedPhotos().isEmpty()));
			hasSelectedPhotos = wrapper.getReadOnlyProperty();
		}
		return hasSelectedPhotos;
	}

	void removeSelectedPhotos()
	{
		removePhotos(new HashSet<>(selectedPhotos));
	}

	void removePhoto(Photo photo)
	{
		removePhotos(singleton(photo));
	}

	void removePhotos(Collection<Photo> photosToRemove)
	{
		Collection<Thumbnail> thumbnailsToRemove = streamThumbnails()
				.filter(t -> photosToRemove.contains(t.getPhoto()))
				.collect(toList());

		photoPane.getChildren().removeAll(thumbnailsToRemove);
		photos.removeAll(photosToRemove);
		selectedPhotos.removeAll(photosToRemove);
		setThumbnailsType();
	}

	private void addThumbnails(int index, Collection<? extends Photo> photosToAdd)
	{
		List<Thumbnail> thumbnails = photosToAdd.stream().map(this::createThumbnail).collect(toList());
		photoPane.getChildren().addAll(index, thumbnails);
		imageLoader.loadVisibleThumbnails();
		setThumbnailsType();
	}

	private Thumbnail createThumbnail(Photo photo)
	{
		Thumbnail thumbnail = new Thumbnail(photo,
				s -> onPhotoSelected(photo, s),
				s -> selectAll(photo.getTime().toLocalDate(), s));
		thumbnail.onMouseClickedProperty().set(slideshowHandler);
		thumbnail.setOnContextMenuRequested(contextMenuHandler);
		if (tooltipFunction != null)
		{
    		thumbnail.imageView.setOnMouseEntered(e ->
            {
                String tooltipText = tooltipFunction.apply(thumbnail.photo);
                if (isNotBlank(tooltipText))
                {
                	Tooltip.install(thumbnail.imageView, new Tooltip(tooltipText));
                }
                thumbnail.imageView.setOnMouseEntered(null);
            });
		}
		return thumbnail;
	}

	private void setThumbnailsType()
	{
        if (thumbnailsAreSelectable)
        {
            LocalDate previousDay = LocalDate.MIN;
            LocalDate currentDay = previousDay;
            for (Thumbnail t : streamThumbnails().collect(toList()))
            {
                previousDay = currentDay;
                currentDay = t.getPhoto().getTime().toLocalDate();
                ThumbnailType type = currentDay.equals(previousDay) ? ThumbnailType.SELECTABLE : ThumbnailType.DAY_SELECTABLE;
                t.setType(type);
            }
        }
	}

	private void selectAll(boolean selected)
	{
		streamThumbnails().forEach(t -> t.select(selected));
	}

	private void selectAll(LocalDate date, boolean selected)
	{
		streamThumbnails(date).forEach(t -> t.select(selected));
	}

	private void onPhotoSelected(Photo photo, boolean selected)
	{
		List<Thumbnail> thumbnails = streamThumbnails(photo.getTime().toLocalDate()).collect(toList());
		boolean allSelected = thumbnails.stream().allMatch(t -> t.isSelected());
		Thumbnail t = thumbnails.get(0);
		t.selectDaySilently(allSelected);
		if (selected)
		{
			selectedPhotos.add(photo);
		}
		else
		{
			selectedPhotos.remove(photo);
		}
	}

	private Stream<Thumbnail> streamThumbnails(LocalDate date)
	{
		return streamThumbnails()
				.filter(t -> t.photo.getTime().toLocalDate().equals(date));
	}

	private Stream<Thumbnail> streamThumbnails()
	{
		return photoPane
				.getChildren()
				.stream()
				.map(Thumbnail.class::cast);
	}

    private Cursor<Photo> createCursor(HasPhoto hasPhoto)
    {
        List<Photo> photoList = new ArrayList<>(photos);
        int index = photoList.indexOf(hasPhoto.getPhoto());
        return Cursor.of(photoList, index);
    }

	/**
	 * Sets handler for context menu on photo thumbnails.
	 * <p>
	 * The event source passed to the handler implements {@link HasPhoto}.
	 * 
	 * @param contextMenuHandler handler for context menu, or {@code null}
	 */
	public void setPhotoContextMenuHandler(EventHandler<? super ContextMenuEvent> contextMenuHandler)
	{
		this.contextMenuHandler = contextMenuHandler;
	}

	/**
	 * Sets a function that produces photo tooltips, {@code null} is returned when thumbnail should have no no tooltip.
	 * 
	 * @param tooltipFunction photo tooltip function or {@code null} for no tooltips
	 */
	public void setTooltipFunction(Function<Photo, String> tooltipFunction)
	{
	    this.tooltipFunction = tooltipFunction;
	}

	private static class Thumbnail extends BorderPane implements HasPhoto
	{
		private static final double LABEL_HEIGHT = 16;
		private static final double IMAGE_HEIGHT = 150;
		private static final double TOTAL_HEIGHT = LABEL_HEIGHT + IMAGE_HEIGHT + LABEL_HEIGHT;

		private final CheckBox dayCheckBox;
		private final Label header;
		private final ImageView imageView;
		private final CheckBox imageCheckBox;
		private final Label footer;

		private final Consumer<Boolean> onImageSelection;
		private final Consumer<Boolean> onDaySelection;

		private Photo photo;
		private ThumbnailType type;
		private boolean imageLoaded;
		private boolean selectDaySilently;

		Thumbnail(Photo p, Consumer<Boolean> onImageSelection, Consumer<Boolean> onDaySelection)
		{
			this.onImageSelection = onImageSelection;
			this.onDaySelection = onDaySelection;
			this.dayCheckBox = new CheckBox();
			this.dayCheckBox.setVisible(false);
			this.dayCheckBox.managedProperty().bind(dayCheckBox.visibleProperty());
			this.dayCheckBox.selectedProperty().addListener(obs -> onDaySelected());
			this.header = new Label("");
			this.imageView = new ImageView();
			this.imageView.setFitHeight(150);
			this.imageView.setPreserveRatio(true);
			this.imageCheckBox = new CheckBox();
			this.imageCheckBox.setVisible(false);
			this.imageCheckBox.selectedProperty().addListener(obs -> this.onImageSelection.accept(imageCheckBox.isSelected()));
			this.footer = new Label();

			setMinHeight(USE_PREF_SIZE);
			setPrefHeight(TOTAL_HEIGHT);
			setMaxHeight(USE_PREF_SIZE);

			setTop(new HBox(dayCheckBox, header));
			StackPane.setAlignment(imageCheckBox, TOP_LEFT);
			setCenter(new StackPane(imageView, imageCheckBox));
			setBottom(footer);

			setPhoto(p);
			setType(ThumbnailType.SIMPLE);
		}

		void loadImage()
		{
			if (!imageLoaded)
			{
				new ImageViewLoader(imageView, photo.getThumbnail().getStream()).start();
				imageLoaded = true;
			}
		}

		@Override
		public Photo getPhoto()
		{
			return photo;
		}

		void setPhoto(Photo photo)
		{
			this.photo = photo;
			footer.setText(photo.getTime().format(DATE_TIME_FORMAT));
			footer.setTooltip(new Tooltip(photo.getName()));
		}

		void setType(ThumbnailType type)
		{
		    if (this.type == type)
		    {
		        return;
		    }
			this.type = type;
			if (type == ThumbnailType.DAY_SELECTABLE)
			{
			    header.setText(photo.getTime().toLocalDate().toString());
			    header.setOnMouseClicked(e ->
			    {
			        dayCheckBox.fire();
			        e.consume();
			    });
			}
			else
			{
			    header.setText("");
			    header.setOnMouseClicked(null);
			}
			if (type.isSelectable())
			{
				setOnMouseEntered(e -> mouseEntered());
				setOnMouseExited(e -> mouseExited());
			}
			else
			{
				setOnMouseEntered(null);
				setOnMouseExited(null);
			}
			mouseExited();
		}

		protected void mouseEntered()
		{
			dayCheckBox.setVisible(type == ThumbnailType.DAY_SELECTABLE);
			imageCheckBox.setVisible(true);
		}

		protected void mouseExited()
		{
			dayCheckBox.setVisible(dayCheckBox.isSelected());
			imageCheckBox.setVisible(imageCheckBox.isSelected());
		}

		private void onDaySelected()
		{
			if (!selectDaySilently)
			{
				onDaySelection.accept(dayCheckBox.isSelected());
			}
		}

		void selectDaySilently(boolean selected)
		{
			selectDaySilently = true;
			dayCheckBox.setSelected(selected);
			dayCheckBox.setVisible(dayCheckBox.isVisible() || selected);
			selectDaySilently = false;
		}

		void select(boolean selected)
		{
			imageCheckBox.setSelected(selected);
			imageCheckBox.setVisible(selected);
		}

		boolean isSelected()
		{
			return imageCheckBox.isSelected();
		}
	}

	private enum ThumbnailType
	{
		SIMPLE(false),
		SELECTABLE(true),
		DAY_SELECTABLE(true);

		private final boolean selectable;

		private ThumbnailType(boolean selectable)
		{
			this.selectable = selectable;
		}

		boolean isSelectable()
		{
			return selectable;
		}
	}

	private class ThumbnailLoader implements InvalidationListener
	{
		private final Timeline timeline;

		ThumbnailLoader()
		{
			timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> loadVisibleThumbnails()));
		}

		@Override
		public void invalidated(Observable obs)
		{
			timeline.playFromStart();
		}

		void loadVisibleThumbnails()
		{
			streamVisibleThumbnails().forEach(Thumbnail::loadImage);
		}
	}

	private Stream<Thumbnail> streamVisibleThumbnails()
	{
		Bounds viewportBounds = scroll.getViewportBounds();
		if (Double.isNaN(scroll.getVvalue()))
		{
			scroll.setVvalue(0);
		}
		double top = scroll.getVvalue() * (photoPane.getHeight() - viewportBounds.getHeight());
		BoundingBox scrollBounds = new BoundingBox(0, top, viewportBounds.getWidth(), viewportBounds.getHeight());
		return photoPane.getChildren().stream()
				.filter(node -> node.getBoundsInParent().getWidth() > 0)
				.filter(node -> node.getBoundsInParent().intersects(scrollBounds))
				.map(Thumbnail.class::cast);
	}
}

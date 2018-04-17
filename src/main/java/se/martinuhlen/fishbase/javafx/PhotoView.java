package se.martinuhlen.fishbase.javafx;

import static java.lang.Math.min;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import static se.martinuhlen.fishbase.javafx.utils.Images.getImageView16;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import se.martinuhlen.fishbase.dao.FishBaseDao;
import se.martinuhlen.fishbase.domain.Trip;
import se.martinuhlen.fishbase.drive.photo.FishingPhoto;
import se.martinuhlen.fishbase.drive.photo.PhotoService;
import se.martinuhlen.fishbase.javafx.action.Action;
import se.martinuhlen.fishbase.javafx.action.RunnableAction;
import se.martinuhlen.fishbase.javafx.photo.HasPhoto;
import se.martinuhlen.fishbase.javafx.photo.ThumbnailPane;

class PhotoView implements View
{
	private final ReadOnlyStringProperty titleProperty = new ReadOnlyStringWrapper("Photos").getReadOnlyProperty();
	private final RunnableAction refreshAction = new RunnableAction(true, () -> refresh());

	private final PhotoService service;
	private final FishBaseDao dao;
	private final Consumer<String> tripOpener;
	private final StackPane stackPane;
	private final ThumbnailPane thumbnailPane;
	private final ProgressIndicator progressIndicator;
	private final PhotoLoader photoLoader;

	PhotoView(PhotoService service, FishBaseDao dao, Consumer<String> tripOpener)
	{
		this.service = service;
        this.dao = dao;
        this.tripOpener = tripOpener;
		this.thumbnailPane = createThumbnailPane();
		this.progressIndicator = new ProgressIndicator();
		this.stackPane = new StackPane(thumbnailPane, progressIndicator);
		this.photoLoader = new PhotoLoader();
	}

    private ThumbnailPane createThumbnailPane()
    {
        ThumbnailPane pane = ThumbnailPane.forTimeline();
        pane.setTooltipFunction(photo ->
        {
            FishingPhoto p = (FishingPhoto) photo;
            Trip trip = dao.getTrip(p.getTripId());
            return trip.getDescription()
                +  trip.getSpecimens()
                        .stream()
                        .filter(s -> p.containsSpecimen(s.getId()))
                        .map(s -> s.getLabel() + " " + s.getMethod() + "/" + s.getBait() + " " + s.getLocation())
                        .collect(joining("\n", "\n", ""));
        });

        MenuItem openTrip = new MenuItem("Open trip", getImageView16("window_next.png"));
        openTrip.setOnAction(e -> tripOpener.accept(openTrip.getUserData().toString()));
        ContextMenu contextMenu = new ContextMenu(openTrip);
        pane.setPhotoContextMenuHandler(e ->
        {
            HasPhoto owner = (HasPhoto) e.getSource();
            FishingPhoto photo = (FishingPhoto) owner.getPhoto();
            openTrip.setUserData(photo.getTripId());
            contextMenu.show((Node) e.getSource(), e.getScreenX(), e.getScreenY());
        });
        return pane;
    }

	@Override
	public Node getContent()
	{
	    progressIndicator.prefHeightProperty().bind(stackPane.heightProperty().multiply(0.3));
	    progressIndicator.prefWidthProperty().bind(progressIndicator.heightProperty());
	    progressIndicator.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
		return stackPane;
	}

	@Override
	public Action refreshAction()
	{
		return refreshAction;
	}

	private void refresh()
	{
	    photoLoader.restart();
	}

	@Override
	public ReadOnlyStringProperty titleProperty()
	{
		return titleProperty;
	}

	private class PhotoLoader extends Service<List<FishingPhoto>>
	{
	    private final Timeline batchTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> addNextBatchOfPhotos()));
	    private List<FishingPhoto> loadedPhotos = emptyList();

        @Override
        protected Task<List<FishingPhoto>> createTask()
        {
            loadedPhotos = emptyList();
            thumbnailPane.setPhotos(emptySet());
            progressIndicator.setVisible(true);
            return new Task<>()
            {
                @Override
                protected List<FishingPhoto> call() throws Exception
                {
                    List<FishingPhoto> photos = service.getFishingPhotos();
                    photos.sort(thumbnailPane.getPhotoComparator());
                    return photos;
                }
            };
        }

        @Override
        protected void succeeded()
        {
            progressIndicator.setVisible(false);
            loadedPhotos = getValue();
            addNextBatchOfPhotos();
        }

        private void addNextBatchOfPhotos()
        {
            if (!loadedPhotos.isEmpty())
            {
                List<FishingPhoto> subList = loadedPhotos.subList(0, min(100, loadedPhotos.size()));
                List<FishingPhoto> photos = new ArrayList<>(subList);
                thumbnailPane.addPhotos(photos);
                subList.clear();
                batchTimeline.playFromStart();
            }
        }
	}
}

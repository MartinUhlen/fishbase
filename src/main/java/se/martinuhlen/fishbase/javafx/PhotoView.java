package se.martinuhlen.fishbase.javafx;

import static java.lang.Math.min;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static javafx.scene.layout.Region.USE_PREF_SIZE;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import se.martinuhlen.fishbase.drive.photo.FishingPhoto;
import se.martinuhlen.fishbase.drive.photo.PhotoService;
import se.martinuhlen.fishbase.javafx.action.Action;
import se.martinuhlen.fishbase.javafx.action.RunnableAction;
import se.martinuhlen.fishbase.javafx.photo.ThumbnailPane;

public class PhotoView implements View
{
	private final ReadOnlyStringProperty titleProperty = new ReadOnlyStringWrapper("Photos").getReadOnlyProperty();
	private final RunnableAction refreshAction = new RunnableAction(true, () -> refresh());

	private final PhotoService service;
	private final StackPane stackPane;
	private final ThumbnailPane thumbnailPane;
	private final ProgressIndicator progressIndicator;
	private final PhotoLoader photoLoader;

	public PhotoView(PhotoService service)
	{
		this.service = service;
		this.thumbnailPane = ThumbnailPane.forTimeline();
		this.progressIndicator = new ProgressIndicator();
		this.stackPane = new StackPane(thumbnailPane, progressIndicator);
		this.photoLoader = new PhotoLoader();
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
                thumbnailPane.appendPhotos(photos);
                subList.clear();
                batchTimeline.playFromStart();
            }
        }
	}
}

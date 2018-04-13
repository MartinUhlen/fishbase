package se.martinuhlen.fishbase.javafx;

import static java.util.Collections.emptySet;
import static javafx.scene.layout.Region.USE_PREF_SIZE;

import java.util.List;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
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
        @Override
        protected Task<List<FishingPhoto>> createTask()
        {
            thumbnailPane.setPhotos(emptySet());
            progressIndicator.setVisible(true);
            return new Task<>()
            {
                @Override
                protected List<FishingPhoto> call() throws Exception
                {
                    return service.getFishingPhotos();
                }
            };
        }

        @Override
        protected void succeeded()
        {
            progressIndicator.setVisible(false);
            thumbnailPane.setPhotos(getValue());
        }
	}
}

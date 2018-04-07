package se.martinuhlen.fishbase.javafx;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Node;
import se.martinuhlen.fishbase.drive.photo.PhotoService;
import se.martinuhlen.fishbase.javafx.action.Action;
import se.martinuhlen.fishbase.javafx.action.RunnableAction;
import se.martinuhlen.fishbase.javafx.photo.ThumbnailPane;

public class PhotoView implements View
{
	private final ReadOnlyStringProperty titleProperty = new ReadOnlyStringWrapper("Photos").getReadOnlyProperty();
	private final RunnableAction refreshAction = new RunnableAction(true, () -> refresh());

	private final PhotoService service;
	private final ThumbnailPane thumbnailPane;

	public PhotoView(PhotoService service)
	{
		this.service = service;
		this.thumbnailPane = ThumbnailPane.forTimeline();
	}

	@Override
	public Node getContent()
	{
		return thumbnailPane;
	}

	@Override
	public Action refreshAction()
	{
		return refreshAction;
	}

	private void refresh()
	{
		thumbnailPane.setPhotos(service.getFishingPhotos());
	}

	@Override
	public ReadOnlyStringProperty titleProperty()
	{
		return titleProperty;
	}
}

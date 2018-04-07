package se.martinuhlen.fishbase.javafx;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static javafx.geometry.Pos.BOTTOM_RIGHT;
import static se.martinuhlen.fishbase.javafx.utils.ImageSize.SIZE_16;
import static se.martinuhlen.fishbase.utils.EmptyCursor.emptyCursor;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import se.martinuhlen.fishbase.dao.FishBaseDao;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.drive.photo.FishingPhoto;
import se.martinuhlen.fishbase.drive.photo.Photo;
import se.martinuhlen.fishbase.drive.photo.PhotoService;
import se.martinuhlen.fishbase.filter.SpecimenTextPredicate;
import se.martinuhlen.fishbase.javafx.data.SpecimenWrapper;
import se.martinuhlen.fishbase.javafx.photo.SlideshowPane;
import se.martinuhlen.fishbase.utils.Cursor;

class SpecimenView extends AbstractTableView<SpecimenWrapper, Specimen>
{
	private final FishBaseDao dao;
	private final PhotoService photoService;
	private final SlideshowPane slideshow;
	private final PhotoLoader photoLoader;
	private final Consumer<String> tripOpener;

	SpecimenView(FishBaseDao dao, PhotoService photoService, Consumer<String> tripOpener)
	{
		super("Specimens", dao::getSpecimens, dao::saveSpecimens, dao::deleteSpecimens);
		this.dao = dao;
		this.photoService = photoService;
		this.tripOpener = tripOpener;
		this.slideshow = new SlideshowPane();
		photoLoader = new PhotoLoader();
	}

	@Override
	TableView<SpecimenWrapper> createTable(FilteredList<SpecimenWrapper> filteredList)
	{
		SpecimenTable specimenTable = new SpecimenTable(filteredList, dao.getSpecies());
		specimenTable.getSelectionModel().selectedItemProperty().addListener(obs -> photoLoader.restart());
		specimenTable.setContextMenu(createContextMenu());
		return specimenTable;
	}

	private ContextMenu createContextMenu()
	{
		ContextMenu menu = new ContextMenu();
		MenuItem openTrip = new MenuItem("Open trip");
		menu.getItems().add(openTrip);
		openTrip.setOnAction(e -> tripOpener.accept(getTable().getSelectionModel().getSelectedItem().getWrapee().getTripId()));
		menu.setOnShowing(e -> openTrip.setDisable(getTable().getSelectionModel().isEmpty()));
		return menu;
	}

	@Override
	Node getTableNode()
	{
		StackPane stackPane = new StackPane(getTable(), slideshow);
		slideshow.setVisible(false);
		slideshow.setButtonSize(SIZE_16);
		slideshow.bindSizeToFitWithin(
				stackPane.widthProperty().multiply(0.50),
				stackPane.heightProperty().multiply(0.50));
		StackPane.setAlignment(slideshow, BOTTOM_RIGHT);
		StackPane.setMargin(slideshow, new Insets(0, 16, 16, 0));
		return stackPane;
	}

	@Override
	Predicate<? super SpecimenWrapper> createFilterPredicate(String text)
	{
		SpecimenTextPredicate predicate = new SpecimenTextPredicate(text);
		return w -> predicate.test(w.getWrapee());
	}

	@Override
	SpecimenWrapper wrap(Specimen specimen)
	{
		return new SpecimenWrapper(specimen);
	}

	private class PhotoLoader extends Service<List<FishingPhoto>>
	{
		@Override
		protected Task<List<FishingPhoto>> createTask()
		{
			slideshow.setPhotos(emptyCursor());
			SpecimenWrapper selectedItem = getTable().getSelectionModel().getSelectedItem();
			return new Task<>()
			{
				@Override
				protected List<FishingPhoto> call() throws Exception
				{
					if (selectedItem == null)
					{
						return emptyList();
					}
					else
					{
						Specimen specimen = selectedItem.getWrapee();
						return photoService.getSpecimenPhotos(specimen.getTripId(), specimen.getId());
					}
				}
			};
		}

		@Override
		protected void succeeded()
		{
			List<FishingPhoto> photos = getValue();
			photos.sort(comparing(Photo::getTime));
			slideshow.setPhotos(Cursor.of(photos, 0));
			slideshow.setVisible(!photos.isEmpty());
		}
	}
}

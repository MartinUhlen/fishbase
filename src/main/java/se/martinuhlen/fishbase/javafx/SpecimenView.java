package se.martinuhlen.fishbase.javafx;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static javafx.beans.binding.Bindings.createStringBinding;
import static javafx.geometry.Pos.BOTTOM_RIGHT;
import static org.controlsfx.control.textfield.TextFields.createClearableTextField;
import static se.martinuhlen.fishbase.javafx.Converters.converter;
import static se.martinuhlen.fishbase.javafx.utils.ImageSize.SIZE_16;
import static se.martinuhlen.fishbase.utils.EmptyCursor.emptyCursor;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
	private final TextField filterField;
	private final Slider ratioSlider;

	SpecimenView(FishBaseDao dao, PhotoService photoService, Consumer<String> tripOpener)
	{
		super("Specimens", dao::getSpecimens, dao::saveSpecimens, dao::deleteSpecimens);
		this.dao = dao;
		this.photoService = photoService;
		this.tripOpener = tripOpener;
		this.slideshow = new SlideshowPane();
		this.photoLoader = new PhotoLoader();
        this.filterField = createClearableTextField();
		this.ratioSlider = new Slider(0, 1.0, 0.5);
	}

	@Override
	Node createTopNode()
	{
	    filterField.setPrefWidth(200);
	    ratioSlider.setPrefWidth(200);
	    ratioSlider.setShowTickLabels(true);
	    ratioSlider.setShowTickMarks(true);
	    ratioSlider.setSnapToTicks(true);
	    ratioSlider.setMajorTickUnit(0.25);
	    ratioSlider.setMinorTickCount(4);
	    ratioSlider.setLabelFormatter(converter(ratio -> Integer.toString((int) (ratio.doubleValue() * 100))));

	    Label ratioLabel = new Label();
	    ratioLabel.textProperty().bind(createStringBinding(() -> ratioSlider.getLabelFormatter().toString(ratioSlider.getValue()) + "%", ratioSlider.valueProperty()));

	    Label countLabel = new Label();
	    countLabel.textProperty().bind(createStringBinding(() -> getTable().getItems().size() + " specimens", getTable().getItems()));

	    HBox box = new HBox(8, filterField, ratioSlider, ratioLabel, new Label("  "), countLabel);
	    box.setAlignment(Pos.CENTER_LEFT);
	    return box;
	}

	@Override
	TableView<SpecimenWrapper> createTable()
	{
	    FilteredList<SpecimenWrapper> filteredList = list.filtered(createFilterPredicate());
        SpecimenTable specimenTable = new SpecimenTable(filteredList, dao::getSpecies, dao::getAutoCompletions, tripOpener);
        specimenTable.getSelectionModel().selectedItemProperty().addListener(obs -> photoLoader.restart());
        filterField.textProperty().addListener(obs -> filteredList.setPredicate(createFilterPredicate()));
        ratioSlider.valueProperty().addListener(obs -> filteredList.setPredicate(createFilterPredicate()));
        return specimenTable;
	}

    private Predicate<SpecimenWrapper> createFilterPredicate()
    {
        SpecimenTextPredicate predicate = new SpecimenTextPredicate(filterField.getText());
        Predicate<SpecimenWrapper> textPredicate = w -> predicate.test(w.getWrapee());
        Predicate<SpecimenWrapper> ratioPredicate = w -> w.ratioProperty().getValue() >= ratioSlider.getValue();
        return textPredicate.and(ratioPredicate);
    }

	@Override
	Node createTableNode()
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
	SpecimenWrapper wrap(Specimen specimen)
	{
		return new SpecimenWrapper(specimen);
	}

	@Override
	void removeSelected()
	{
	    ((SpecimenTable) getTable()).removeSelected();
	}
	
	@Override
	void deleteRows(List<Specimen> rowsToDelete)
	{
	    super.deleteRows(rowsToDelete);
	    removeSpecimensFromPhotos(rowsToDelete);
	}

    private void removeSpecimensFromPhotos(List<Specimen> rowsToDelete)
    {
        rowsToDelete.forEach(specimen ->
	    {
	        photoService.getSpecimenPhotos(specimen.getTripId(), specimen.getId())
	                .stream()
	                .map(photo -> photo.withoutSpecimen(specimen.getId()))
	                .forEach(photo -> photoService.savePhoto(photo));
	    });
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
						List<FishingPhoto> photos = photoService.getSpecimenPhotos(specimen.getTripId(), specimen.getId());
						photos.sort(comparing(FishingPhoto::isStarred).reversed().thenComparing(Photo::getTime));
						return photos;
					}
				}
			};
		}

		@Override
		protected void succeeded()
		{
			List<FishingPhoto> photos = getValue();			
			slideshow.setPhotos(Cursor.of(photos, 0));
			slideshow.setVisible(!photos.isEmpty());
		}
	}
}

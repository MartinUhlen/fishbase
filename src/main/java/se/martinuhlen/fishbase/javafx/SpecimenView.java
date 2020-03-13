package se.martinuhlen.fishbase.javafx;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;
import static javafx.beans.binding.Bindings.createStringBinding;
import static javafx.geometry.Pos.BOTTOM_RIGHT;
import static org.controlsfx.control.textfield.TextFields.createClearableTextField;
import static se.martinuhlen.fishbase.javafx.utils.Converters.converter;
import static se.martinuhlen.fishbase.javafx.utils.ImageSize.SIZE_16;
import static se.martinuhlen.fishbase.utils.EmptyCursor.emptyCursor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javafx.beans.InvalidationListener;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import se.martinuhlen.fishbase.dao.FishBaseDao;
import se.martinuhlen.fishbase.domain.Photo;
import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.filter.SpecimenTextPredicate;
import se.martinuhlen.fishbase.google.photos.FishingPhoto;
import se.martinuhlen.fishbase.google.photos.PhotoService;
import se.martinuhlen.fishbase.javafx.data.SpecimenWrapper;
import se.martinuhlen.fishbase.javafx.photo.SlideshowPane;
import se.martinuhlen.fishbase.utils.Cursor;

class SpecimenView extends AbstractTableView<SpecimenWrapper, Specimen>
{
	// FIXME Add a column (of icons) indicating if specimen has photo or not.

	private final FishBaseDao dao;
	private final PhotoService photoService;
	private final SlideshowPane slideshow;
	private final PhotoLoader photoLoader;
	private final Consumer<String> tripOpener;
	private final TextField filterField;
	private final Slider ratioSlider;
	private final CheckBox personalBestCheckBox;

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
		this.personalBestCheckBox = new CheckBox("PB");
		filterField.setPromptText("Filter...");
		filterField.setTooltip(new Tooltip("Filter specimens by free text"));
		ratioSlider.setTooltip(new Tooltip("Filter specimens by the ratio of their weight compared to the reg weight of it's specie"));
		personalBestCheckBox.setTooltip(new Tooltip("Show only personal best of each specie"));
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

	    HBox box = new HBox(8, filterField, ratioSlider, ratioLabel, personalBestCheckBox, new Label("  "), countLabel);
	    box.setAlignment(Pos.CENTER_LEFT);
	    return box;
	}

	@Override
	TableView<SpecimenWrapper> createTable()
	{
	    FilteredList<SpecimenWrapper> filteredList = list.filtered(createFilterPredicate());
        SpecimenTable specimenTable = new SpecimenTable(filteredList, dao::getSpecies, dao::getAutoCompletions, tripOpener);
        specimenTable.getSelectionModel().selectedItemProperty().addListener(obs -> photoLoader.restart());
        InvalidationListener listener = obs -> filteredList.setPredicate(createFilterPredicate());
        filterField.textProperty().addListener(listener);
        ratioSlider.valueProperty().addListener(listener);
        personalBestCheckBox.selectedProperty().addListener(listener);
        return specimenTable;
	}

    private Predicate<SpecimenWrapper> createFilterPredicate()
    {
        Predicate<Specimen> textPredicate = new SpecimenTextPredicate(filterField.getText());
        Predicate<Specimen> ratioPredicate = s -> s.getRatio() >= ratioSlider.getValue();
        Predicate<Specimen> personalBestPredicate = w -> true;
        if (personalBestCheckBox.isSelected())
        {
            Map<Specie, Specimen> personalBest = list
                    .stream()
                    .map(SpecimenWrapper::getWrapee)
                    .collect(groupingBy(
                            Specimen::getSpecie,
                            collectingAndThen(
                                    reducing((s1, s2) -> s1.getWeight() >= s2.getWeight() ? s1 : s2),
                                    Optional::get)));

            personalBestPredicate = s -> s.equals(personalBest.get(s.getSpecie()));
        }
        Predicate<Specimen> predicate = textPredicate.and(ratioPredicate).and(personalBestPredicate);
        return w -> predicate.test(w.getWrapee());
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
	    	throw new UnsupportedOperationException("Not yet implemented");
//	        photoService.getSpecimenPhotos(specimen.getTripId(), specimen.getId())
//	                .stream()
//	                .map(photo -> photo.withoutSpecimen(specimen.getId()))
//	                .forEach(photo -> photoService.savePhoto(photo));
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
//						Specimen specimen = selectedItem.getWrapee();
//						List<FishingPhoto> photos = photoService.getSpecimenPhotos(specimen.getTripId(), specimen.getId());
//						photos.sort(comparing(FishingPhoto::isStarred).reversed().thenComparing(Photo::getTime));
//						return photos;

						Specimen specimen = selectedItem.getWrapee();
						List<Photo> photos = dao.getTrip(specimen.getTripId())
								.getPhotos()
								.stream()
								.filter(p -> p.getSpecimens().contains(specimen.getId()))
								.sorted(comparing(Photo::isStarred).reversed().thenComparing(Photo::getTime))
								.collect(toList());
						return photoService.load(photos);
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

package se.martinuhlen.fishbase.javafx;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.control.ButtonType.CANCEL;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.controlsfx.validation.Validator.createPredicateValidator;
import static se.martinuhlen.fishbase.domain.Trip.DATES_IN_RANGE;
import static se.martinuhlen.fishbase.domain.Trip.DATE_NOT_IN_FUTURE;
import static se.martinuhlen.fishbase.domain.Trip.DESCRIPTION_IS_MANDATORY;
import static se.martinuhlen.fishbase.domain.Trip.EMPTY_TRIP;
import static se.martinuhlen.fishbase.domain.Trip.TEXT_IS_MANDATORY;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import org.controlsfx.validation.ValidationSupport;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import se.martinuhlen.fishbase.dao.FishBaseDao;
import se.martinuhlen.fishbase.domain.Photo;
import se.martinuhlen.fishbase.domain.Trip;
import se.martinuhlen.fishbase.google.photos.FishingPhoto;
import se.martinuhlen.fishbase.google.photos.PhotoService;
import se.martinuhlen.fishbase.javafx.action.Action;
import se.martinuhlen.fishbase.javafx.action.RunnableAction;
import se.martinuhlen.fishbase.javafx.controls.DatePicker;
import se.martinuhlen.fishbase.javafx.data.TripWrapper;
import se.martinuhlen.fishbase.javafx.photo.PhotoPane;

class TripView implements View
{
	private final RunnableAction addAction = new RunnableAction(true, () -> add());
	private final RunnableAction refreshAction = new RunnableAction(true, () -> refreshOrRollback());
	private final RunnableAction saveAction = new RunnableAction(false, () -> save());
	private final RunnableAction deleteAction = new RunnableAction(false, () -> delete());

	private final FishBaseDao dao;
	private final PhotoService photoService;
	private final TripWrapper wrapper;
	private final ReadOnlyStringProperty titleProperty;
	private final SplitPane splitPane;
	private final TripList list;
	private final Node tripPane;
	private final TextField descriptionField;
	private final ObservableList<FishingPhoto> photos = observableArrayList();

	TripView(FishBaseDao dao, PhotoService photoService)
	{
		this.dao = dao;
		this.photoService = photoService;
		this.wrapper = new TripWrapper();
		this.titleProperty = createTitleProperty();
		this.list = new TripList(trip -> selectTrip(trip));
		this.descriptionField = new TextField("");
		this.tripPane = createTripPane();
		this.splitPane = createSplitPane();
		bindPhotos();
		bindButtons();
	}

	private void bindButtons()
	{
		wrapper.addListener(obs ->
		{
			saveAction.setEnabled(wrapper.hasChanges());
			deleteAction.setEnabled(!wrapper.isEmpty());
		});
	}

	private void bindPhotos()
	{
		AtomicBoolean sync = new AtomicBoolean(true);
		Consumer<List<FishingPhoto>> resetter = fishingPhotos ->
		{
			sync.set(false);
			try
			{
				photos.setAll(fishingPhotos);
			}
			finally
			{
				sync.set(true);
			}			
		};

		photos.addListener(new ListChangeListener<FishingPhoto>()
		{
			@Override
			public void onChanged(Change<? extends FishingPhoto> change)
			{
				while(change.next())
				{
					change.getAddedSubList().forEach(photo -> photo.addListener(p -> sync()));
				}
				sync();
			}

			private void sync()
			{
				if (sync.get())
				{
					List<Photo> domains = photos.stream().map(p -> p.getDomain()).collect(Collectors.toList());
					wrapper.photos().setValue(domains);
				}
			}
		});

		Service<List<FishingPhoto>> loader = new Service<List<FishingPhoto>>()
		{
			@Override
			protected Task<List<FishingPhoto>> createTask()
			{
				return new Task<>()
				{
					@Override
					protected List<FishingPhoto> call() throws Exception
					{
						Thread.sleep(10);
						List<Photo> photosToLoad = wrapper.photos().getValue();
						List<FishingPhoto> loadedPhotos = photoService.load(photosToLoad);
						return loadedPhotos;
					}
				};
			}

			@Override
			protected void succeeded()
			{
				resetter.accept(getValue());
			}
		};

		wrapper.id().addListener(change ->
		{
			resetter.accept(emptyList());
			loader.restart();
		});
	}

	void selectTrip(String tripId)
	{
		list.selectTrip(tripId);
		descriptionField.requestFocus();
	}

	private void selectTrip(Trip trip)
	{
		if (!wrapper.getWrapee().equalsId(trip))
		{
			if (discardChanges())
			{
				setTrip(trip.isNew() ? trip : dao.getTrip(trip.getId()));
			}
			else
			{
				runLater(() -> list.selectTrip(wrapper.id().getValue()));
			}
		}
	}

	private ReadOnlyStringProperty createTitleProperty()
	{
		String defaultTitle = "Trips";
		ReadOnlyStringWrapper property = new ReadOnlyStringWrapper(defaultTitle);
		wrapper.addListener(obs ->
		{
			Trip trip = wrapper.getWrapee();
			String title = trip == EMPTY_TRIP
					? defaultTitle
					: trip.getLabel();
			property.set(title);
		});
		return property.getReadOnlyProperty();
	}

	private Node createTripPane()
	{
		VBox overviewBox = new VBox();
		overviewBox.setPadding(new Insets(8));
		descriptionField.textProperty().bindBidirectional(wrapper.description());
		overviewBox.getChildren().add(new Label("Description"));
		overviewBox.getChildren().add(descriptionField);
		overviewBox.getChildren().add(new Label(""));

		DatePicker startDatePicker = createDatePicker(wrapper.startDate());
		DatePicker endDatePicker = createDatePicker(wrapper.endDate());
		overviewBox.getChildren().add(new Label("Date"));
		overviewBox.getChildren().add(new HBox(startDatePicker, new Label("  -  "), endDatePicker));
		overviewBox.getChildren().add(new Label(""));

		TextArea textArea = new TextArea();
		VBox.setVgrow(textArea, Priority.ALWAYS);
		textArea.setWrapText(true);
		textArea.textProperty().bindBidirectional(wrapper.text());
		overviewBox.getChildren().add(new Label("Text"));
		overviewBox.getChildren().add(textArea);

		PhotoPane photoPane = createPhotoPane();
		SplitPane hSplit = new SplitPane(overviewBox, photoPane);
		bindPersistedDividerLocation(hSplit, "TripView.horizontalSplit.right.dividerLocation", 0.50);

		SpecimenTable specimenTable = createSpecimenTable();
		SplitPane vSplit = new SplitPane(hSplit, specimenTable);
		vSplit.setOrientation(VERTICAL);
		bindPersistedDividerLocation(vSplit, "TripView.verticalSplit.dividerLocation", 0.75);

		ValidationSupport vs = new ValidationSupport();
		addValidation(vs, descriptionField, DESCRIPTION_IS_MANDATORY);
		addValidation(vs, startDatePicker, DATE_NOT_IN_FUTURE);
		addValidation(vs, endDatePicker, DATES_IN_RANGE);
		addValidation(vs, textArea, TEXT_IS_MANDATORY);

		return vSplit;
	}

	private void addValidation(ValidationSupport vs, Control control, String message)
    {
	    vs.registerValidator(control, false, createPredicateValidator(x -> !wrapper.getWrapee().getValidationErrors().anyMatch(str -> str.equals(message)), message));
    }

    private void bindPersistedDividerLocation(SplitPane split, String key, double defaultValue)
	{
        Preferences preferences = Preferences.userRoot();
        Divider divider = split.getDividers().get(0);
        divider.setPosition(preferences.getDouble(key, defaultValue));
        divider.positionProperty().addListener((obs, oldValue, newValue) -> preferences.putDouble(key, newValue.doubleValue()));
	}

	private DatePicker createDatePicker(Property<LocalDate> property)
	{
		DatePicker picker = new DatePicker(property);
		picker.setPrefWidth(130);
		return picker;
	}

	private SpecimenTable createSpecimenTable()
	{
		return new SpecimenTable(wrapper.specimenWrappers(), dao::getSpecies, dao::getAutoCompletions, () -> wrapper.getWrapee());
	}

	private PhotoPane createPhotoPane()
	{
		return new PhotoPane(photoService, wrapper.startDate(), wrapper.endDate(), wrapper.id(), wrapper.specimens(), photos);
	}

	private SplitPane createSplitPane()
	{
		SplitPane split = new SplitPane(list, tripPane);
		bindPersistedDividerLocation(split, "TripView.horizontalSplit.left.dividerLocation", 0.20);
		list.setMinWidth(50);
		tripPane.setVisible(false);
		wrapper.addListener(obs -> tripPane.setVisible(wrapper.getWrapee() != EMPTY_TRIP));
		return split;
	}

	@Override
	public Node getContent()
	{
		return splitPane;
	}

	@Override
	public ReadOnlyStringProperty titleProperty()
	{
		return titleProperty;
	}

	@Override
	public Action addAction()
	{
		return addAction;
	}

	private void add()
	{
		selectTrip(Trip.asNew());
		descriptionField.requestFocus();
	}

	@Override
	public Action refreshAction()
	{
		return refreshAction;
	}

	private void refreshOrRollback()
	{
		if (discardChanges())
		{
			refresh();
		}
	}

	private void refresh()
	{
		Trip trip = wrapper.getWrapee();
		List<Trip> trips = dao.getTrips();
		list.setTrips(trips);
		Trip refreshedTrip = trips.stream()
			.filter(t -> t.equalsId(trip))
			.findAny()
			.orElse(wrapper.isEmpty() ? EMPTY_TRIP : trip.isNew() ? Trip.asNew() : EMPTY_TRIP);
		setTrip(refreshedTrip);
		list.selectTrip(refreshedTrip.getId());
	}

	private void setTrip(Trip trip)
	{
		wrapper.setWrapee(trip);
	}

	@Override
	public Action saveAction()
	{
		return saveAction;
	}

	private void save()
	{
		Trip trip = wrapper.getWrapee();
		String errorMessage = trip.getValidationErrors().collect(joining("\n"));
		if (isNotBlank(errorMessage))
		{
			Alert alert = new Alert(ERROR);
			alert.setTitle("Can not save");
			alert.setHeaderText("Can not save due to validation errors.");
			alert.setContentText(errorMessage);
			alert.showAndWait();
		}
		else
		{
			dao.saveTrip(trip);
			refresh();
		}
	}

	@Override
	public Action deleteAction()
	{
		return deleteAction;
	}

	private void delete()
	{
		ButtonType delete = new ButtonType("Delete", OK_DONE);
		Alert alert = new Alert(CONFIRMATION);
		alert.setTitle("Confirm deletion");
		alert.setHeaderText("Are you sure you want to delete '" + titleProperty.get() + "'?");
		alert.getButtonTypes().setAll(delete, CANCEL);
		alert.showAndWait()
			.filter(b -> b == delete)
			.ifPresent(b -> deleteImpl());
	}

	private void deleteImpl()
	{
		dao.deleteTrip(wrapper.getWrapee());
		wrapper.setWrapee(EMPTY_TRIP);
		refresh();
	}
}

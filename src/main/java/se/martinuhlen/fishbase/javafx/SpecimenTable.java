package se.martinuhlen.fishbase.javafx;

import static java.util.Arrays.asList;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.cell.TextFieldTableCell.forTableColumn;
import static se.martinuhlen.fishbase.javafx.utils.Converters.dateConverter;
import static se.martinuhlen.fishbase.javafx.utils.Converters.lengthConverter;
import static se.martinuhlen.fishbase.javafx.utils.Converters.specieConverter;
import static se.martinuhlen.fishbase.javafx.utils.Converters.timeConverter;
import static se.martinuhlen.fishbase.javafx.utils.Images.getImageView16;
import static se.martinuhlen.fishbase.javafx.utils.Styles.BACKGROUND_GREEN;
import static se.martinuhlen.fishbase.javafx.utils.Styles.BACKGROUND_RED;
import static se.martinuhlen.fishbase.javafx.utils.Styles.RIGHT_ALIGNMENT;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.util.converter.IntegerStringConverter;
import se.martinuhlen.fishbase.domain.AutoCompleteField;
import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;
import se.martinuhlen.fishbase.javafx.data.SpecimenWrapper;

class SpecimenTable extends TableView<SpecimenWrapper>
{
    private final ObservableList<SpecimenWrapper> specimens;
	private final Supplier<Collection<Specie>> specieSupplier;
	private final Function<AutoCompleteField, SortedSet<String>> autoCompleter;
    private final Supplier<Trip> tripSupplier;
    private final Consumer<String> tripOpener;

    private SpecimenTable(ObservableList<SpecimenWrapper> sourceSpecimens, ObservableList<SpecimenWrapper> tableSpecimens, Supplier<Collection<Specie>> specieSupplier, Function<AutoCompleteField, SortedSet<String>> autoCompleter, Supplier<Trip> tripSupplier, Consumer<String> tripOpener)
    {
        this.specimens = sourceSpecimens;
        this.specieSupplier = specieSupplier;
        this.autoCompleter = autoCompleter;
        this.tripSupplier = tripSupplier;
        this.tripOpener = tripOpener;
        setSpecimens(tableSpecimens);
        getColumns().setAll(createColumns());
        setContextMenu(createContextMenu());
        setEditable(true);
    }

    SpecimenTable(ObservableList<SpecimenWrapper> specimens, Supplier<Collection<Specie>> species, Function<AutoCompleteField, SortedSet<String>> autoCompleter, Supplier<Trip> tripSupplier)
    {
        this(specimens, specimens, species, autoCompleter, tripSupplier, null);
    }

	@SuppressWarnings("unchecked")
    SpecimenTable(FilteredList<SpecimenWrapper> filteredSpecimens, Supplier<Collection<Specie>> species, Function<AutoCompleteField, SortedSet<String>> autoCompleter, Consumer<String> tripOpener)
	{
	    this((ObservableList<SpecimenWrapper>) filteredSpecimens.getSource(), filteredSpecimens, species, autoCompleter, null, tripOpener);
	}

    private ContextMenu createContextMenu()
    {
	    List<MenuItem> items = new ArrayList<>();
	    MenuItem openTrip = new MenuItem("Open trip", getImageView16("window_next.png"));
	    if (tripOpener != null)
	    {
    	    openTrip.setOnAction(e -> tripOpener.accept(getSelectedSpecimen().getTripId()));
    	    items.add(openTrip);
    	    items.add(new SeparatorMenuItem());
	    }

        MenuItem add = new MenuItem("Add", getImageView16("add.png"));
        if (tripSupplier != null)
        {
            add.setOnAction(e -> editSpecimen(true, createNewSpecimen(), s -> specimens.add(new SpecimenWrapper(s)))); // FIXME Must also add a listener (SpecimenView)?
            items.add(add);
            setOnMouseClicked(e ->
            {
                if (e.getClickCount() >= 2)
                {
                    add.fire();
                }
            });
        }

        MenuItem copy = new MenuItem("Copy", getImageView16("copy.png"));
        copy.setOnAction(e -> editSpecimen(true, getSelectedSpecimen().copyAsNew(), s -> specimens.add(new SpecimenWrapper(s))));
        items.add(copy);

        MenuItem edit = new MenuItem("Edit", getImageView16("edit.png"));
        edit.setOnAction(e -> editSpecimen(false, getSelectedSpecimen().copy(), s -> getSelectedSpecimenWrapper().setWrapee(s)));
        items.add(edit);

        MenuItem remove = new MenuItem("Remove", getImageView16("delete.png"));
        remove.setOnAction(e -> removeSelected());
        items.add(remove);

        ContextMenu menu = new ContextMenu();
        menu.getItems().setAll(items);
        menu.setOnShowing(e -> asList(openTrip, copy, edit, remove).forEach(item -> item.setDisable(getSelectionModel().isEmpty())));
        return menu;
    }

    void removeSelected()
    {
        ButtonType delete = new ButtonType("Remove", OK_DONE);
        Alert alert = new Alert(CONFIRMATION);
        alert.setTitle("Confirm removal");
        alert.setHeaderText("Are you sure you want to remove '" + getSelectedSpecimen().getLabel() + "'?");
        alert.getButtonTypes().setAll(delete, CANCEL);
        alert.showAndWait()
            .filter(b -> b == delete)
            .ifPresent(b -> specimens.remove(getSelectedSpecimenWrapper()));
    }

    private Specimen getSelectedSpecimen()
    {
        return getSelectedSpecimenWrapper().getWrapee();
    }

    private SpecimenWrapper getSelectedSpecimenWrapper()
    {
        return getSelectionModel().getSelectedItem();
    }

    private void editSpecimen(boolean add, Specimen initialSpecimen, Consumer<Specimen> handler)
    {
        new SpecimenDialog(add, specieSupplier.get(), autoCompleter, initialSpecimen)
            .showAndWait()
            .ifPresent(specimen ->
            {
                handler.accept(specimen);
                requestFocus();
                getItems().stream().filter(s -> s.getWrapee().equalsId(specimen)).findAny().ifPresent(sw ->
                {
                    getSelectionModel().select(sw);
                });
            });
    }

    private Specimen createNewSpecimen()
    {
        Trip trip = tripSupplier.get();
        return Specimen.asNew(trip.getId())
                        .withInstant(trip.getStartDate().atStartOfDay());
    }

    private void setSpecimens(ObservableList<SpecimenWrapper> tableSpecimens)
	{
		SortedList<SpecimenWrapper> sortedList = new SortedList<>(tableSpecimens);
		setItems(sortedList);
		sortedList.comparatorProperty().bind(comparatorProperty());
	}

	private Collection<TableColumn<SpecimenWrapper, ?>> createColumns()
	{
	    Collection<Specie> species = specieSupplier.get();
		TableColumn<SpecimenWrapper, Specie> specieColumn = new TableColumn<>("Specie");
		specieColumn.setCellValueFactory(cdf -> cdf.getValue().specieProperty());
		specieColumn.setCellFactory(c ->
        {
        	ComboBoxTableCell<SpecimenWrapper, Specie> cell = new ComboBoxTableCell<>();
            cell.getItems().setAll(species);
        	cell.setConverter(specieConverter());
        	return cell;
        });

		TableColumn<SpecimenWrapper, Integer> weightColumn = new TableColumn<>("Weight");
		weightColumn.setCellValueFactory(cdf -> cdf.getValue().weightProperty());
		weightColumn.setCellFactory(forTableColumn(new IntegerStringConverter()));
		weightColumn.setStyle(RIGHT_ALIGNMENT);

		TableColumn<SpecimenWrapper, Double> ratioColumn = new TableColumn<>("Ratio");
		ratioColumn.setCellValueFactory(cdf -> cdf.getValue().ratioProperty());
		ratioColumn.setCellFactory(column -> new ProgressBarTableCell<>()
        {
        	@Override
        	public void updateItem(Double ratio, boolean empty)
        	{
        		super.updateItem(ratio, empty);
        		setTooltip(empty ? null : new Tooltip(((int) (ratio * 100)) + "%"));
        		if (!empty)
        		{
        			if (ratio >= 1.0)
        			{
        				getGraphic().setStyle(BACKGROUND_GREEN);
        			}
        			else if (ratio <= 0.5)
        			{
        				getGraphic().setStyle(BACKGROUND_RED);
        			}
        			else
        			{
        				getGraphic().setStyle(null);
        			}
        		}
        	}
        });

		TableColumn<SpecimenWrapper, Float> lengthColumn = new TableColumn<>("Length");
		lengthColumn.setStyle(RIGHT_ALIGNMENT);
		lengthColumn.setCellValueFactory(cdf -> cdf.getValue().lengthProperty());
		lengthColumn.setCellFactory(forTableColumn(lengthConverter()));

		TableColumn<SpecimenWrapper, String> locationColumn = new TableColumn<>("Location");
		locationColumn.setCellValueFactory(f -> f.getValue().locationProperty());
		locationColumn.setCellFactory(forTableColumn());

		TableColumn<SpecimenWrapper, LocalDate> dateColumn = new TableColumn<>("Date");
		dateColumn.setCellValueFactory(cdf -> cdf.getValue().dateProperty());
		dateColumn.setCellFactory(forTableColumn(dateConverter()));

		TableColumn<SpecimenWrapper, LocalTime> timeColumn = new TableColumn<>("Time");
		timeColumn.setCellValueFactory(cdf -> cdf.getValue().timeProperty());
		timeColumn.setCellFactory(forTableColumn(timeConverter()));

		TableColumn<SpecimenWrapper, String> methodColumn = new TableColumn<>("Method");
		methodColumn.setCellValueFactory(f -> f.getValue().methodProperty());
		methodColumn.setCellFactory(forTableColumn());

		TableColumn<SpecimenWrapper, String> baitColumn = new TableColumn<>("Bait");
		baitColumn.setCellValueFactory(f -> f.getValue().baitProperty());
		baitColumn.setCellFactory(forTableColumn());

		TableColumn<SpecimenWrapper, String> weatherColumn = new TableColumn<>("Weather");
		weatherColumn.setCellValueFactory(f -> f.getValue().weatherProperty());
		weatherColumn.setCellFactory(forTableColumn());

		TableColumn<SpecimenWrapper, String> textColumn = new TableColumn<>("Text");
		textColumn.setCellValueFactory(f -> f.getValue().textProperty());
		textColumn.setCellFactory(forTableColumn());

		specieColumn.setPrefWidth(80);
		weightColumn.setPrefWidth(70);
		ratioColumn.setPrefWidth(120);
		lengthColumn.setPrefWidth(70);
		locationColumn.setPrefWidth(200);
		dateColumn.setPrefWidth(100);
		timeColumn.setPrefWidth(60);
		methodColumn.setPrefWidth(110);
		baitColumn.setPrefWidth(200);
		weatherColumn.setPrefWidth(110);
		widthProperty().addListener(o -> resizeTextColumn());

		return asList(specieColumn, weightColumn, ratioColumn, lengthColumn, locationColumn, dateColumn, timeColumn, methodColumn, baitColumn, weatherColumn, textColumn);
	}

	/**
	 * Resizes text column to occupy remaining width of table.
	 * <p>
	 * TableView has bad support for column sizes, it lacks pack feature as J(X)Table has.
	 * Clicking edge of column header resizes column to fit content, but implementation is hidden in TableSkinUtils.
	 * <p>
	 * Possible workarounds:
	 * 		http://dlsc.com/2015/12/10/javafx-tip-22-autosize-tree-table-columns/
	 *		Google "javafx tableview autosize columns"
	 */
	private void resizeTextColumn()
	{
		TableColumn<?, ?> textColumn = getColumns().get(getColumns().size() - 1);
		double occupiedWidth = getColumns().stream().filter(c -> c != textColumn).mapToDouble(TableColumn::getWidth).sum();
		double columnWidth = Math.max(getWidth() - occupiedWidth - 16, 80);
		textColumn.setPrefWidth(columnWidth);
	}
}

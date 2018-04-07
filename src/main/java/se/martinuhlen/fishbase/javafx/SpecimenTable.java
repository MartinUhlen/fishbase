package se.martinuhlen.fishbase.javafx;

import static java.util.Arrays.asList;
import static javafx.scene.control.cell.TextFieldTableCell.forTableColumn;
import static se.martinuhlen.fishbase.javafx.Converters.dateConverter;
import static se.martinuhlen.fishbase.javafx.Converters.lengthConverter;
import static se.martinuhlen.fishbase.javafx.Converters.specieConverter;
import static se.martinuhlen.fishbase.javafx.Converters.timeConverter;
import static se.martinuhlen.fishbase.javafx.utils.Constants.RIGHT_ALIGNMENT;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;

import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.javafx.data.SpecimenWrapper;

class SpecimenTable extends TableView<SpecimenWrapper>
{
	private final Collection<Specie> species;

	SpecimenTable(ObservableList<SpecimenWrapper> specimens, Collection<Specie> species)
	{
		this.species = species;
		setSpecimens(specimens);
		getColumns().setAll(createColumns());
		setEditable(true);
	}

	private void setSpecimens(ObservableList<SpecimenWrapper> specimens)
	{
		SortedList<SpecimenWrapper> sortedList = new SortedList<>(specimens);
		setItems(sortedList);
		sortedList.comparatorProperty().bind(comparatorProperty());
	}

	private Collection<TableColumn<SpecimenWrapper, ?>> createColumns()
	{
		TableColumn<SpecimenWrapper, Specie> specieColumn = new TableColumn<>("Specie");
		specieColumn.setCellValueFactory(cdf -> cdf.getValue().specieProperty());
		specieColumn.setCellFactory(new Callback<>()
		{
			@Override
			public TableCell<SpecimenWrapper, Specie> call(TableColumn<SpecimenWrapper, Specie> c)
			{
				ComboBoxTableCell<SpecimenWrapper, Specie> cell = new ComboBoxTableCell<>();
				cell.getItems().setAll(species);
				cell.setConverter(specieConverter());
				return cell;
			}
		});

		TableColumn<SpecimenWrapper, Integer> weightColumn = new TableColumn<>("Weight");
		weightColumn.setCellValueFactory(cdf -> cdf.getValue().weightProperty());
		weightColumn.setCellFactory(forTableColumn(new IntegerStringConverter()));
		weightColumn.setStyle(RIGHT_ALIGNMENT);

		TableColumn<SpecimenWrapper, Double> ratioColumn = new TableColumn<>("Ratio");
		ratioColumn.setCellValueFactory(cdf -> cdf.getValue().ratioProperty());
		ratioColumn.setCellFactory(new Callback<>()
		{
			@Override
			public TableCell<SpecimenWrapper, Double> call(TableColumn<SpecimenWrapper, Double> column)
			{
				return new ProgressBarTableCell<>()
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
								getGraphic().setStyle("-fx-accent: green;");
							}
							else if (ratio <= 0.5)
							{
								getGraphic().setStyle("-fx-accent: red;");
							}
							else
							{
								getGraphic().setStyle(null);
							}
						}
					}
				};
			}
		});

		TableColumn<SpecimenWrapper, Float> lengthColumn = new TableColumn<>("Length");
		lengthColumn.setStyle(RIGHT_ALIGNMENT);
		lengthColumn.setCellValueFactory(cdf -> cdf.getValue().lengthProperty());
		lengthColumn.setCellFactory(forTableColumn(lengthConverter()));

		TableColumn<SpecimenWrapper, String> locationColumn = new TableColumn<>("Location");
		locationColumn.setCellValueFactory(f -> f.getValue().locationProperty());
		locationColumn.setCellFactory(forTableColumn());
//		locationColumn.setCellFactory(c ->
//		{
//			TextFieldTableCell<SpecimenWrapper, String> cell = new TextFieldTableCell<>();
//			//TextFields.bindAutoCompletion(cell.getTe, possibleSuggestions)
//			return cell;
//		});

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

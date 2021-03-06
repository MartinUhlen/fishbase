package se.martinuhlen.fishbase.javafx;

import static java.util.Arrays.asList;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.cell.TextFieldTableCell.forTableColumn;
import static se.martinuhlen.fishbase.javafx.utils.Styles.RIGHT_ALIGNMENT;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.converter.IntegerStringConverter;
import se.martinuhlen.fishbase.dao.FishBaseDao;
import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.javafx.action.Action;
import se.martinuhlen.fishbase.javafx.action.RunnableAction;
import se.martinuhlen.fishbase.javafx.data.SpecieWrapper;

class SpecieView extends AbstractTableView<SpecieWrapper, Specie>
{
	private final FishBaseDao dao;
	private final RunnableAction addAction = new RunnableAction(true, () -> add());

	SpecieView(FishBaseDao dao)
	{
		super("Species", dao::getSpecies, dao::saveSpecies, dao::deleteSpecies);
		this.dao = dao;
	}

	@Override
	TableView<SpecieWrapper> createTable()
	{
		SortedList<SpecieWrapper> sortedList = new SortedList<>(list);
		TableView<SpecieWrapper> table = new TableView<>(sortedList);
		sortedList.comparatorProperty().bind(table.comparatorProperty());
		table.setEditable(true);

		TableColumn<SpecieWrapper, String> nameColumn = new TableColumn<>("Name");
		nameColumn.setCellValueFactory(f -> f.getValue().nameProperty());
		nameColumn.setCellFactory(forTableColumn());

		TableColumn<SpecieWrapper, Integer> regWeightColumn = new TableColumn<>("Reg weight");
		regWeightColumn.setStyle(RIGHT_ALIGNMENT);
		regWeightColumn.setCellValueFactory(f -> f.getValue().regWeightProperty());
		regWeightColumn.setCellFactory(forTableColumn(new IntegerStringConverter()));

		TableColumn<SpecieWrapper, Boolean> freshWaterColumn = new TableColumn<>("Fresh water");
		freshWaterColumn.setCellValueFactory(f -> f.getValue().freshWaterProperty());
		freshWaterColumn.setCellFactory(c -> new CheckBoxTableCell<>());

		table.getColumns().setAll(asList(nameColumn, regWeightColumn, freshWaterColumn));

		nameColumn.prefWidthProperty().set(150);
		regWeightColumn.prefWidthProperty().set(100);
		freshWaterColumn.prefWidthProperty().set(100);

		return table;
	}

	@Override
	boolean isRemovable(Specie specie)
	{
		if (!dao.isSpecieDeletable(specie))
		{
			Alert alert = new Alert(ERROR);
			alert.setTitle("Cannot delete");
			alert.setHeaderText("Cannot delete the specie '" + specie.getLabel() + "' because it has specimens.");
			alert.showAndWait();
			return false;
		}
		else
		{
			return true;
		}
	}

	@Override
	SpecieWrapper wrap(Specie specie)
	{
		return new SpecieWrapper(specie);
	}

	@Override
	public Action addAction()
	{
		return addAction;
	}

	private void add()
	{
		TableView<SpecieWrapper> table = getTable();
		list.add(new SpecieWrapper(Specie.asNew(), this::tableChange));
		int lastRow = list.size() - 1;
		TableColumn<SpecieWrapper, ?> firstColumn = table.getColumns().get(0);
		//table.scrollTo(lastRow);
		table.edit(lastRow, firstColumn);
		table.getFocusModel().focus(table.getEditingCell());
		table.getSelectionModel().select(lastRow);
	}
}

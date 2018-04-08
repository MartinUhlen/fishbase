package se.martinuhlen.fishbase.javafx;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.control.ButtonType.CANCEL;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.controlsfx.control.textfield.TextFields;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import se.martinuhlen.fishbase.domain.Domain;
import se.martinuhlen.fishbase.javafx.action.Action;
import se.martinuhlen.fishbase.javafx.action.RunnableAction;
import se.martinuhlen.fishbase.javafx.data.Wrapper;
import se.martinuhlen.fishbase.utils.Logger;

abstract class AbstractTableView<W extends Wrapper<D>, D extends Domain<D>> implements View
{
	private final Logger logger = Logger.getLogger(getClass());
	private final ReadOnlyStringProperty titleProperty;
	private final Supplier<List<D>> loader;
	private final Consumer<Collection<D>> saver;
	private final Consumer<Collection<D>> deleter;

	private final TextField filterField;
	private TableView<W> table;

	final ObservableList<W> list;
	private List<D> unchangedList = emptyList();

	private final RunnableAction saveAction = new RunnableAction(false, () -> save());
	private final RunnableAction refreshAction = new RunnableAction(true, () -> refreshOrRollback());
	private final RunnableAction deleteAction = new RunnableAction(false, () -> delete());

	private Node content;

	AbstractTableView(String title, Supplier<List<D>> loader, Consumer<Collection<D>> saver, Consumer<Collection<D>> deleter)
	{
		this.titleProperty = new ReadOnlyStringWrapper(title).getReadOnlyProperty();
		this.loader = loader;
		this.saver = saver;
		this.deleter = deleter;

		this.filterField = TextFields.createClearableTextField();
		this.filterField.setPromptText("Filter...");
		TextFields.bindAutoCompletion(filterField, "A", "AA", "ABCC", "AABBCC", "AAAA", "AAAABCC");

		this.list = observableArrayList();
		list.addListener(this::tableChange);
	}

	@Override
	public ReadOnlyStringProperty titleProperty()
	{
		return titleProperty;
	}

	@Override
	public final Node getContent()
	{
		if (content == null)
		{
			refreshAction.run();	// FIXME Must do this lazily, to avoid all views being loaded up front

			BorderPane pane = new BorderPane();
			pane.setTop(filterField);
			pane.setCenter(getTableNode());
			filterField.setMaxWidth(300);
			content = pane;
		}
		return content;
	}

	Node getTableNode()
	{
		return getTable();
	}

	TableView<W> getTable()
	{
		if (table == null)
		{
			FilteredList<W> filteredList = new FilteredList<>(list);
			filterField.textProperty().addListener((observable, oldValue, newValue) -> filteredList.setPredicate(createFilterPredicate(newValue)));
			table = createTable(filteredList);
			table.setEditable(true);
			table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> deleteAction.setEnabled(newValue != null));
		}
		return table;
	}

	abstract Predicate<? super W> createFilterPredicate(String text);

	abstract TableView<W> createTable(FilteredList<W> filteredList);

	void tableChange(Observable obs)
	{
		tableChange();
	}

	void tableChange()
	{
		boolean hasChanges = !unchangedList.equals(currentList());
		logger.log("Has changes: " + hasChanges);
		saveAction.setEnabled(hasChanges);
	}

	List<D> currentList()
	{
		return list.stream().map(Wrapper::getWrapee).collect(toList());
	}

	@Override
	public final Action saveAction()
	{
		return saveAction;
	}

	private void save()
	{
		String message = currentList()
				.stream()
				.flatMap(Domain::getValidationErrors)
				.collect(Collectors.joining("\n"));
		if (isNotBlank(message))
		{
			Alert alert = new Alert(ERROR);
			alert.setTitle("Can not save");
			alert.setHeaderText("Can not save due to validation errors.");
			alert.setContentText(message);
			alert.showAndWait();
		}
		else
		{
			List<D> rowsToSave = currentList();
			rowsToSave.removeAll(unchangedList);
			saver.accept(rowsToSave);
			logger.log("Saving: (" + rowsToSave.size() + ") " + rowsToSave);

			Set<String> currentIds = currentList().stream().map(Domain::getId).collect(toSet());
			List<D> rowsToDelete = unchangedList.stream().filter(t -> !currentIds.contains(t.getId())).collect(toList());
			logger.log("Deleting: (" + rowsToDelete.size() + ") " + rowsToDelete);
			deleteRows(rowsToDelete);

			refresh();
		}
	}

    void deleteRows(List<D> rowsToDelete)
    {
        deleter.accept(rowsToDelete);
    }

	@Override
	public final Action refreshAction()
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
		list.forEach(s -> s.removeAllListeners());
		unchangedList = loader.get();
		list.setAll(unchangedList.stream().map(t -> wrap(t)).peek(w -> w.addListener(this::tableChange)).collect(toList()));
	}

	abstract W wrap(D d);

	@Override
	public final Action deleteAction()
	{
		return deleteAction;
	}

	private void delete()
	{
		W wrapper = table.getSelectionModel().getSelectedItem();
		D wrapee = wrapper.getWrapee();
		if (isRemovable(wrapee))
		{
		    removeSelected();
		}
	}

    void removeSelected()
    {
        W wrapper = table.getSelectionModel().getSelectedItem();
        D wrapee = wrapper.getWrapee();

        ButtonType delete = new ButtonType("Remove", OK_DONE);
        Alert alert = new Alert(CONFIRMATION);
        alert.setTitle("Confirm removal");
        alert.setHeaderText("Are you sure you want to remove '" + wrapee.getLabel() + "'?");
        alert.getButtonTypes().setAll(delete, CANCEL);
        alert.showAndWait()
        	.filter(b -> b == delete)
        	.ifPresent(b -> list.remove(wrapper));
    }

    /**
     * Checks if if given wrapee object can be removed from the table and then on save be deleted.
     * 
     * @param wrapee to check
     * @return {@code true} if OK to remove
     */
	boolean isRemovable(D wrapee)
	{
		return true;
	}
}

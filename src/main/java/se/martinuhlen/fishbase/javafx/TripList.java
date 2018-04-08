package se.martinuhlen.fishbase.javafx;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.layout.Priority.ALWAYS;
import static javafx.scene.text.FontWeight.BOLD;

import java.util.Collection;
import java.util.function.Consumer;

import org.controlsfx.control.textfield.TextFields;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;
import se.martinuhlen.fishbase.filter.TripTextPredicate;

class TripList extends VBox
{
	private final TextField filter;
	private final ObservableList<Trip> list;
	private final ListView<Trip> listView;

	TripList(Consumer<Trip> selectionHandler)
	{
		list = FXCollections.observableArrayList();
		FilteredList<Trip> filteredList = list.filtered(t -> true);
		filter = createFilter(filteredList);
		listView = createListView(selectionHandler, filteredList);

		Label label = new Label();
		filteredList.addListener((Observable obs) -> label.setText(filteredList.size() + " trips"));

		setVgrow(listView, ALWAYS);
		getChildren().setAll(filter, listView, label);
	}

	private TextField createFilter(FilteredList<Trip> filteredList)
	{
		TextField filter = TextFields.createClearableTextField();
		filter.setPromptText("Filter...");
		filter.textProperty().addListener(obs -> filteredList.setPredicate(new TripTextPredicate(filter.getText())));
		filter.onKeyPressedProperty().set(e ->
		{
			if (e.getCode() == ESCAPE)
			{
				filter.setText("");
			}
			else if (e.getCode() == DOWN)
			{
				listView.requestFocus();
				if (!listView.getItems().isEmpty() && listView.getSelectionModel().getSelectedIndex() == -1)
				{
					listView.getSelectionModel().select(0);
				}
			}
		});
		return filter;
	}

	private ListView<Trip> createListView(Consumer<Trip> selectionHandler, FilteredList<Trip> filteredList)
	{
		ListView<Trip> listView = new ListView<>(filteredList);
		listView.setCellFactory(list -> new TripCell());
		listView.getSelectionModel().selectedItemProperty().addListener((obs, oldTrip, newTrip) ->
		{
			if (newTrip != null)
			{
				selectionHandler.accept(newTrip);
			}
		});
		listView.addEventFilter(KeyEvent.KEY_PRESSED, e ->
		{
			if (e.getCode() == UP && listView.getSelectionModel().getSelectedIndex() <= 0)
			{
				filter.requestFocus();
			}
		});
		return listView;
	}

	private static class TripCell extends ListCell<Trip>
	{
		private static final Font BOLD_FONT = Font.font(Font.getDefault().getFamily(), BOLD, Font.getDefault().getSize() - 2);

		@Override
		protected void updateItem(Trip trip, boolean empty)
		{
			super.updateItem(trip, empty);
			setGraphic(null);
			if (!empty)
			{
				VBox box = new VBox(new Text(trip.getStartDate() + "\n" + trip.getDescription()));
				if (!trip.getSpecimens().isEmpty())
				{
					Text specimens = new Text(trip.getSpecimens()
							.stream()
							.sorted(comparing(Specimen::getRatio).reversed())
							.map(Specimen::getLabel)
							.collect(joining(", ")));
					specimens.setFont(BOLD_FONT);
					box.getChildren().add(specimens);
				}
				setGraphic(box);
			}
		}
	}

	void setTrips(Collection<Trip> trips)
	{
		list.setAll(trips);
	}

	void selectTrip(String tripId)
	{
		listView.getSelectionModel().clearSelection();
		listView.getItems().stream()
				.filter(trip -> trip.getId().equals(tripId))
				.findAny()
				.ifPresent(trip ->
				{
					listView.getSelectionModel().select(trip);
					listView.scrollTo(trip);
				});
	}
}

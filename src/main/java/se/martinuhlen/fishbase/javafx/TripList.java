package se.martinuhlen.fishbase.javafx;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static javafx.scene.layout.HBox.setHgrow;
import static javafx.scene.layout.Priority.ALWAYS;
import static javafx.scene.text.FontWeight.BOLD;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.controlsfx.control.textfield.TextFields;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;
import se.martinuhlen.fishbase.filter.TripTextPredicate;

class TripList extends VBox {

    private static final Predicate<Trip> ACCEPT_ALL = _ -> true;

    private final TextField textFilter;
    private final CheckBox photoCheckBox;
    private final CheckBox specimenCheckBox;
    private final ObservableList<Trip> list;
    private final FilteredList<Trip> filteredList;
    private final ListView<Trip> listView;

    TripList(Consumer<Trip> selectionHandler) {
        list = FXCollections.observableArrayList();
        filteredList = list.filtered(ACCEPT_ALL);
        textFilter = createFilter();
        photoCheckBox = createCheckBox("Photos");
        specimenCheckBox = createCheckBox("Specimens");
        listView = createListView(selectionHandler, filteredList);

        Label label = new Label();
        filteredList.addListener((Observable _) -> label.setText(filteredList.size() + " trips"));

        setVgrow(listView, ALWAYS);
        setHgrow(textFilter, ALWAYS);
        getChildren().setAll(new HBox(textFilter, photoCheckBox, specimenCheckBox), listView, label);
    }

    private TextField createFilter() {
        TextField filter = TextFields.createClearableTextField();
        filter.setPromptText("Filter...");
        filter.textProperty().addListener(_ -> applyFilter());
        filter.onKeyPressedProperty().set(e -> {
            if (e.getCode() == ESCAPE) {
                filter.setText("");
            }
            else if (e.getCode() == DOWN) {
                listView.requestFocus();
                if (!listView.getItems().isEmpty() && listView.getSelectionModel().getSelectedIndex() == -1) {
                    listView.getSelectionModel().select(0);
                }
            }
        });
        return filter;
    }

    private ListView<Trip> createListView(Consumer<Trip> selectionHandler, FilteredList<Trip> filteredList) {
        ListView<Trip> listView = new ListView<>(filteredList);
        listView.setCellFactory(_ -> new TripCell());
        listView.getSelectionModel().selectedItemProperty().addListener((_, _, newTrip) -> {
            if (newTrip != null) {
                selectionHandler.accept(newTrip);
            }
        });
        listView.addEventFilter(KEY_PRESSED, e -> {
            if (e.getCode() == UP && listView.getSelectionModel().getSelectedIndex() <= 0) {
                textFilter.requestFocus();
            }
        });
        return listView;
    }

    private CheckBox createCheckBox(String label) {
        CheckBox checkBox = new CheckBox(label);
        checkBox.selectedProperty().addListener(_ -> applyFilter());
        return checkBox;
    }

    private void applyFilter() {
        Predicate<Trip> filter = ACCEPT_ALL;
        if (!textFilter.getText().isBlank()) {
            filter = filter.and(new TripTextPredicate(textFilter.getText()));
        }
        if (photoCheckBox.isSelected()) {
            filter = filter.and(Trip::hasPhotos);
        }
        if (specimenCheckBox.isSelected()) {
            filter = filter.and(Trip::hasSpecimens);
        }
        filteredList.setPredicate(filter);
    }

    private static class TripCell extends ListCell<Trip> {
        private static final Font BOLD_FONT = Font.font(Font.getDefault().getFamily(), BOLD, Font.getDefault().getSize() - 2);

        @Override
        protected void updateItem(Trip trip, boolean empty) {
            super.updateItem(trip, empty);
            setGraphic(null);
            if (!empty) {
                VBox box = new VBox(new Text(trip.getStartDate() + "\n" + trip.getDescription()));
                if (!trip.getSpecimens().isEmpty()) {
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

    void setTrips(Collection<Trip> trips) {
        list.setAll(trips);
    }

    void selectTrip(String tripId) {
        listView.getSelectionModel().clearSelection();
        listView.getItems().stream()
                .filter(trip -> trip.getId().equals(tripId))
                .findAny()
                .ifPresentOrElse(trip -> {
                    listView.getSelectionModel().select(trip);
                    listView.scrollTo(trip);
                },
                () -> {
                    if (!textFilter.getText().equals("")) {
                        textFilter.setText("");
                        selectTrip(tripId);
                    }
                });
    }
}

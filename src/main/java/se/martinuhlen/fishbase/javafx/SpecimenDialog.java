package se.martinuhlen.fishbase.javafx;

import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.OK;
import static se.martinuhlen.fishbase.javafx.Converters.lengthConverter;
import static se.martinuhlen.fishbase.javafx.Converters.specieConverter;
import static se.martinuhlen.fishbase.javafx.Converters.timeConverter;
import static se.martinuhlen.fishbase.javafx.utils.Constants.RIGHT_ALIGNMENT;

import java.time.LocalTime;
import java.util.Collection;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.converter.IntegerStringConverter;
import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.javafx.data.SpecimenWrapper;

class SpecimenDialog extends Dialog<Specimen>
{
	private final SpecimenWrapper wrapper;
	private final ComboBox<Specie> specieCombo;

	SpecimenDialog(boolean add, Collection<Specie> species, Specimen specimen)
	{
		this.wrapper = new SpecimenWrapper(specimen, obs -> enableOkButton());
		specieCombo = new ComboBox<>(observableArrayList(species));
		getDialogPane().getButtonTypes().setAll(CANCEL, OK);
		getDialogPane().setContent(createForm());
		setResultConverter(b -> b == OK ? wrapper.getWrapee() : null);
		setTitle(add ? "Add new specimen" : "Edit specimen");
		//setResizable(true);
		//setWidth(200);
		setOnShowing(e -> onShowing());
	}

	private void onShowing()
	{
		enableOkButton();
		runLater(() -> specieCombo.requestFocus());
	}

	private void enableOkButton()
	{
		Node button = getDialogPane().lookupButton(OK);
		boolean hasErrors = wrapper.getWrapee().getValidationErrors().findAny().isPresent();
		boolean hasChanges = wrapper.hasChanges();
		button.setDisable(hasErrors || !hasChanges);
	}

	private Node createForm()
	{
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(0);
		grid.setPadding(new Insets(20, 20, 20, 20));

		Label specieLabel = new Label("Specie");
		grid.add(specieLabel, 0, 0);
		specieCombo.setConverter(specieConverter());
		specieCombo.valueProperty().bindBidirectional(wrapper.specieProperty());
		grid.add(specieCombo, 0, 1);
		grid.add(emptyRow(), 0, 2);

		Label weightLabel = new Label("Weight");
		grid.add(weightLabel, 0, 3);
		TextFormatter<Integer> weightFormatter = new TextFormatter<>(new IntegerStringConverter());
		weightFormatter.valueProperty().bindBidirectional(wrapper.weightProperty());
		TextField weightField = new TextField();
		weightField.setTextFormatter(weightFormatter);
		weightField.setStyle(RIGHT_ALIGNMENT);
		grid.add(suffix(weightField, " g"), 0, 4);

		Label lengthLabel = new Label("Length");
		grid.add(lengthLabel, 1, 3);
		TextFormatter<Float> lengthFormatter = new TextFormatter<>(lengthConverter());
		lengthFormatter.valueProperty().bindBidirectional(wrapper.lengthProperty());
		TextField lengthField = new TextField();
		lengthField.setTextFormatter(lengthFormatter);
		lengthField.setStyle(RIGHT_ALIGNMENT);
		grid.add(suffix(lengthField, " cm"), 1, 4);
		grid.add(emptyRow(), 0, 5);

		Label locationLabel = new Label("Location");
		grid.add(locationLabel, 0, 6);
		TextField locationField = new TextField();
		locationField.textProperty().bindBidirectional(wrapper.locationProperty());
		grid.add(locationField, 0, 7, 2, 1);
		grid.add(emptyRow(), 0, 8);

		Label dateLabel = new Label("Date");
		grid.add(dateLabel, 0, 9);
		DatePicker datePicker = new DatePicker(wrapper.dateProperty());
		grid.add(datePicker, 0, 10);

		Label timeLabel = new Label("Time");
		grid.add(timeLabel, 1, 9);
		TextFormatter<LocalTime> timeFormatter = new TextFormatter<>(timeConverter());
		timeFormatter.valueProperty().bindBidirectional(wrapper.timeProperty());
		TextField timeField = new TextField();
		timeField.setTextFormatter(timeFormatter);
		grid.add(timeField, 1, 10);
		grid.add(emptyRow(), 0, 11);

		Label methodLabel = new Label("Method");
		grid.add(methodLabel, 0, 12);
		TextField methodField = new TextField();
		methodField.textProperty().bindBidirectional(wrapper.methodProperty());
		grid.add(methodField, 0, 13);

		Label baitLabel = new Label("Bait");
		grid.add(baitLabel, 1, 12);
		TextField baitField = new TextField();
		baitField.textProperty().bindBidirectional(wrapper.baitProperty());
		grid.add(baitField, 1, 13);
		grid.add(emptyRow(), 0, 14);

		Label weatherLabel = new Label("Weather");
		grid.add(weatherLabel, 0, 15);
		TextField weatherField = new TextField();
		weatherField.textProperty().bindBidirectional(wrapper.weatherProperty());
		grid.add(weatherField, 0, 16);
		grid.add(emptyRow(), 0, 17);

		Label textLabel = new Label("Text");
		grid.add(textLabel, 0, 18);
		TextArea textArea = new TextArea();
		textArea.textProperty().bindBidirectional(wrapper.textProperty());
		textArea.setPrefRowCount(2);
		textArea.setPrefColumnCount(2);
		grid.add(textArea, 0, 19, 2, 2);

//		grid.getColumnConstraints().add(new ColumnConstraints(100));
//		grid.getColumnConstraints().add(new ColumnConstraints(100));

		return grid;

	}

	private Node suffix(TextField field, String suffix)
	{
		HBox box = new HBox(field, new Label(suffix));
		box.setAlignment(Pos.BASELINE_LEFT);
		return box;
	}

	private Node emptyRow()
	{
		Region row = new Label("");
		row.setMinHeight(10);
		row.setPrefHeight(10);
		row.setMaxHeight(10);
		return row;
	}
}

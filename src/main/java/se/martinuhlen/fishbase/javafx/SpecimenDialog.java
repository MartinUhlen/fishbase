package se.martinuhlen.fishbase.javafx;

import static java.util.stream.Collectors.toList;
import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.geometry.Pos.BASELINE_LEFT;
import static javafx.geometry.Pos.CENTER;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.OK;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.math.NumberUtils.isDigits;
import static org.controlsfx.validation.Validator.createPredicateValidator;
import static se.martinuhlen.fishbase.domain.AutoCompleteField.BAIT;
import static se.martinuhlen.fishbase.domain.AutoCompleteField.LOCATION;
import static se.martinuhlen.fishbase.domain.AutoCompleteField.METHOD;
import static se.martinuhlen.fishbase.domain.AutoCompleteField.WEATHER;
import static se.martinuhlen.fishbase.domain.Specimen.BAIT_IS_REQUIRED;
import static se.martinuhlen.fishbase.domain.Specimen.DATE_IS_REQUIRED;
import static se.martinuhlen.fishbase.domain.Specimen.LOCATION_IS_REQUIRED;
import static se.martinuhlen.fishbase.domain.Specimen.METHOD_IS_REQUIRED;
import static se.martinuhlen.fishbase.domain.Specimen.SPECIE_IS_REQUIRED;
import static se.martinuhlen.fishbase.domain.Specimen.WEATHER_IS_REQUIRED;
import static se.martinuhlen.fishbase.domain.Specimen.WEIGHT_IS_REQUIRED;
import static se.martinuhlen.fishbase.javafx.utils.Converters.lengthConverter;
import static se.martinuhlen.fishbase.javafx.utils.Converters.specieConverter;
import static se.martinuhlen.fishbase.javafx.utils.Converters.timeConverter;
import static se.martinuhlen.fishbase.javafx.utils.Converters.weightConverter;
import static se.martinuhlen.fishbase.javafx.utils.Styles.RIGHT_ALIGNMENT;

import java.time.LocalTime;
import java.util.Collection;
import java.util.SortedSet;
import java.util.function.Function;

import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.ValidationSupport;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import se.martinuhlen.fishbase.domain.AutoCompleteField;
import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.javafx.controls.ComboBox;
import se.martinuhlen.fishbase.javafx.controls.DatePicker;
import se.martinuhlen.fishbase.javafx.data.SpecimenWrapper;

/**
 * A dialog to add new or edit existing {@link Specimen}s.
 *
 * @author Martin
 */
class SpecimenDialog extends Dialog<Specimen>
{
	private final SpecimenWrapper wrapper;
	private final ComboBox<Specie> specieCombo;
    private final Function<AutoCompleteField, SortedSet<String>> autoCompleter;

	SpecimenDialog(boolean add, Collection<Specie> species, Function<AutoCompleteField, SortedSet<String>> autoCompleter, Specimen specimen)
	{
		this.autoCompleter = autoCompleter;
        this.wrapper = new SpecimenWrapper(specimen, obs -> enableOkButton());
		specieCombo = new ComboBox<>(observableArrayList(species));
		getDialogPane().getButtonTypes().setAll(CANCEL, OK);
		getDialogPane().setContent(createForm());
		getDialogPane().setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
		setWidth(USE_COMPUTED_SIZE);
		setHeight(USE_COMPUTED_SIZE);
		setResizable(true);
		setResultConverter(b -> b == OK ? wrapper.getWrapee() : null);
		setTitle(add ? "Add new specimen" : "Edit specimen");
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
		grid.setAlignment(CENTER);
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
		TextFormatter<Integer> weightFormatter = new TextFormatter<>(weightConverter(), 0);
		weightFormatter.valueProperty().bindBidirectional(wrapper.weightProperty());
		TextField weightField = new TextField();
		weightField.setTextFormatter(weightFormatter);
		weightField.setStyle(RIGHT_ALIGNMENT);
		grid.add(suffix(weightField, " g"), 0, 4);
        weightField.textProperty().addListener(new InvalidationListener() // Commit weight value on any edit so error icon disappears.
        {
            private boolean syncing;

            @Override
            public void invalidated(Observable observable)
            {
                if (!syncing && (isDigits(weightField.getText()) || isBlank(weightField.getText())))
                {
                    syncing = true;
                    weightField.commitValue();
                    syncing = false;
                }
            }
        });


		Label lengthLabel = new Label("Length");
		grid.add(lengthLabel, 1, 3);
		TextFormatter<Float> lengthFormatter = new TextFormatter<>(lengthConverter(), 0f);
		lengthFormatter.valueProperty().bindBidirectional(wrapper.lengthProperty());
		TextField lengthField = new TextField();
		lengthField.setTextFormatter(lengthFormatter);
		lengthField.setStyle(RIGHT_ALIGNMENT);
		grid.add(suffix(lengthField, " cm"), 1, 4);
		grid.add(emptyRow(), 0, 5);

		Label locationLabel = new Label("Location");
		grid.add(locationLabel, 0, 6);
		TextField locationField = textField(wrapper.locationProperty(), LOCATION);
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
		TextField methodField = textField(wrapper.methodProperty(), METHOD);
		grid.add(methodField, 0, 13);

		Label baitLabel = new Label("Bait");
		grid.add(baitLabel, 1, 12);
		TextField baitField = textField(wrapper.baitProperty(), BAIT);
		grid.add(baitField, 1, 13);
		grid.add(emptyRow(), 0, 14);

		Label weatherLabel = new Label("Weather");
		grid.add(weatherLabel, 0, 15);
		TextField weatherField = textField(wrapper.weatherProperty(), WEATHER);
		grid.add(weatherField, 0, 16);
		grid.add(emptyRow(), 0, 17);

		Label textLabel = new Label("Text");
		grid.add(textLabel, 0, 18);
		TextArea textArea = new TextArea();
		textArea.textProperty().bindBidirectional(wrapper.textProperty());
		textArea.setPrefRowCount(2);
		textArea.setPrefColumnCount(2);
		grid.add(textArea, 0, 19, 2, 2);

		ValidationSupport vs = new ValidationSupport();
		addValidation(vs, specieCombo, SPECIE_IS_REQUIRED);
		addValidation(vs, weightField, WEIGHT_IS_REQUIRED);
		addValidation(vs, locationField, LOCATION_IS_REQUIRED);
		addValidation(vs, datePicker, DATE_IS_REQUIRED);
		addValidation(vs, methodField, METHOD_IS_REQUIRED);
		addValidation(vs, baitField, BAIT_IS_REQUIRED);
		addValidation(vs, weatherField, WEATHER_IS_REQUIRED);
		vs.initInitialDecoration();

		return grid;
	}

	private void addValidation(ValidationSupport vs, Control control, String message)
    {
	    vs.registerValidator(control, false, createPredicateValidator(x -> !wrapper.getWrapee().getValidationErrors().anyMatch(str -> str.equals(message)), message));
    }

    private TextField textField(Property<String> property, AutoCompleteField autoCompleteField)
    {
	    TextField field = new TextField();
	    field.textProperty().bindBidirectional(property);
	    SortedSet<String> suggestions = autoCompleter.apply(autoCompleteField);
	    TextFields.bindAutoCompletion(field, request ->
        {
            String text = request.getUserText().toLowerCase().trim();
            if (text.isEmpty())
            {
                return suggestions;
            }
            else
            {
                return suggestions
                        .stream()
                        .filter(str -> str.toLowerCase().contains(text))
                        .sorted((String s1, String s2) ->
                        {
                            String str1 = s1.toLowerCase();
                            String str2 = s2.toLowerCase();
                            return str1.startsWith(text) && str2.startsWith(text) 
                                    ? str1.length() - str2.length() 
                                    : str1.startsWith(text) 
                                        ? -1 
                                        : str2.startsWith(text) 
                                            ? 1 
                                            : 0;
                        })
                        .collect(toList());
            }
        });

	    return field;
    }

    private Node suffix(TextField field, String suffix)
	{
		HBox box = new HBox(field, new Label(suffix));
		box.setAlignment(BASELINE_LEFT);
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

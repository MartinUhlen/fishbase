package se.martinuhlen.fishbase.javafx;

import static se.martinuhlen.fishbase.javafx.Converters.dateConverter;

import java.time.LocalDate;

import javafx.beans.property.Property;

class DatePicker extends javafx.scene.control.DatePicker
{
	DatePicker(Property<LocalDate> dateProperty)
	{
		super();
		valueProperty().bindBidirectional(dateProperty);;
		setConverter(dateConverter());
		focusedProperty().addListener(obs -> setValue(getConverter().fromString(getEditor().getText())));  // Workaround value not commited on focus lost
	}
}

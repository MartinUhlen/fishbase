package se.martinuhlen.fishbase.javafx.controls;

import static se.martinuhlen.fishbase.javafx.utils.Converters.dateConverter;

import java.time.LocalDate;

import javafx.beans.property.Property;

/**
 * {@link javafx.scene.control.DatePicker} extension with features such as commit on focus lost.
 * 
 *
 * @author Martin
 */
public class DatePicker extends javafx.scene.control.DatePicker
{
    /**
     * Creates a new date picker.
     */
	public DatePicker()
	{
		super();
		setConverter(dateConverter());
		focusedProperty().addListener(obs -> setValue(getConverter().fromString(getEditor().getText())));  // Workaround value not commited on focus lost
	}

    /**
     * Creates a new date picker.
     * 
     * @param property this picker should be bound to
     */
	public DatePicker(Property<LocalDate> property)
	{
		this();
		valueProperty().bindBidirectional(property);
	}
}

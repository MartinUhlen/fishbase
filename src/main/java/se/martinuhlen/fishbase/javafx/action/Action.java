package se.martinuhlen.fishbase.javafx.action;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public interface Action extends EventHandler<ActionEvent>
{
	ReadOnlyBooleanProperty enabledProperty();

	default boolean isEnabled()
	{
		return enabledProperty().get();
	}
}

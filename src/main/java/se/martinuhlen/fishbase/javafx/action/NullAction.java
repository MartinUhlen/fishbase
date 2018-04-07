package se.martinuhlen.fishbase.javafx.action;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;

public final class NullAction implements Action
{
	public static final NullAction NULL_ACTION = new NullAction();

	private NullAction()
	{
	}

	private final SimpleBooleanProperty enabledProperty = new SimpleBooleanProperty(false);

	@Override
	public ReadOnlyBooleanProperty enabledProperty()
	{
		return enabledProperty;
	}

	@Override
	public void handle(ActionEvent arg0)
	{
	}
}

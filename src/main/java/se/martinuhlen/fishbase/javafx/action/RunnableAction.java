package se.martinuhlen.fishbase.javafx.action;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;

public class RunnableAction implements Action, Runnable
{
	private final SimpleBooleanProperty enabledProperty;
	private final Runnable action;

	public RunnableAction(boolean enabled, Runnable action)
	{
		enabledProperty = new SimpleBooleanProperty(enabled);
		this.action = requireNonNull(action, "action can't be null");
	}

	public void setEnabled(boolean value)
	{
		enabledProperty.set(value);
	}

	@Override
	public ReadOnlyBooleanProperty enabledProperty()
	{
		return enabledProperty;
	}

	@Override
	public void handle(ActionEvent event)
	{
		run();
	}

	@Override
	public void run()
	{
		if (enabledProperty().get())
		{
			action.run();
		}
	}
}

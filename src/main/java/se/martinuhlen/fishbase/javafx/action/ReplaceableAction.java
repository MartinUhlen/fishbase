package se.martinuhlen.fishbase.javafx.action;

import static se.martinuhlen.fishbase.javafx.action.NullAction.NULL_ACTION;

import org.apache.commons.lang3.ObjectUtils;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;

public class ReplaceableAction implements Action
{
	private final SimpleBooleanProperty enabledProperty;
	private Action action;

	public ReplaceableAction()
	{
		this.enabledProperty = new SimpleBooleanProperty();
		setAction(null);
	}

	public void setAction(Action action)
	{
		this.action = ObjectUtils.defaultIfNull(action, NULL_ACTION);
		enabledProperty.unbind();
		enabledProperty.bind(this.action.enabledProperty());
	}

	@Override
	public ReadOnlyBooleanProperty enabledProperty()
	{
		return enabledProperty;
	}

	@Override
	public void handle(ActionEvent event)
	{
		action.handle(event);
	}
}

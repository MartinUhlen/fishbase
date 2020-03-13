package se.martinuhlen.fishbase.javafx.action;

import static javafx.scene.control.Alert.AlertType.ERROR;
import static se.martinuhlen.fishbase.javafx.action.NullAction.NULL_ACTION;

import org.apache.commons.lang3.ObjectUtils;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;

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
		try
		{
			action.handle(event);
		}
		catch (Exception e)
		{
			Alert alert = new Alert(ERROR);
			alert.setTitle("An error occurred");
			alert.setHeaderText(e.getMessage());
			alert.showAndWait();
		}
	}
}

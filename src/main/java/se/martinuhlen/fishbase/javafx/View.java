package se.martinuhlen.fishbase.javafx;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.control.ButtonType.CANCEL;
import static se.martinuhlen.fishbase.javafx.action.NullAction.NULL_ACTION;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import se.martinuhlen.fishbase.javafx.action.Action;

interface View
{
	Node getContent();

	ReadOnlyStringProperty titleProperty();

	default String getTitle()
	{
		return titleProperty().get();
	}

	default boolean hasChanges()
	{
		return saveAction().enabledProperty().get();
	}

	public default boolean discardChanges()
	{
		if (hasChanges())
		{
			Alert alert = new Alert(CONFIRMATION);
			alert.setTitle("Discard changes?");
			alert.setHeaderText("'" + getTitle() + "' has unsaved changes, discard them?");
			ButtonType discard = new ButtonType("Discard changes", OK_DONE);
			alert.getButtonTypes().setAll(discard, CANCEL);
			return alert.showAndWait()
				.filter(b -> b == discard)
				.isPresent();
		}
		else
		{
			return true;
		}
	}

	default Action addAction()
	{
		return NULL_ACTION;
	}

	default Action saveAction()
	{
		return NULL_ACTION;
	}

	default Action refreshAction()
	{
		return NULL_ACTION;
	}

	default Action deleteAction()
	{
		return NULL_ACTION;
	}

	View EMPTY_VIEW = new View()
	{
		private final ReadOnlyStringProperty titleProperty = new ReadOnlyStringWrapper("").getReadOnlyProperty();

		@Override
		public Node getContent()
		{
			return null;
		}

		@Override
		public ReadOnlyStringProperty titleProperty()
		{
			return titleProperty;
		}
	};
}

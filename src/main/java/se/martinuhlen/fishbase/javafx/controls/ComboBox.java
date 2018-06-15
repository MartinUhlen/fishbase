package se.martinuhlen.fishbase.javafx.controls;

import static javafx.collections.FXCollections.emptyObservableList;
import static javafx.scene.input.KeyEvent.CHAR_UNDEFINED;

import java.time.Instant;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyEvent;

/**
 * A {@link javafx.scene.control.ComboBox} with extended features such as better key navigation.
 *
 * @author Martin
 */
public class ComboBox<T> extends javafx.scene.control.ComboBox<T>
{
    public ComboBox(ObservableList<T> items)
    {
        super(items);
        setOnKeyTyped(new KeyHandler());
    }

    public ComboBox()
    {
        this(emptyObservableList());
    }

    private class KeyHandler implements EventHandler<KeyEvent>
    {
        private String typedText = "";
        private Instant typedTextAt = Instant.now();

        @Override
        public void handle(KeyEvent event)
        {
            Instant now = Instant.now();
            if (typedTextAt.plusSeconds(1).isBefore(now))
            {
                typedText = "";
            }
            typedTextAt = now;

            if (isPopupTrigger(event))
            {
                show();
                return;
            }
            else if (event.getCharacter().equals(CHAR_UNDEFINED))
            {
                return;
            }
            else
            {
                typedText += event.getCharacter().toLowerCase();
            }

            getItems().stream()
                    .filter(item -> getConverter().toString(item).toLowerCase().startsWith(typedText))
                    .findFirst()
                    .ifPresent(this::selectItem);
        }

        private boolean isPopupTrigger(KeyEvent event)
        {
            return event.getCharacter().equals(" ") && event.isControlDown();
        }

        private void selectItem(T item)
        {
            @SuppressWarnings("unchecked")
            ListView<T> lv = (ListView<T>) ((ComboBoxListViewSkin<T>) getSkin()).getPopupContent();
            lv.scrollTo(item);
            getSelectionModel().select(item);
        }
    }
}

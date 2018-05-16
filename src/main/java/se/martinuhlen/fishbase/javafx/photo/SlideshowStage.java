package se.martinuhlen.fishbase.javafx.photo;

import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.stage.Modality.WINDOW_MODAL;
import static javafx.stage.StageStyle.UTILITY;

import java.util.function.Function;

import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import se.martinuhlen.fishbase.drive.photo.Photo;
import se.martinuhlen.fishbase.utils.Cursor;

/**
 * A facility to show {@link SlideshowPane} in a modal {@link Stage}.
 *
 * @author Martin
 */
class SlideshowStage
{
    private SlideshowStage()
    {
    }

    /**
     * Creates a handler to show {@link SlideshowPane} in a modal {@link Stage}.
     * 
     * @param callback accepts the clicked photo and returns cursor over all photos
     * @return mouse clicked handler
     */
    static EventHandler<MouseEvent> openOnClick(Function<HasPhoto, Cursor<Photo>> callback)
    {
        return new EventHandler<>()
        {
            private SlideshowPane slideshow;
            private Stage slideshowStage;

            @Override
            public void handle(MouseEvent event)
            {
                if (event.getClickCount() == 1 && event.getButton() == PRIMARY)
                {
                    showSlideshow(event);
                }
            }

            private void showSlideshow(MouseEvent event)
            {
                if (slideshow == null)
                {
                    slideshow = new SlideshowPane(false);
                    slideshowStage = new Stage(UTILITY);
                    slideshowStage.initModality(WINDOW_MODAL);
                    slideshowStage.initOwner(((Node) event.getSource()).getScene().getWindow());
                    Scene scene = new Scene(slideshow);
                    scene.getAccelerators().put(new KeyCodeCombination(ESCAPE), () -> slideshowStage.hide());
                    slideshowStage.setScene(scene);
                    Rectangle2D screenSize = Screen.getPrimary().getBounds();
                    slideshowStage.setWidth(screenSize.getWidth() * 0.90);
                    slideshowStage.setHeight(screenSize.getHeight() * 0.90);
                }
                HasPhoto source = (HasPhoto) event.getSource();
                Cursor<Photo> cursor = callback.apply(source);
                slideshow.setPhotos(cursor);
                slideshowStage.show();
                slideshowStage.requestFocus();
            }
        };
    }
}

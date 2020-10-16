package se.martinuhlen.fishbase.javafx;

import static javafx.concurrent.Worker.State.RUNNING;
import static javafx.stage.Modality.WINDOW_MODAL;
import static javafx.stage.StageStyle.UTILITY;

import javafx.beans.Observable;
import javafx.concurrent.Service;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

class ProgressDisplayer
{
	private final Stage stage;
	private final Service<?> service;

	ProgressDisplayer(Window owner, Service<?> service)
	{
		this.service = service;
		stage = new Stage(UTILITY);
		stage.initOwner(owner);
		stage.initModality(WINDOW_MODAL);
		stage.setResizable(false);
		stage.titleProperty().bind(service.titleProperty());

		Label message = new Label();
		message.textProperty().bind(service.messageProperty());

		ProgressBar progress = new ProgressBar(0);
		progress.progressProperty().bind(service.progressProperty());
		progress.setPrefWidth(500);

		stage.setScene(new Scene(new VBox(message, progress)));
	}

	void start()
	{
		startAndThen(() -> {});
	}

	void startAndThen(Runnable postAction)
	{
		service.start();
		service.stateProperty().addListener((Observable obs) ->
		{
			if (service.getState().compareTo(RUNNING) <= 0)
			{
				stage.show();
				stage.requestFocus();
			}
			else
			{
				stage.hide();
				postAction.run();
			}
		});
	}
}

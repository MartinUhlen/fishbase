package se.martinuhlen.fishbase.javafx.photo;

import static java.lang.Double.MAX_VALUE;
import static javafx.geometry.Pos.CENTER;
import static javafx.scene.layout.Priority.ALWAYS;
import static javafx.scene.media.MediaPlayer.Status.DISPOSED;
import static javafx.scene.media.MediaPlayer.Status.PAUSED;
import static javafx.scene.media.MediaPlayer.Status.PLAYING;
import static javafx.scene.media.MediaPlayer.Status.READY;
import static javafx.scene.media.MediaPlayer.Status.STALLED;
import static javafx.scene.media.MediaPlayer.Status.STOPPED;
import static javafx.util.Duration.UNKNOWN;
import static javafx.util.Duration.ZERO;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Set;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

/*
 * FIXME Video does not work, need additional codecs?
 * 
 * sudo apt-get dist-upgrade
 * sudo apt-get install ubuntu-restricted-extras
 */
class VideoPane extends BorderPane
{
	private final MediaView mediaView;
	private final Button playButton;
	private final Slider timeSlider;
	private final Label playTime;
	private final Label error;

	VideoPane()
	{
		mediaView = new MediaView();
		Pane mediaPane = new Pane();
		mediaPane.getChildren().add(mediaView);
		setCenter(mediaPane);
		mediaView.fitHeightProperty().bind(mediaPane.heightProperty());
		mediaView.fitWidthProperty().bind(mediaPane.widthProperty());
		mediaView.setPreserveRatio(true);
		mediaView.setSmooth(true);

		HBox mediaBar = new HBox();
		mediaBar.setAlignment(CENTER);
		mediaBar.setPadding(new Insets(5, 10, 5, 10));
		BorderPane.setAlignment(mediaBar, CENTER);

		playButton = new Button(">");
		playButton.setOnAction(e ->
		{
			MediaPlayer player = getPlayer();
			Status status = player.getStatus();
			if (status == PAUSED || status == READY || status == STOPPED)
			{
				player.play();
			}
			else if (status == PLAYING || status == STALLED)
			{
				player.pause();
			}
		});

		mediaBar.getChildren().add(playButton);
		mediaBar.getChildren().add(new Label(" "));

		timeSlider = new Slider();
		HBox.setHgrow(timeSlider, ALWAYS);
		timeSlider.setMinWidth(50);
		timeSlider.setMaxWidth(MAX_VALUE);
		timeSlider.valueProperty().addListener(obs ->
		{
			if (timeSlider.isValueChanging())
			{
				getPlayer().seek(getDuration().multiply(timeSlider.getValue() / 100.0));
			}
		});
		mediaBar.getChildren().add(timeSlider);

		playTime = new Label();
		mediaBar.getChildren().add(playTime);

		error = new Label();
		error.setVisible(false);
		error.setAlignment(CENTER);
		HBox errorBox = new HBox(error);
		errorBox.setAlignment(CENTER);
		errorBox.setVisible(false);

		enableControls();
		setBottom(new VBox(mediaBar, errorBox));
	}

	void setVideo(Media media, boolean autoPlay)
	{
		disposeCurrentVideo();
		if (media != null)
		{
			try
			{
				setPlayer(new MediaPlayer(media), autoPlay);
				enableControls();
				updateTime();
			}
			catch (Exception e)
			{
				showError(e.getMessage());
			}
		}
	}

	void disposeCurrentVideo()
	{
		MediaPlayer player = getPlayer();
		if (player != null)
		{
			player.stop();
			player.dispose();
			mediaView.setMediaPlayer(null);
		}
	}

	private void setPlayer(MediaPlayer player, boolean autoPlay)
	{
		player.setAutoPlay(autoPlay);
		mediaView.setMediaPlayer(player);
		player.currentTimeProperty().addListener(obs -> updateTime());
		player.statusProperty().addListener(obs ->
		{
			updateTime();
			enableControls();
			playButton.setText(getStatus() == PLAYING ? "||" : ">");
			System.err.println("STATUS: " + player.getStatus());
		});

		showError("");
		player.setOnError(() -> showError(player.getError().getMessage()));
		
		player.setOnEndOfMedia(() ->
		{
			player.seek(player.getStartTime());
			player.pause();
		});
	}

	private void showError(String text)
	{
		error.setText(text);
		error.setVisible(isNotBlank(text));
		error.getParent().setVisible(error.isVisible());
	}

	private void updateTime()
	{
		if (hasDuration())
		{
			Duration duration = getDuration();
			Duration currentTime = getCurrentTime();
			playTime.setText(formatTime(currentTime, duration));
			if (!timeSlider.isValueChanging())
			{
				timeSlider.setValue(currentTime.divide(duration.toMillis()).toMillis() * 100.0);
			}
		}
		else
		{
			timeSlider.setValue(0);
			playTime.setText("");
		}
	}

	private void enableControls()
	{
		boolean disable = !Set.of(READY, PLAYING, PAUSED, STOPPED, STALLED).contains(getStatus());
		playButton.setDisable(disable);
		timeSlider.setDisable(disable || !hasDuration());
	}

	private boolean hasDuration()
	{
		return !getDuration().isUnknown()
			&& !getDuration().isIndefinite()
			&& getDuration().greaterThan(ZERO);
	}

	private MediaPlayer getPlayer()
	{
		return mediaView.getMediaPlayer();
	}

	private Status getStatus()
	{
		return getPlayer() == null
				? DISPOSED
				: defaultIfNull(getPlayer().getStatus(), DISPOSED); // FIXME Status is null = evaluates to DISPOSED
	}

	private Duration getCurrentTime()
	{
		return getPlayer() == null
				? Duration.ZERO
				: getPlayer().getCurrentTime();
	}

	private Duration getDuration()
	{
		return getPlayer() == null
				? UNKNOWN
				: getPlayer().getMedia().getDuration();
	}

	private static String formatTime(Duration elapsed, Duration duration)
	{
		int intElapsed = (int) Math.floor(elapsed.toSeconds());
		int elapsedHours = intElapsed / (60 * 60);
		if (elapsedHours > 0)
		{
			intElapsed -= elapsedHours * 60 * 60;
		}
		int elapsedMinutes = intElapsed / 60;
		int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

		if (duration.greaterThan(Duration.ZERO))
		{
			int intDuration = (int) Math.floor(duration.toSeconds());
			int durationHours = intDuration / (60 * 60);
			if (durationHours > 0)
			{
				intDuration -= durationHours * 60 * 60;
			}
			int durationMinutes = intDuration / 60;
			int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;
			if (durationHours > 0)
			{
				return String.format("%d:%02d:%02d/%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds, durationHours, durationMinutes, durationSeconds);
			}
			else
			{
				return String.format("%02d:%02d/%02d:%02d", elapsedMinutes, elapsedSeconds, durationMinutes, durationSeconds);
			}
		}
		else
		{
			if (elapsedHours > 0)
			{
				return String.format("%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
			}
			else
			{
				return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
			}
		}
	}
}

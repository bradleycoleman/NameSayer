package controllers;

import data.FileCommands;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import main.Main;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This controls the playback/practice screen of the app. Users can listen to recordings of each of the
 * names in the playlist they determined on the start screen. They can then add their own attempts of the
 * names, which are saved, and can be deleted later. They can update the ratings of the database recording(s)
 * of the names. These ratings are saved to a text file, and displayed to future users when they select the name.
 */
public class TestScreenController {
    @FXML private Button _record;
    @FXML private Label _recordPrompt;
    @FXML private Button _play;
    @FXML private ProgressBar _progressIndicator;

    private enum State {START, RECORDING, PLAYABLE, PLAYING}
    private State _state;
    private TimerTask _timerTask;
    private Timer _timeWorker;
    private AudioInputStream _audio;
    private Clip _clip;
    private Main _main;
    private Task<Void> _recordTask;
    private final File AUDIOFILE = new File("userdata/test.wav");

    public void startTest(Main main) {
        _main = main;
        _timeWorker = new Timer();
        setState(State.START);
    }

    private void setState(State state) {
        _state = state;
        if (state == State.PLAYING) {
            _play.setDisable(true);
            _record.setDisable(true);
        } else if (state == State.RECORDING){
            FileCommands.deleteFile(AUDIOFILE);
            _record.setDisable(true);
            _play.setDisable(true);
        } else if (state == State.START) {
            FileCommands.deleteFile(AUDIOFILE);
            _progressIndicator.setProgress(0);
            _play.setDisable(true);
            _record.setDisable(false);
        } else {
            _progressIndicator.setProgress(0);
            _play.setDisable(false);
            _record.setDisable(false);
        }
    }

    /**
     * Starts a background task to record the name using ffmpeg, and another to count every second
     * and indicate to the user how long they've been recording for.
     */
    @FXML
    private void recordTest() {
        setState(State.RECORDING);
        _recordTask = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    FileCommands.recordTest();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        new Thread(_recordTask).start();
        _timerTask = new ProgessTask();
        _timeWorker.schedule(_timerTask,50,50);
    }

    /**
     * Plays the current name.
     */
    @FXML
    private void playTest(){
        setState(State.PLAYING);
        try {
            _clip = AudioSystem.getClip();
            _audio = AudioSystem.getAudioInputStream(AUDIOFILE);
            _clip.open(_audio);
            _clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
        _timerTask = new ProgessTask();
        _timeWorker.schedule(_timerTask,50,50);
    }

    /**
     * Button method for returning to the start screen.
     */
    @FXML
    private void returnToStartScreen(){
        // If the last playback is still playing, end it
        if (_clip != null) {
            _clip.stop();
        }
        if (_timeWorker != null) {
            _timeWorker.cancel();
        }
        _main.setSceneToCurate();
    }

    private class ProgessTask extends TimerTask {
        private int _seconds = 0;
        @Override
        public void run() {
            Platform.runLater(() -> {
                _progressIndicator.setProgress(_seconds/100.0);
            });
            _seconds++;
            if (_seconds > 99) {
                setState(State.PLAYABLE);
                cancel();
            }
        }
    }

}

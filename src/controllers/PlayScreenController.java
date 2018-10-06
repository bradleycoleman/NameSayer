package controllers;

import data.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import main.Main;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This controls the playback/practice screen of the app. Users can listen to recordings of each of the
 * names in the playlist they determined on the start screen. They can then add their own attempts of the
 * names, which are saved, and can be deleted later. They can update the ratings of the database recording(s)
 * of the names. These ratings are saved to a text file, and displayed to future users when they select the name.
 */
public class PlayScreenController {
    @FXML private Label _nameNumber;
    @FXML private Label _currentName;
    @FXML private Button _record;
    @FXML private Label _recordPrompt;
    @FXML private Button _stop;
    @FXML private Label _timer;
    @FXML private Button _play;
    @FXML private ProgressBar _databaseIndicator, _attemptIndicator;
    private ProgressBar _progressIndicator;
    @FXML private Button _previous;
    @FXML private Button _next;
    @FXML private HBox _attemptInfo;

    private Playlist _playlist;
    private int _index;
    private NameSayerModel _nameSayerModel;
    private Main _main;
    private FullName _name;
    private File _recentAttempt;
    private enum State {IDLE, PLAYING, RECORDING}
    private Timer _timeWorker;
    private AudioStream _clip;

    public void initializeData(NameSayerModel nameSayerModel, Main main){
        _nameSayerModel = nameSayerModel;
        _main = main;
    }

    /**
     * Initializes the practice menu.
     */
    public void startPractice(Playlist playlist) {
        _playlist = playlist;
        setIndex(0);
    }

    /**
     * Refreshes various components based on an inputted index of the playlist
     * @param index int which signifies the index of the name to be practiced.
     */
    private void setIndex(int index) {
        _playlist.setCompletion(index + 1);
        _index = index;
        _name = _playlist.getFullNames().get(index);
        setState(State.IDLE);
        _timer.setText("0s");
        _databaseIndicator.setProgress(0);
        _attemptIndicator.setProgress(0);
        _nameNumber.setText("Name " + (_index + 1) +" of " + _playlist.getFullNames().size());
        _currentName.setText(_name.toString());
    }

    /**
     * Set the current state for this screen. The state determines what components are enabled for the user.
     * @param state input the State to change to
     */
    private void setState(State state) {
        if (state == State.PLAYING) {
            _play.setDisable(true);
            _record.setDisable(true);
            _next.setDisable(true);
            _previous.setDisable(true);
        } else if (state == State.RECORDING){
            _recordPrompt.setText("Stop Recording:");
            _record.setVisible(false);
            _stop.setVisible(true);
            _stop.setDisable(false);
            _play.setDisable(true);
            _next.setDisable(true);
            _previous.setDisable(true);
        } else {
            _recordPrompt.setText("Record Attempt:");
            _play.setDisable(false);
            _record.setDisable(false);
            _record.setVisible(true);
            _stop.setVisible(false);
            _stop.setDisable(true);
            _next.setDisable(true);
            _previous.setDisable(false);
        }

        // Disables the previous or next button based on the current index selected.
        if (_index < 1) {
            _previous.setDisable(true);
        } else {
            _previous.setDisable(false);
        }
        if (_index >= _playlist.getFullNames().size() - 1) {
            _next.setDisable(true);
        } else {
            _next.setDisable(false);
        }
    }

    @FXML
    private void nextName() {
        _index++;
        setIndex(_index);
    }

    @FXML
    private void prevName() {
        _index--;
        setIndex(_index);
    }


    /**
     * Starts a background task to record the name using ffmpeg, and another to count every second
     * and indicate to the user how long they've been recording for.
     */
    @FXML
    private void recordAttempt() {

        Task<Void> recordTask = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    _name.addAttempt();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void done() {
                _recentAttempt = _name.getAttempts().get(_name.getAttempts().size()-1);
            }
        };
        TimerTask timerTask = new TimerTask() {
            private int seconds = 0;
            @Override
            public void run() {
                Platform.runLater(() -> {
                    // user must record 1s before stopping
                    _stop.setDisable(false);
                    _timer.setText(seconds + "s");
                });
                seconds++;
            }
        };
        _timeWorker = new Timer();
        setState(State.RECORDING);
        new Thread(recordTask).start();
        _timeWorker.schedule(timerTask,1000,1000);
    }

    @FXML
    private void stopAttempt() {
        FileCommands.cancelRecording();
        setState(State.IDLE);
        _timeWorker.cancel();
    }

    @FXML
    private void playAttempt() {
        _progressIndicator = _attemptIndicator;
        AudioStream clip = null;
        try {
            clip = new AudioStream(new FileInputStream(_recentAttempt));
            AudioPlayer.player.start(clip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        _timeWorker = new Timer();
        setState(State.PLAYING);
        _timeWorker.schedule(new ProgressBarTask(),clip.getLength()/20000, clip.getLength()/20000);
    }

    /**
     * Plays the current file
     */
    @FXML
    private void playRecording(){
        _progressIndicator = _databaseIndicator;
        int length = 0;
        // If the last playback is still playing, end it
        List<AudioStream> nameRecs = new ArrayList<>();
        for (File file: _name.getAudioFiles()) {
            try {
                nameRecs.add(new AudioStream(new FileInputStream(file)));
                length += nameRecs.get(nameRecs.size()-1).getLength();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Task audioTask = new Task() {
            @Override
            protected Object call() throws Exception {
                for (AudioStream clip: nameRecs) {
                    System.out.println(nameRecs.size());
                    AudioPlayer.player.start(clip);
                    wait();
                }
                return null;
            }
        };

        _timeWorker = new Timer();
        setState(State.PLAYING);
        audioTask.run();
        _timeWorker.schedule(new ProgressBarTask(),length/10000, length/10000);
    }

    /**
     * Button method for returning to the start screen.
     */
    @FXML
    private void returnToStartScreen(){
        // Ending any threads for playback/timing
        if (_clip != null) {
            try{
                _clip.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        if (_timeWorker != null) {
            _timeWorker.cancel();
        }
        // commands the referenced Main to set the scene to the start
        _main.setSceneToStart();
    }

    private class ProgressBarTask extends TimerTask {
        private double progress = 0;
        @Override
        public void run() {
            Platform.runLater(() -> {
                _progressIndicator.setProgress(progress/100.0);
            });
            if (progress>100) {
                Platform.runLater(() -> {
                    setState(State.IDLE);
                });
                cancel();
            }
            progress++;
        }
    }
}

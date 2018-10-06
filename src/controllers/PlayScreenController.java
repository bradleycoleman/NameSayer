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
    @FXML private Button _play, _playAttempt;
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
        _index = index;
        _name = _playlist.getFullNames().get(index);
        setState(State.IDLE);
        _timer.setText("0s");
        _databaseIndicator.setProgress(0);
        _attemptIndicator.setProgress(0);
        _nameNumber.setText("Name " + (_index + 1) +" of " + _playlist.getFullNames().size() + " from " + _playlist);
        _currentName.setText(_name.toString());
    }

    /**
     * Set the current state for this screen. The state determines what components are enabled for the user.
     * @param state input the State to change to
     */
    private void setState(State state) {
        if (state == State.PLAYING) {
            _playAttempt.setDisable(true);
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
        } else if (state == State.IDLE) {
            _recordPrompt.setText("Record Attempt:");
            _play.setDisable(false);
            _playAttempt.setDisable(false);
            _record.setDisable(false);
            _record.setVisible(true);
            _stop.setVisible(false);
            _stop.setDisable(true);
            if (_recentAttempt == null) {
                // if no attempt has been made, then the user cannot move ahead
                _next.setDisable(true);
            } else {
                _next.setDisable(false);
            }
            _previous.setDisable(false);
        }

        // Disables the previous or next button based on the current index selected.
        if (_index < 1) {
            _previous.setDisable(true);
        } else {
            _previous.setDisable(false);
        }
        if (_index >= _playlist.getFullNames().size() - 1) {
            _next.setText("Finished!");
        }
    }

    @FXML
    private void nextName() {
        if (_index == _playlist.getFullNames().size() - 1) {
            Alert congrats = new Alert(Alert.AlertType.INFORMATION);
            congrats.setTitle("Congratulations!");
            congrats.setHeaderText("You finished " + _playlist + "!");
            congrats.setContentText(null);
            congrats.showAndWait();
            returnToStartScreen();
        }
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
                _playlist.setCompletion(_index + 1);
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
        if(_recentAttempt == null){
            return;
        }
        _progressIndicator = _attemptIndicator;
        AudioUtils au = new AudioUtils();
        au.playFile(_recentAttempt);
        int clipLength = au.getClipLength(_recentAttempt);

        _timeWorker = new Timer();
        setState(State.PLAYING);
        _timeWorker.schedule(new ProgressBarTask(),clipLength/20000, clipLength/20000);
    }

    /**
     * Plays the current file
     */
    @FXML
    private void playRecording(){
        AudioUtils au = new AudioUtils();
        _progressIndicator = _databaseIndicator;
        int totalLength = 0;
        // If the last playback is still playing, end it

        // Find all the fixed versions of the subnames, then play them all
        List<File> nameRecs = new ArrayList<File>();
        for (File file: _name.getAudioFiles()) {
            File fixedFile = new File("userdata/fixed/fix"+file.getName());
            nameRecs.add(fixedFile);
            totalLength += au.getClipLength(fixedFile);
        }

        if(totalLength != 0){
            _timeWorker = new Timer();
            setState(State.PLAYING);
            _timeWorker.schedule(new ProgressBarTask(),totalLength/10000, totalLength/10000);
        }

        au.playFiles(nameRecs);
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
        if (_nameSayerModel.getPlaylists().contains(_playlist)) {
            System.out.println(_playlist);
            System.out.println("sweety");
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

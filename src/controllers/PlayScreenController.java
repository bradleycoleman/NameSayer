package controllers;

import data.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import main.Main;

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
    @FXML private TextField _loopNo;
    @FXML private Button _playLoop;

    private Playlist _playlist;
    private int _index;
    private NameSayerModel _nameSayerModel;
    private Main _main;
    private FullName _name;
    private File _recentAttempt;
    private enum State {IDLE, PLAYING, RECORDING}
    private Timer _timeWorker;
    private AudioUtils au = new AudioUtils();

    public void initializeData(NameSayerModel nameSayerModel, Main main){
        _nameSayerModel = nameSayerModel;
        _main = main;
        // making sure only numbers are entered into the loop
        _loopNo.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                // if string isn't a single number
                if (!newValue.matches("\\d")) {
                    // remove all non number characters and make only last number the current value
                    String onlyNums = newValue.replaceAll("[^\\d]", "");
                    // checking in case user backspaced
                    if (!onlyNums.isEmpty()) {
                        _loopNo.setText(String.valueOf(onlyNums.charAt(onlyNums.length()-1)));
                    } else {
                        // if user backspaced then the field is set to the old value as it can't be empty
                        _loopNo.setText(oldValue);
                    }
                }
            }
        });
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
        _recentAttempt = null;
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
            _loopNo.setDisable(true);
            _playLoop.setDisable(true);
            _record.setDisable(true);
            _next.setDisable(true);
            _previous.setDisable(true);
        } else if (state == State.RECORDING){
            _recordPrompt.setText("Stop Recording:");
            _record.setVisible(false);
            _stop.setVisible(true);
            _stop.setDisable(true);
            _play.setDisable(true);
            _loopNo.setDisable(true);
            _playLoop.setDisable(true);
            _next.setDisable(true);
            _previous.setDisable(true);
        } else if (state == State.IDLE) {
            _recordPrompt.setText("Record Attempt:");
            _play.setDisable(false);
            _record.setDisable(false);
            _record.setVisible(true);
            _stop.setVisible(false);
            _stop.setDisable(true);
            if (_recentAttempt == null) {
                // if no attempt has been made, then the user cannot move ahead
                _next.setDisable(true);
                _playAttempt.setDisable(true);
                _loopNo.setDisable(true);
                _playLoop.setDisable(true);
            } else {
                _playAttempt.setDisable(false);
                _loopNo.setDisable(false);
                _playLoop.setDisable(false);
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
        } else {
            _next.setText("Next Name");
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
    private void playLoop() {
        int n = Integer.parseInt(_loopNo.getText());
        // Will run playRecording, and playAttempt in a background thread so that the thread can sleep while it waits
        // for the previous method to complete
        Thread bThread = new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                for (int i = 0; i < n; i++) {
                    try {
                        // the playX methods return the length of playback
                        int r = playRecording();
                        Thread.sleep(r / 100);
                        System.out.println("done");
                        int a = playAttempt();
                        Thread.sleep(a / 200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        });
        bThread.start();
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
                _name.addAttempt();
                return null;
            }

            protected void done() {
                _recentAttempt = _name.getAttempts().get(_name.getAttempts().size()-1);
                _playlist.setCompletion(_index + 1);
                Platform.runLater(() -> {
                    setState(PlayScreenController.State.IDLE);
                });
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
        _timeWorker.cancel();
    }

    @FXML
    private int playAttempt() {
        if(_recentAttempt == null){
            return 0;
        }
        _progressIndicator = _attemptIndicator;

        au.playFile(_recentAttempt);
        int clipLength = au.getClipLength(_recentAttempt);

        _timeWorker = new Timer();
        setState(State.PLAYING);
        _timeWorker.schedule(new ProgressBarTask(),clipLength/20000, clipLength/20000);
        return clipLength;
    }

    /**
     * Plays the current file
     */
    @FXML
    private int playRecording(){
        _progressIndicator = _databaseIndicator;
        int totalLength = 0;
        // If the last playback is still playing, end it

        // Find all the fixed versions of the subnames, then play them all
        List<File> nameRecs = new ArrayList<File>();
        for (File file: _name.getAudioFiles()) {
            File fixedFile = new File("names/"+file.getName());
            nameRecs.add(fixedFile);
            totalLength += au.getClipLength(fixedFile);
        }

        if(totalLength != 0){
            _timeWorker = new Timer();
            setState(State.PLAYING);
            _timeWorker.schedule(new ProgressBarTask(),totalLength/10000, totalLength/10000);
        }
        au.playFiles(nameRecs);
        return totalLength;
    }

    /**
     * Button method for returning to the start screen.
     */
    @FXML
    private void returnToStartScreen(){
        // Ending any threads for playback/timing
        if (au.getClip() != null) {
            try{
                au.getClip().close();
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

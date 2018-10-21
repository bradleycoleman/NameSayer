package controllers;

import data.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.Main;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This controls the playback/practice screen of the app. Users can listen to recordings of each of the
 * names in the playlist they determined on the start screen. They can then add their own attempts of the
 * names, which are saved, and can be deleted later. They can update the ratings of the database recording(s)
 * of the names. These ratings are saved to a text file, and displayed to future users when they select the name.
 */
public class PlayScreenController {
    @FXML private Label _nameNumber;
    @FXML private Label _currentName;
    @FXML private Button _record, _delete;
    @FXML private Label _recordPrompt, _playPrompt, _ratePrompt;
    @FXML private Button _stop;
    @FXML private Label _timer;
    @FXML private Button _play, _playAttempt, _playPastAttempt;
    @FXML private ProgressBar _databaseIndicator, _attemptIndicator;
    @FXML private Button _previous;
    @FXML private Button _next;
    @FXML private TextField _loopNo;
    @FXML private Button _playLoop;
    @FXML private TitledPane _subnamePane, _attemptsPane;
    @FXML private ListView<File> _fileListView;
    @FXML private ListView<File> _attemptsListView;

    private Playlist _playlist;
    private ProgressBar _progressIndicator;
    private int _index;
    private NameSayerModel _nameSayerModel;
    private Main _main;
    private FullName _fullName;

    private Name _currentSubname;
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
        // upon selecting a subname, the user is prompted to rate or play it
        _fileListView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<File>() {
            @Override
            public void onChanged(Change<? extends File> c) {
                if (c.next() && c.wasAdded()) {
                    _currentSubname = _fullName.getSubNames().get(_fullName.getAudioFiles().indexOf(_fileListView.getSelectionModel().getSelectedItem()));
                    _playPrompt.setText("Play File for " + _currentSubname);
                    _ratePrompt.setText("Rate File for " + _currentSubname);
                }
            }
        });
        // The files are displayed as their subnames with their rating
        _fileListView.setCellFactory(param -> new ListCell<File>() {
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null || file.toString() == null) {
                    setText(null);
                } else {
                    // getting the respective subname for this file in the fullname
                    Name subname = _fullName.getSubNames().get(_fullName.getAudioFiles().indexOf(file));
                    if (subname.getRating(file) == 2) {
                        setText("✓ " + subname.toString());
                    } else if (subname.getRating(file) == 1) {
                        setText("✕ " + subname.toString());
                    } else {
                        setText("(unrated) " + subname.toString());
                    }
                }
            }
        });
        // The delete and play buttons for past attempts will only be enabled if the past attempts list is >0
        _attemptsListView.getItems().addListener(new ListChangeListener<File>() {
            @Override
            public void onChanged(Change<? extends File> c) {
                if (c.next()) {
                    if (_attemptsListView.getItems().size() > 0) {
                        _playPastAttempt.setDisable(false);
                        _delete.setDisable(false);
                        _attemptsListView.getSelectionModel().selectFirst();
                    } else {
                        _playPastAttempt.setDisable(true);
                        _delete.setDisable(true);
                    }
                }
            }
        });
        // Makes the attempts list show a more user-friendly output than the file name
        _attemptsListView.setCellFactory(param -> new ListCell<File>() {
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null || file.toString() == null) {
                    setText(null);
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Attempt from: ");
                    int i = 0;
                    Pattern num = Pattern.compile("\\d+");
                    Matcher m = num.matcher(file.getName());
                    while (m.find()) {
                        i++;
                        sb.append(file.getName().substring(m.start(), m.end()));
                        if (i<3) {
                            // The first three numbers are dates
                            sb.append("/");
                        } else if (i==3) {
                            // break
                            sb.append(" at ");
                        } else if (i<6) {
                            // The last three numbers are for time
                            sb.append(":");
                        }
                    }
                    setText(sb.toString());
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
        _fullName = _playlist.getFullNames().get(index);
        _subnamePane.setText("Sub-names of " + _fullName.toString());
        _attemptsPane.setText("Past Attempts of " + _fullName.toString());
        setState(State.IDLE);
        _timer.setText("0s");
        _databaseIndicator.setProgress(0);
        _attemptIndicator.setProgress(0);
        _nameNumber.setText("Name " + (_index + 1) +" of " + _playlist.getFullNames().size() + " from " + _playlist);
        _currentName.setText(_fullName.toString());
        _fileListView.getItems().setAll(_fullName.getAudioFiles());
        _fileListView.getSelectionModel().selectFirst();
        // these will be enabled if the setAll call adds anything
        _delete.setDisable(true);
        _playPastAttempt.setDisable(true);
        _attemptsListView.getItems().setAll(_fullName.getAttempts());
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
            if (_fullName.getAttempts().size() == 0) {
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
        } else {
            _index++;
            setIndex(_index);
        }
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
                _fullName.addAttempt();
                return null;
            }

            protected void done() {
                _playlist.setCompletion(_index + 1);
                Platform.runLater(() -> {
                    // adding the most recent attempt to the attempts list
                    _attemptsListView.getItems().add(_fullName.getAttempts().get(_fullName.getAttempts().size() -1));
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
        if(_fullName.getAttempts().size() == 0){
            return 0;
        }
        // The progress worker will reference the attempt progress bar
        _progressIndicator = _attemptIndicator;
        // playing the most recent attempt and scheduling progress bar
        File recentAttempt = _fullName.getAttempts().get(_fullName.getAttempts().size()-1);
        au.playFile(recentAttempt);
        int clipLength = au.getClipLength(recentAttempt);
        _timeWorker = new Timer();
        setState(State.PLAYING);
        _timeWorker.schedule(new ProgressBarTask(),clipLength/20000, clipLength/20000);
        return clipLength;
    }

    @FXML
    private int playRecording(){
        _progressIndicator = _databaseIndicator;
        int totalLength = 0;

        // Get all the files for the name then play
        List<File> nameRecs = new ArrayList<File>();
        for (File file: _fullName.getAudioFiles()) {
            nameRecs.add(file);
            totalLength += au.getClipLength(file);
        }

        if(totalLength != 0){
            _timeWorker = new Timer();
            setState(State.PLAYING);
            _timeWorker.schedule(new ProgressBarTask(),totalLength/10000, totalLength/10000);
        }
        au.playFiles(nameRecs);
        return totalLength;
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
    private void playPastAttempt() {
        File selectedAttempt = _attemptsListView.getSelectionModel().getSelectedItem();
        if (selectedAttempt != null) {
            if (au.getClip() != null) {
                try{
                    au.getClip().close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            au.playFile(selectedAttempt);
        }
    }

    @FXML
    private void playFile() {
        // playing the audio file at the corresponding index for the currently selected subname
        if (_currentSubname != null) {
            au.playFile(_fullName.getAudioFiles().get(_fullName.getSubNames().indexOf(_currentSubname)));
        }
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

    @FXML
    private void deleteAttempt() {
        File selectedAttempt = _attemptsListView.getSelectionModel().getSelectedItem();
        if (selectedAttempt != null) {
            Alert deleteAlert = new Alert(Alert.AlertType.CONFIRMATION,"Are you sure you want to delete " +
                    selectedAttempt.getName() + "?", ButtonType.NO, ButtonType.YES);
            deleteAlert.setHeaderText(null);
            deleteAlert.setTitle("Delete Past Attempt");
            Optional<ButtonType> result = deleteAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES){
                _fullName.deleteAttempt(selectedAttempt);
                _attemptsListView.getItems().remove(selectedAttempt);
            }
        }
    }

    @FXML
    private void rateGood() {
        if (_currentSubname != null) {
            _currentSubname.updateRatingOfFile(_fileListView.getSelectionModel().getSelectedItem(), 2);
        }
        _fileListView.refresh();
        _nameSayerModel.writeGoodBadNames();
    }

    @FXML
    private void rateBad() {
        if (_currentSubname != null) {
            _currentSubname.updateRatingOfFile(_fileListView.getSelectionModel().getSelectedItem(), 1);
        }
        _fileListView.refresh();
        _nameSayerModel.writeGoodBadNames();
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

package controllers;

import data.FileCommands;
import data.NameSayerModel;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import main.Main;
import data.Name;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.sampled.*;

/**
 * This controls the playback/practice screen of the app. Users can listen to recordings of each of the
 * names in the playlist they determined on the start screen. They can then add their own attempts of the
 * names, which are saved, and can be deleted later. They can update the ratings of the database recording(s)
 * of the names. These ratings are saved to a text file, and displayed to future users when they select the name.
 */
public class PlayScreenController {
    @FXML private Button _delete;
    @FXML private Label _nameNumber;
    @FXML private Label _currentName;
    @FXML private Button _record;
    @FXML private Label _recordPrompt;
    @FXML private Button _stop;
    @FXML private Label _timer;
    @FXML private Button _play;
    @FXML private ProgressBar _progressIndicator;
    @FXML private Button _previous;
    @FXML private Button _next;
    @FXML private Label _ratingPrompt;
    @FXML private Slider _rating;
    @FXML private ChoiceBox<File> _chooser;

    private ObservableList _items;
    private List<Name> _playlist;
    private int _index;
    private NameSayerModel _nameSayerModel;
    private Main _main;
    private Name _name;
    private enum State {IDLE, PLAYING, RECORDING}
    private Timer _timeWorker;
    private AudioInputStream _audio;
    private Clip _clip;

    public void initializeData(NameSayerModel nameSayerModel, Main main){
        _nameSayerModel = nameSayerModel;
        _main = main;
    }

    public void startPractice() {
        _playlist = _nameSayerModel.getPlaylist();
        setIndex(0);
    }

    /**
     * Refreshes various components based on an inputted index of the playlist
     * @param index int which signifies the index of the name to be practiced.
     */
    private void setIndex(int index) {
        setState(State.IDLE);
        _index = index;
        _name = _playlist.get(_index);
        _progressIndicator.setProgress(0);
        _chooser.setConverter(new StringConverter<File>() {
            @Override
            public String toString(File file) {
                if (file.toString().contains("userdata/attempts")) {
                    return ("Practice Attempt: " + file.getName());
                } else if (_name.getFiles().size() > 1) {
                    return ("Database Recording: " + file.getName());
                }
                return ("Database Recording");
            }

            @Override
            public File fromString(String string) {
                return null;
            }
        });
        List<File> all = new ArrayList<>();
        all.addAll(_name.getFiles());
        all.addAll(_name.getAttempts());
        _items = FXCollections.observableArrayList();
        _items.addAll(_name.getFiles());
        _items.addAll(_name.getAttempts());
        _chooser.setItems(_items);
        _chooser.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
                if (newValue != null) {
                    if (newValue.toString().contains("userdata/attempts")) {
                        _delete.setDisable(false);
                        _rating.setVisible(false);
                        _rating.setDisable(true);
                        _ratingPrompt.setVisible(false);
                    } else {
                        _delete.setDisable(true);
                        _rating.setDisable(false);
                        _rating.setVisible(true);
                        _ratingPrompt.setVisible(true);
                    }
                }
            }
        });
        _chooser.setValue(_name.getFiles().get(0));
        _nameNumber.setText("Name " + (_index + 1) +" of " + _playlist.size());
        _currentName.setText(_playlist.get(_index).toString());
        if (_index < 1) {
            _previous.setDisable(true);
        } else {
            _previous.setDisable(false);
        }
        if (_index >= _playlist.size() - 1) {
            _next.setDisable(true);
        } else {
            _next.setDisable(false);
        }
    }

    private void setState(State state) {
        if (state == State.PLAYING) {
            _chooser.setDisable(true);
            _play.setDisable(true);
            _record.setDisable(true);
            _delete.setDisable(true);
        } else if (state == State.RECORDING){
            _recordPrompt.setText("Stop Recording:");
            _chooser.setDisable(true);
            _record.setVisible(false);
            _stop.setVisible(true);
            _stop.setDisable(false);
            _delete.setDisable(true);
            _play.setDisable(true);
        } else {
            _recordPrompt.setText("Record Attempt:");
            _chooser.setDisable(false);
            _play.setDisable(false);
            _record.setDisable(false);
            _record.setVisible(true);
            _stop.setVisible(false);
            _stop.setDisable(true);
            _delete.setDisable(false);
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
                // Add the attempt which was just recorded.
                _items.add(_name.getAttempts().get(_name.getAttempts().size()-1));
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
        _timeWorker.schedule(timerTask,1000l,1000l);
    }

    @FXML
    private void stopAttempt() {
        FileCommands.cancelRecording();
        setState(State.IDLE);
        _timeWorker.cancel();
    }

    /**
     * Plays the current name.
     */
    @FXML
    private void playRecording(){

        // If the last playback is still playing, end it
        if (_clip != null) {
            _clip.stop();
        }

        // If there is nothing selected, then tell the user to select something.
        if(_chooser.getSelectionModel().getSelectedItem()==null){
            System.out.println("It's empty fam, pick a recording");
            return;
        }

        try {
            _clip = AudioSystem.getClip();
            _audio = AudioSystem.getAudioInputStream(_chooser.getValue());
            _clip.open(_audio);
            _clip.start();
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        TimerTask progessBar = new TimerTask() {
            private double progress = 0;
            @Override
            public void run() {
                Platform.runLater(() -> {
                    _progressIndicator.setProgress(progress/100.0);
                });
                if (!_clip.isRunning()) {
                    Platform.runLater(() -> {
                        setState(State.IDLE);
                    });
                    cancel();
                }
                progress++;
            }
        };

        _timeWorker = new Timer();
        setState(State.PLAYING);
        _timeWorker.schedule(progessBar,_clip.getMicrosecondLength()/100000, _clip.getMicrosecondLength()/100000);
    }

    @FXML
    private void deleteRecording() {
        File toDelete = _chooser.getValue();
        if (toDelete.toString().contains("userdata/attempts")) {
            _name.deleteAttempt(toDelete);
            // Setting the selection to a database recording
            _chooser.setValue(_name.getFiles().get(0));
            // Removing deleted item as option
            _chooser.getItems().remove(toDelete);
        }
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
        _main.setSceneToStart();
    }


}

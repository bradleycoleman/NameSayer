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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.StringConverter;
import main.Main;
import data.Name;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Edit this, along with the playScreen.fxml file, to make the play screen
 */
public class PlayScreenController {
    @FXML private Button _testButton;
    @FXML private Button _delete;
    @FXML private Label _nameNumber;
    @FXML private Label _currentName;
    @FXML private Circle _record;
    @FXML private Label _recordPrompt;
    @FXML private Rectangle _stop;
    @FXML private Label _timer;
    @FXML private Polygon _play;
    @FXML private ProgressBar _progressIndicator;
    @FXML private Button _previous;
    @FXML private Button _next;
    @FXML private Label _ratingPrompt;
    @FXML private Slider _rating;
    @FXML private ChoiceBox<File> _chooser;

    private Thread _soundThread;
    private List<Name> _playlist;
    private int _index;
    private NameSayerModel _nameSayerModel;
    private Main _main;
    private Name _name;
    private enum State {IDLE, PLAYING, RECORDING}
    private Timer _timeWorker;

    @FXML
    private void initialize() {
    }

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

        _index = index;
        _name = _playlist.get(_index);
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
        List<File> all = _name.getFiles();
        all.addAll(_name.getAttempts());
        ObservableList items = FXCollections.observableArrayList(all);
        _chooser.setItems(items);
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
            _record.setDisable(true);
        } else if (state == State.RECORDING){
            _recordPrompt.setText("Stop Recording:");
            _record.setVisible(false);
            _record.setDisable(true);
            _stop.setVisible(true);
            _stop.setDisable(false);
        } else {
            _recordPrompt.setText("Record Attempt:");
            _record.setDisable(false);
            _record.setVisible(true);
            _stop.setVisible(false);
            _stop.setDisable(true);
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
                    // if FileCommands.cancelRecording is called, an InterruptedException
                    // will be thrown
                    done();
                }
                return null;
            }
        };
        TimerTask timerTask = new TimerTask() {
            private int seconds = 0;
            @Override
            public void run() {
                Platform.runLater(() -> {
                    _timer.setText(seconds + "s");
                });
                seconds++;
            }
        };
        _timeWorker = new Timer();
        _timeWorker.schedule(timerTask,1000l,1000l);
        setState(State.RECORDING);
        new Thread(recordTask).start();
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
        if (_soundThread != null) {
            _soundThread.interrupt();
        }

        // If there is nothing selected, then tell the user to select something.
        if(_chooser.getSelectionModel().getSelectedItem()==null){
            System.out.println("It's empty fam, pick a recording");
            return;
        }

        setState(State.PLAYING);
        _soundThread = new Thread(new Runnable(){
            @Override
            public void run() {
                AudioInputStream audio;
                try {
                    audio = AudioSystem.getAudioInputStream(_chooser.getValue());
                    Clip clip = AudioSystem.getClip();
                    clip.open(audio);
                    clip.start();
                } catch (UnsupportedAudioFileException | IOException e) {
                    e.printStackTrace();
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }

            }
        });

        _soundThread.run();
    }

    /**
     * Button method for returning to the start screen.
     */
    @FXML
    private void returnToStartScreen(){
        // If the last playback is still playing, end it
        if (_soundThread != null) {
            _soundThread.interrupt();
        }
        _main.setSceneToStart();
    }


}

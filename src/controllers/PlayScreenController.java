package controllers;

import data.NameSayerModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import main.Main;
import data.Name;
import java.io.IOException;
import java.util.List;
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

    private List<Name> _playlist;
    private int _index;
    private NameSayerModel _nameSayerModel;
    private Main _main;

    private Name _nameFile;

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
        _nameFile = _playlist.get(_index);
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

    @FXML
    private void playRecording(){

        Thread soundThread = new Thread(new Runnable(){
            @Override
            public void run() {
                AudioInputStream audio;
                try {
                    audio = AudioSystem.getAudioInputStream(_nameFile.getFile());
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

        soundThread.run();
    }

    /**
     * Button method for returning to the start screen.
     */
    @FXML
    private void returnToStartScreen(){
        _main.setSceneToStart();
    }


}

package controllers;

import data.Name;
import data.NameSayerModel;
import data.Playlist;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import main.Main;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;


public class CurateScreenController {
    @FXML private Button _shuffle;
    @FXML private Button _sortButton;
    @FXML private ListView<Name> _playlistView;
    @FXML private ListView<Name> _nameslistView;
    @FXML private Label _name;
    @FXML private TitledPane _playlistBox, _currentName;
    @FXML private ChoiceBox _fileChooser;

    private Main _main;

    private ListView<Name> _currentView = null;
    private ListView<Name> _otherView = null;
    private Name _nameObject;
    private Playlist _playlist;
    private NameSayerModel _nameSayerModel = null;


    /**
     * Initializes the controller class.
     */
    public void initializeData(NameSayerModel nameSayerModel, Main main){
        _main = main;
        _nameSayerModel = nameSayerModel;

    }

    public void newPlaylist(String name) {
        _playlist = new Playlist(name);
        refreshName();
    }

    @FXML
    private void rateGood() {
        System.out.println("nice");
    }

    @FXML
    private void rateBad() {
        System.out.println("yuck");
    }

    @FXML
    private void play() {
        System.out.println("play");
    }
    @FXML
    private void rename() {
        TextInputDialog newName = new TextInputDialog(_playlist.toString());

        newName.setTitle("Rename Playlist");
        newName.setHeaderText("Enter a new name");
        newName.setContentText("Name:");

        Optional<String> result = newName.showAndWait();

        result.ifPresent(name -> {
            _playlist.rename(name);
        });
        refreshName();
    }

    private void refreshName() {
        _playlistBox.setText(_playlist.toString());
        _name.setText(_playlist.toString());
    }

    /**
     * Method to shuffle the playlist.
     */
    @FXML
    private void shuffle() {
        Collections.shuffle(_nameSayerModel.getPlaylist());
        _playlistView.setItems(FXCollections.observableArrayList(_nameSayerModel.getPlaylist()));
    }

    /**
     * Method to sort the playlist by alphabetical order.
     */
    @FXML
    private void sort(){
        Collections.sort(_nameSayerModel.getPlaylist());
        _playlistView.setItems(FXCollections.observableArrayList(_nameSayerModel.getPlaylist()));
    }

    @FXML
    private void exit() {
        _main.setSceneToStart();
    }

}

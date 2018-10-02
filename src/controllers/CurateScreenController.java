package controllers;

import data.Name;
import data.NameSayerModel;
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


public class CurateScreenController {
    @FXML private Button _practice;
    @FXML private Button _testMic;
    @FXML private Button _switch;
    @FXML private Button _shuffle;
    @FXML private Button _sortButton;
    @FXML private Label _currentName;
    @FXML private TextArea _nameDetails;
    @FXML private ListView<Name> _playlistView;
    @FXML private ListView<Name> _nameslistView;
    @FXML private TextField _searchBar;

    private Main _main;

    private ListView<Name> _currentView = null;
    private ListView<Name> _otherView = null;

    private NameSayerModel _nameSayerModel = null;


    /**
     * Initializes the controller class.
     */
    public void initializeData(NameSayerModel nameSayerModel, Main main){
        _main = main;
        _nameSayerModel = nameSayerModel;

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

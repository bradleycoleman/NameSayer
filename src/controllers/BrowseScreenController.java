package controllers;

import data.NameSayerModel;
import data.Playlist;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import main.Main;

import java.util.ArrayList;
import java.util.List;

public class BrowseScreenController {
    @FXML private ListView<Playlist> _playlists;
    private Main _main;
    private NameSayerModel _namesModel;

    public void initializeData(NameSayerModel namesModel, Main main) {
        _main = main;
        _namesModel = namesModel;
        _playlists.setItems(FXCollections.observableArrayList(namesModel.getPlatlists()));
    }

    @FXML
    private void returnToStart() {
        _main.setSceneToStart();
    }

    @FXML
    private void createNew() {
        _main.setSceneToCurate();
    }
}

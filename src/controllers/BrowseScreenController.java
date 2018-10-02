package controllers;

import data.NameSayerModel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import main.Main;

import java.util.ArrayList;
import java.util.List;

public class BrowseScreenController {
    @FXML private ListView<String> _playlists;
    private Main _main;
    private NameSayerModel _namesModel;

    public void initializeData(NameSayerModel namesModel, Main main) {
        _main = main;
        _namesModel = namesModel;
        List<String> list = new ArrayList<>();
        for (int i = 1; i<100; i++) {
            list.add("Playlist" + i);
        }
        _playlists.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void returnToStart() {
        _main.setSceneToStart();
    }
}

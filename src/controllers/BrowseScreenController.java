package controllers;

import data.FullName;
import data.NameSayerModel;
import data.Playlist;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import main.Main;

import java.util.Optional;

public class BrowseScreenController {
    @FXML private ListView<Playlist> _playlists;
    @FXML private ListView<FullName> _playlist;
    private Main _main;
    private NameSayerModel _namesModel;
    private Playlist _currentPlaylist;

    public void initializeData(NameSayerModel namesModel, Main main) {
        _main = main;
        _namesModel = namesModel;
        _playlists.setItems(FXCollections.observableArrayList(namesModel.getPlaylists()));
        _playlists.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<Playlist>() {
            @Override
            public void onChanged(Change<? extends Playlist> c) {
                if (c.next() && c.wasAdded()) {
                    _playlist.setItems(FXCollections.observableArrayList(_playlists.getSelectionModel().getSelectedItem()
                            .getFullNames()));
                }
            }
        });
    }

    public void update() {
        System.out.println(_namesModel.getPlaylists().size());
        _playlists.setItems(FXCollections.observableArrayList(_namesModel.getPlaylists()));
        _playlists.getSelectionModel().selectFirst();
    }
    @FXML
    private void returnToStart() {
        _main.setSceneToStart();
    }

    @FXML
    private void createNew() {
        TextInputDialog newName = new TextInputDialog();

        newName.setTitle("Name of new playlist");
        newName.setHeaderText(null);
        newName.setContentText("Enter a name:");

        Optional<String> result = newName.showAndWait();

        result.ifPresent(name -> {
            _main.setSceneToCurateNew(name);
        });
    }

    @FXML
    private void edit() {
        _main.setSceneToCurateEdit(_playlists.getSelectionModel().getSelectedItem());
    }
}

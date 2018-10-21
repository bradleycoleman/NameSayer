package controllers;

import data.AudioLevelListener;
import data.FileCommands;
import data.FullName;
import data.NameSayerModel;
import data.Playlist;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import main.Main;

import java.util.Optional;
import java.util.regex.Pattern;

public class BrowseScreenController {
    @FXML private ListView<Playlist> _playlists;
    @FXML private ListView<FullName> _playlist;
    @FXML private TitledPane _playlistBox;
    @FXML private TextField _searchBar;
    @FXML private ProgressBar _micLevel;
    @FXML private Button _practice;
    @FXML private GridPane _grid;
    
    private Main _main;
    private NameSayerModel _namesModel;
    private Playlist _currentPlaylist;
    private AudioLevelListener all;
    
    public void initializeData(NameSayerModel namesModel, Main main) {
        _main = main;
        _namesModel = namesModel;
        // When a playlist is selected, the names on the right will change to be the names from the playlist
        _playlists.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<Playlist>() {
            @Override
            public void onChanged(Change<? extends Playlist> c) {
                if (c.next()) {
                    if (c.wasAdded()) {
                        _currentPlaylist = _playlists.getSelectionModel().getSelectedItem();
                        _playlist.setItems(FXCollections.observableArrayList(_currentPlaylist.getFullNames()));
                        _playlistBox.setText(_currentPlaylist.toString());
                        _practice.setDisable(false);
                    } else {
                        _currentPlaylist = null;
                        _playlistBox.setText("Current Playlist");
                        _playlist.getItems().clear();
                        _practice.setDisable(true);
                    }
                }
            }
        });
        _playlists.setCellFactory(param -> new ListCell<Playlist>() {
            @Override
            protected void updateItem(Playlist item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.toString() == null) {
                    setText(null);
                } else {
                    setText(item.toString() + "\t (Completed " + item.getCompletion() + "/" + item.getFullNames().size() + ")");
                }
            }
        });
        update();

        Thread listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                all = new AudioLevelListener(_main);
            }
        });
        _practice.setDisable(true);
        listenThread.start();
    }

    public void update() {
        _playlists.setItems(FXCollections.observableArrayList(_namesModel.getPlaylists()));
        _playlists.refresh();
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
            // checking if the name is unique
            for (Playlist playlist : _namesModel.getPlaylists()) {
                if (playlist.toString().equals(name)) {
                    Alert duplicateName = new Alert(Alert.AlertType.ERROR, "This name is already in use by " +
                            "another playlist", ButtonType.OK);
                    duplicateName.setHeaderText("Invalid Playlist Name");
                    duplicateName.setTitle("Could not create Playlist");
                    duplicateName.showAndWait();
                    return;
                }
            }
            // checking if name is of the correct length and uses only the allowed characters
            if (name.matches("[A-Za-z0-9 ,-]{1,15}")) {
                // if it is ok, set the scene to the curator
                _main.setSceneToCurateNew(name);
            } else {
                Alert invalidChar = new Alert(Alert.AlertType.ERROR, "Names must be between 1 and 15 \nword " +
                        "characters long");
                invalidChar.setHeaderText("Invalid Playlist Name");
                invalidChar.setTitle("Could not create Playlist");
                invalidChar.showAndWait();
            }

        });
    }

    @FXML
    private void delete() {
        // User is asked to confirm delete before file is deleted
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Note: This will delete all attempt audio files" +
                " except\nthose for names included in other playlists");
        alert.setHeaderText("Delete " + _currentPlaylist + " and Attempts?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            _namesModel.deletePlaylist(_currentPlaylist);
            update();
        }
    }

    @FXML
    private void searchPlaylist(){
        _namesModel.filterPlaylistList(_searchBar.getText());
        _playlists.setItems(FXCollections.observableArrayList(_namesModel.getFilteredPlaylistList()));
    }

    @FXML
    private void practice() {
        // If the selected playlist is empty, then the user will be told they cannot practice it and will be given the
        // option to edit it instead.
        if (_currentPlaylist.getFullNames().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Cannot practice " + _currentPlaylist +" as the playlist is empty.\n" +
                    "Do you want to edit this playlist instead?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText(null);
            alert.setTitle("ERROR: Cannot practice empty playlist");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.YES) {
                edit();
            }
        } else {
            _main.setSceneToPlay(_currentPlaylist);
        }
    }


    @FXML
    private void edit() {
        _main.setSceneToCurateEdit(_currentPlaylist);
    }
    
    public ProgressBar getMicLevelBar() {
    	return _micLevel;
    }
}

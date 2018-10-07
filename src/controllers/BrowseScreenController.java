package controllers;

import data.AudioLevelListener;
import data.FileCommands;
import data.FullName;
import data.NameSayerModel;
import data.Playlist;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.Main;

import java.util.Optional;

public class BrowseScreenController {
    @FXML private ListView<Playlist> _playlists;
    @FXML private ListView<FullName> _playlist;
    @FXML private TitledPane _playlistBox;
    @FXML private TextField _searchBar;
    @FXML private ProgressBar _micLevel;
    
    private Main _main;
    private NameSayerModel _namesModel;
    private Playlist _currentPlaylist;
    private AudioLevelListener all;
    
    public void initializeData(NameSayerModel namesModel, Main main) {
        _main = main;
        _namesModel = namesModel;
        _playlists.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<Playlist>() {
            @Override
            public void onChanged(Change<? extends Playlist> c) {
                if (c.next() && c.wasAdded()) {
                    _currentPlaylist = _playlists.getSelectionModel().getSelectedItem();
                    _playlist.setItems(FXCollections.observableArrayList(_currentPlaylist.getFullNames()));
                    _playlistBox.setText(_currentPlaylist.toString());
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
            _main.setSceneToCurateNew(name);
        });
    }

    @FXML
    private void delete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete: " + _currentPlaylist +"?");
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            FileCommands.deleteFile(_currentPlaylist.getFile());
            _namesModel.getPlaylists().remove(_currentPlaylist);
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
        _main.setSceneToPlay(_currentPlaylist);
    }


    @FXML
    private void edit() {
        _main.setSceneToCurateEdit(_currentPlaylist);
    }
    
    public ProgressBar getMicLevelBar() {
    	return _micLevel;
    }
}

package controllers;

import main.Main;
import data.NameSayerModel;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import data.Name;

import java.io.File;
import java.util.*;


public class StartScreenController {
    @FXML private Button _practice;
    @FXML private Button _testMic;
    @FXML private Button _switch;
    @FXML private CheckBox _shuffle;
    @FXML private Label _currentName;
    @FXML private TextArea _nameDetails;
    @FXML private ListView<Name> _playlistView;
    @FXML private ListView<Name> _nameslistView;
    @FXML private TextField _searchBar;

    private Main _main;

    private ListView<Name> _currentView = null;
    private ListView<Name> _otherView = null;

    private NameSayerModel _nameSayerModel = null;

    @FXML
    private void initialize(){

    }

    /**
     * Initializes the controller class.
     */
    public void initializeData(NameSayerModel nameSayerModel, Main main){
        _main = main;
        _nameSayerModel = nameSayerModel;

        _nameslistView.setItems(FXCollections.observableArrayList(_nameSayerModel.getNameslist()));
        _playlistView.setItems(FXCollections.observableArrayList(_nameSayerModel.getPlaylist()));

        _nameslistView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener() {
            @Override
            public void onChanged(Change c) {
                if (c.next() & c.wasAdded()) {
                    swapList(_nameslistView,_playlistView);
                }
            }
        });

        _playlistView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener() {
            @Override
            public void onChanged(Change c) {
                if (c.next() & c.wasAdded()) {
                    swapList(_playlistView,_nameslistView);
                }
            }
        });
    }

    /**
     * This method will make the currently selected name whatever is selected in the "selectedList", while
     * deselecting the other list (if it was selected) to avoid confusion.
     * @param selectedView The list that was last clicked on
     * @param oldView The other list
     */
    private void swapList(ListView selectedView, ListView oldView) {
        _currentName.setText(selectedView.getSelectionModel().getSelectedItem().toString());
        _nameDetails.setText(((Name) selectedView.getSelectionModel().getSelectedItem()).getDetails());
        // If the other list was selected, deselect.
        if (oldView.getSelectionModel().getSelectedItems() != null) {
            oldView.getSelectionModel().clearSelection();
        }
        if (selectedView == _playlistView) {
            _switch.setText("Remove from Playlist");

        } else {
            _switch.setText("Add to Playlist");
        }
        _currentView = selectedView;
        _otherView = oldView;
    }

    /**
     * This will swap the currently selected name from the list it is currently in, to the other list.
     */
    @FXML
    private void swapName(){
        // Checking if no name/list is currently selected
        if (_currentView == null) {
            _nameDetails.setText("No name selected");
            _currentName.setText("");
        } else if (_currentView.getItems().isEmpty()) {
            // The current list may be empty if all items are moved to the other.
            _nameDetails.setText("No name selected");
            _currentName.setText("");
        } else {
            // Adding the selected name to the other list, then removing it from the current list.
            _otherView.getItems().add(_currentView.getSelectionModel().getSelectedItem());
            _currentView.getItems().remove(_currentView.getSelectionModel().getSelectedItem());

            // Only the other list needs to be re-sorted, as it has been added to.
            _otherView.setItems(FXCollections.observableArrayList(_otherView.getItems().sorted()));
        }
    }

    /**
     * Method to filter the names list by the contents of the search bar.
     */
    @FXML
    private void searchNamesList(){
        _nameSayerModel.filterNamesList(_searchBar.getText());
        _nameslistView.setItems(FXCollections.observableArrayList(_nameSayerModel.getFilteredNamesList()));
    }

    /**
     * Method to start the playScreen upon pressing of the practice button.
     */
    @FXML
    private void startPractice(){
        if (_playlistView.getItems().isEmpty()) {
            _currentName.setText("");
            _nameDetails.setText("Add names to your playlist \nfirst!");
        } else {
            // Set scene to start scene
            _nameSayerModel.setPlaylist(_playlistView.getItems());
            _main.setSceneToPlay();
        }
    }
}

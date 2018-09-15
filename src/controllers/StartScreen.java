package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.Name;

import java.io.File;


public class StartScreen {
    @FXML private Button _practice;
    @FXML private Button _testMic;
    @FXML private Button _switch;
    @FXML private CheckBox _shuffle;
    @FXML private Label _currentName;
    @FXML private TextArea _nameDetails;
    @FXML private ListView _playlist;
    @FXML private ListView _names;
    private ListView _currentList = null;
    private ListView _otherList = null;


    /**
     * Initializes the controller class.
     */
    @FXML
    private void initialize() {
        // Initializing the list of names
        ListView<Name> listView = new ListView<>();
        File folder = new File("names");
        File[] listOfFiles = folder.listFiles();
        ObservableList<Name> names = FXCollections.observableArrayList();
        for (File file : listOfFiles) {
            names.add(new Name(file.getName()));
        }
        _names.setItems(names);

        _names.getSelectionModel().getSelectedItems().addListener(new ListChangeListener() {
            @Override
            public void onChanged(Change c) {
                if (c.next() & c.wasAdded()) {
                    swapList(_names,_playlist);
                }
            }
        });

        _playlist.getSelectionModel().getSelectedItems().addListener(new ListChangeListener() {
            @Override
            public void onChanged(Change c) {
                if (c.next() & c.wasAdded()) {
                    swapList(_playlist,_names);
                }
            }
        });
    }

    /**
     * This method will make the currently selected name whatever is selected in the "selectedList", while
     * deselecting the other list (if it was selected) to avoid confusion.
     * @param selectedList The list that was last clicked on
     * @param oldList The other list
     */
    private void swapList(ListView selectedList, ListView oldList) {
        _currentName.setText(selectedList.getSelectionModel().getSelectedItem().toString());
        _nameDetails.setText(((Name) selectedList.getSelectionModel().getSelectedItem()).getDetails());
        // If the other list was selected, deselect.
        if (oldList.getSelectionModel().getSelectedItems() != null) {
            oldList.getSelectionModel().clearSelection();
        }
        if (selectedList == _playlist) {
            _switch.setText("Remove from Playlist");

        } else {
            _switch.setText("Add to Playlist");
        }
        _currentList = selectedList;
        _otherList = oldList;
    }

    /**
     * This will swap the currently selected name from the list it is currently in, to the other list.
     */
    @FXML
    private void swapName(){
        // Checking if no name/list is currently selected
        if (_currentList == null) {
            _nameDetails.setText("No name selected");
            _currentName.setText("");
        } else {
            // the current list may be empty if all items are moved to the other
            if (_currentList.getItems().isEmpty()) {
                _nameDetails.setText("No name selected");
                _currentName.setText("");
            } else {
                _otherList.getItems().add(_currentList.getSelectionModel().getSelectedItem());
                _currentList.getItems().remove(_currentList.getSelectionModel().getSelectedItem());
            }
        }
    }


}

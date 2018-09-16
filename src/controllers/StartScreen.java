package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.Main;
import main.Name;

import java.io.File;
import java.util.*;


public class StartScreen {
    @FXML private Button _practice;
    @FXML private Button _testMic;
    @FXML private Button _switch;
    @FXML private CheckBox _shuffle;
    @FXML private Label _currentName;
    @FXML private TextArea _nameDetails;
    @FXML private ListView<Name> _playlist;
    @FXML private ListView<Name> _names;
    @FXML private TextField _searchBar;

    private ListView<Name> _currentList = null;
    private ListView<Name> _otherList = null;

    private List<Name> _allNamesList = null;

    /**
     * Initializes the controller class.
     */
    @FXML
    private void initialize() {
        // Initializing the list of names
        File folder = new File("names");
        File[] listOfFiles = folder.listFiles();
        List<Name> names = new ArrayList();
        for (File file : listOfFiles) {
            names.add(new Name(file.getName()));
        }
        names.sort(new Comparator<Name>() {
            @Override
            public int compare(Name o1, Name o2) {
                return (o1.compareTo(o2));
            }
        });

        _allNamesList = names;
        _names.setItems(FXCollections.observableArrayList(names));

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
            // The current list may be empty if all items are moved to the other.
            if (_currentList.getItems().isEmpty()) {
                _nameDetails.setText("No name selected");
                _currentName.setText("");
            } else {
                // Adding the selected name to the other list, then removing it from the current list.
                _otherList.getItems().add(_currentList.getSelectionModel().getSelectedItem());
                _currentList.getItems().remove(_currentList.getSelectionModel().getSelectedItem());

                // Only the other list needs to be re-sorted, as it has been added to.
                _otherList.setItems(FXCollections.observableArrayList(_otherList.getItems().sorted()));
            }
        }
    }

    /**
     * Method to filter the names list by the contents of the search bar.
     */
    @FXML
    private void searchNamesList(){

        List<Name> filteredNames = new ArrayList<Name>();

        _names.setItems(FXCollections.observableArrayList(_allNamesList));

        // Add all names that contain the text from the search bar
        for(Name n: _names.getItems()){
            if(n.toString().toLowerCase().contains(_searchBar.getText().toLowerCase())){
                filteredNames.add(n);
            }
        }

        // Sort with priority of the search bar text appearing earlier in the name.
        filteredNames.sort(new Comparator<Name>() {
            @Override
            public int compare(Name o1, Name o2) {
                String i = o1.toString().toLowerCase().indexOf(_searchBar.getText().toLowerCase())+"";
                String j = o2.toString().toLowerCase().indexOf(_searchBar.getText().toLowerCase())+"";
                return (i.compareTo(j));
            }
        });

        _names.setItems(FXCollections.observableArrayList(filteredNames));
    }

    /**
     * Method to start the playScreen upon pressing of the practice button.
     */
    @FXML
    private void startPractice(){
        if (_playlist.getItems().isEmpty()) {
            _currentName.setText("");
            _nameDetails.setText("Add names to your playlist \nfirst!");
        } else {
            Main.SECONDARY_STAGE.show();
            Main.PRIMARY_STAGE.hide();
        }
    }
}

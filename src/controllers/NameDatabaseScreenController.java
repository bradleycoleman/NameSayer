package controllers;

import data.AudioUtils;
import data.Name;
import data.NameSayerModel;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.util.StringConverter;
import main.Main;

import java.io.File;


public class NameDatabaseScreenController {
    @FXML private ListView<Name> _names;
    @FXML private ChoiceBox<File> _fileChooser;
    @FXML private TitledPane _nameOptions;
    @FXML private TextField _searchBar;


    private Main _main;
    private NameSayerModel _namesModel;
    private Name _currentName;

    public void initializeData(NameSayerModel namesModel, Main main) {
        _namesModel = namesModel;
        _main = main;
        // List of names is set to be the names list from the nameSayerModel
        _names.setItems(FXCollections.observableArrayList(namesModel.getDatabase()));
        // On the selection of a name, change the file selector to show options regarding the selected name
        _names.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<Name>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Name> c) {
                if (c.next() && c.wasAdded()) {
                    _currentName = _names.getSelectionModel().getSelectedItem();
                    _fileChooser.setItems(FXCollections.observableArrayList(_currentName.getFiles()));
                    _fileChooser.getSelectionModel().select(0);
                    _nameOptions.setText("Files for " + _names.getSelectionModel().getSelectedItem().toString());
                }
            }
        });
        // The file chooser displays files with ratings
        _fileChooser.setConverter(new StringConverter<File>() {
            @Override
            public String toString(File file) {
                if (_currentName.getRating(file) == 2) {
                    return "✓ " + file.getName();
                } else if (_currentName.getRating(file) == 1) {
                    return "✕ " + file.getName();
                } else {
                    return "(unrated) " + file.getName();
                }
            }
            @Override
            public File fromString(String string) {
                return null;
            }
        });
        // The names list automatically selects the first name
        _names.getSelectionModel().select(0);
    }

    @FXML
    private void rateGood() {
        updateRatingCurrentFile(2);
    }

    @FXML
    private void rateBad() {
        updateRatingCurrentFile(1);
    }

    /**
     * updates rating for name, then refreshes choices by removing and replacing.
     */
    private void updateRatingCurrentFile(int rating) {
        File selected = (File)_fileChooser.getValue();
        // updating the name object's rating
        _currentName.updateRatingOfFile((File) _fileChooser.getValue(),rating);
        // writing it to the file
        _namesModel.writeGoodBadNames();
        // resetting filechooser
        _fileChooser.getItems().clear();
        _fileChooser.setItems(FXCollections.observableArrayList(_currentName.getFiles()));
        _fileChooser.getSelectionModel().select(selected);
    }

    @FXML
    private void play() {
        AudioUtils au = new AudioUtils();
        if(_currentName == null){
            return;
        }
        au.playFile((File)_fileChooser.getValue());
    }

    @FXML
    private void searchNames() {
        _namesModel.filterNamesList(_searchBar.getText());
        _names.setItems(FXCollections.observableArrayList(_namesModel.getFilteredNamesList()));
    }

    @FXML
    private void returnToStart() {
        _main.setSceneToStart();
    }

}

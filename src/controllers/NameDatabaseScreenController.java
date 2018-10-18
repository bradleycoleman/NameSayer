package controllers;

import data.Name;
import data.NameSayerModel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import main.Main;

import java.io.File;


public class NameDatabaseScreenController {
    @FXML private ListView<Name> _names;
    @FXML private ChoiceBox _fileChooser;


    private Main _main;
    private NameSayerModel _namesModel;
    private Name _currentName;

    public void initializeData(NameSayerModel namesModel, Main main) {
        _namesModel = namesModel;
        _main = main;
        _names.setItems(FXCollections.observableArrayList(namesModel.getDatabase()));
    }

    @FXML
    private void rateBad() {

    }

    @FXML
    private void rateGood() {

    }

    @FXML
    private void play() {

    }

    @FXML
    private void searchNames() {

    }

    @FXML
    private void returnToStart() {
        _main.setSceneToStart();
    }

}

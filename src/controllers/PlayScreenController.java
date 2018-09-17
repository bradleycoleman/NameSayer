package controllers;

import data.NameSayerModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import main.Main;
import data.Name;

import java.util.List;

/**
 * Edit this, along with the playScreen.fxml file, to make the play screen
 */
public class PlayScreenController {
    @FXML private Button _testButton;
    @FXML private Button _delete;
    @FXML private Label _nameNumber;

    NameSayerModel _nameSayerModel;
    Main _main;

    public void initializeData(NameSayerModel nameSayerModel, Main main){
        _nameSayerModel = nameSayerModel;
        _main = main;
    }

    /**
     * Button method for returning to the start screen.
     */
    @FXML
    private void returnToStartScreen(){
        _main.setSceneToStart();
    }


}

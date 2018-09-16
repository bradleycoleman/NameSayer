package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import main.Main;
import main.Name;

import java.util.List;

/**
 * Edit this, along with the playScreen.fxml file, to make the play screen
 */
public class PlayScreen {
    @FXML private Button _testButton;
    @FXML private Button _delete;
    @FXML private Label _nameNumber;

    private List<Name> _playlist;
    private int index = 1;

    @FXML
    private void returnToStartScreen(){
        Main.PRIMARY_STAGE.setScene(Main.START_SCENE);
    }

}

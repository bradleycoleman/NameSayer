package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import main.Main;

/**
 * Edit this, along with the playScreen.fxml file, to make the play screen
 */
public class PlayScreen {
    @FXML private Button _testButton;

    @FXML
    private void returnToStartScreen(){
        Main.SECONDARY_STAGE.hide();
        Main.PRIMARY_STAGE.show();
    }

}

package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import main.Main;


public class StartScreenController {
    @FXML private MenuItem _changePath, _badRecordings;
    @FXML private Button _browse, _practice;
    @FXML private ListView _defaultNames;
    @FXML private GridPane _grid;
    private Main _main;

    public void initializeData(Main main) {
        _main = main;
    }

    @FXML
    private void browse() {
        _main.setSceneToBrowse();
    }

    @FXML
    private void testMic() {
        _main.setSceneToTest();
    }
}

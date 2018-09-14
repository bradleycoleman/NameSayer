package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.StringConverter;


public class Controller {
    @FXML
    private Button _practice;
    @FXML
    private Button _testMic;
    @FXML
    private CheckBox _shuffle;
    @FXML
    private TitledPane _currentName;
    @FXML
    private TextArea _nameDetails;
    @FXML
    private ListView _playlist;
    @FXML
    private ListView _names;

    /**
     * Initializes the controller class.
     */
    @FXML
    private void initialize() {

        
    }
}

package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.util.StringConverter;


public class Controller {
    @FXML
    private Button practice;
    @FXML
    private Text currentNames;
    @FXML
    private ListView names;

    /**
     * Initializes the controller class.
     */
    @FXML
    private void initialize() {
   
    }
    
    @FXML
    private void practiceAction() {
    	System.out.println("end my life");
    }
}

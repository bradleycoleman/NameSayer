package controllers;

import data.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import main.Main;

import java.io.*;
import java.util.*;


public class CurateScreenController {
    @FXML private Button _shuffle, _add, _sortButton, _viewDatabase;
    @FXML private TextField _fullNameText;
    @FXML private Label _name;
    @FXML private TitledPane _playlistBox, _nameOptions;
    @FXML private ChoiceBox _fileChooser;
    @FXML private ListView<FullName> _playlistView;
    @FXML private ListView<Name> _subnames;

    private Main _main;
    private ObservableList _fullNameList;
    private FullName _currentFullName;
    private Name _currentSubname;
    private Playlist _playlist;
    private NameSayerModel _nameSayerModel = null;
    private FileChooser _fileDialog = new FileChooser();


    /**
     * Initializes the controller class.
     */
    public void initializeData(NameSayerModel nameSayerModel, Main main){
        _main = main;
        _nameSayerModel = nameSayerModel;
        // selected a name from the playlist will make the name options appear
        _playlistView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<FullName>() {
            @Override
            public void onChanged(Change<? extends FullName> c) {
                if (c.next() && c.wasAdded()) {
                    setName(_playlistView.getSelectionModel().getSelectedItem());
                }
            }
        });
        // selecting a subname will change the options displayed
        _subnames.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<Name>() {
            @Override
            public void onChanged(Change<? extends Name> c) {
                if (c.next() && c.wasAdded()) {
                    _currentSubname = _subnames.getSelectionModel().getSelectedItem();
                    _fileChooser.setItems(FXCollections.observableArrayList(_currentSubname.getFiles()));
                    int i = _currentFullName.getSubNames().indexOf(_currentSubname);
                    // setting the file as the last selected one.
                    _fileChooser.getSelectionModel().select(_currentFullName.getAudioFiles().get(i));
                }
            }
        });
        // Making a File chooser selection change the preffered file for the current subname
        _fileChooser.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                _currentFullName.setFileAtIndex((File) newValue, _currentFullName.getSubNames().indexOf(_currentSubname));
            }
        });
        // Setting file chooser to display ratings of files from subname files
        _fileChooser.setConverter(new StringConverter() {
            @Override
            public String toString(Object object) {
                File file = (File) object;
                if (_currentSubname.getRating(file) == 2) {
                    return "✓ " + ((File) object).getName();
                } else if (_currentSubname.getRating(file) == 1) {
                    return "✕ " + ((File) object).getName();
                } else {
                    return "(unrated) " + ((File) object).getName();
                }
            }
            @Override
            public Object fromString(String string) {
                return null;
            }
        });
    }

    public void editPlaylist(Playlist playlist) {
        // Options are initially invisible as no name is selected
        _nameOptions.setVisible(false);
        _playlist = playlist;
        refreshName();
        _fullNameList = FXCollections.observableArrayList(_playlist.getFullNames());
        _playlistView.setItems(_fullNameList);
    }

    public void newPlaylist(String name) {
        // Options are initially invisible as no name is selected
        _nameOptions.setVisible(false);
        _playlist = new Playlist(name);
        refreshName();
        _fullNameList = FXCollections.observableArrayList();
        _playlistView.setItems(_fullNameList);
    }

    private void setName(FullName name) {
        _currentFullName = name;
        _nameOptions.setVisible(true);
        _subnames.setItems(FXCollections.observableArrayList(name.getSubNames()));
        _subnames.getSelectionModel().selectFirst();
    }

    @FXML
    private void addName() {
        List<Name> names = new ArrayList<>();
        String fullNameText = _fullNameText.getText();
        _fullNameText.clear();
        FullName fullName = addNameFromString(fullNameText);
        // if names is empty then the name will not be added to the playlist, user is warned
        if (fullName == null) {
            Alert noNameAlert = new Alert(Alert.AlertType.ERROR,"None of the inputted names are in the database,\nplease try again",ButtonType.OK);
            noNameAlert.setHeaderText("Cannot Add Name");
            noNameAlert.showAndWait();
            return;
        }
        for (Name subName: fullName.getSubNames()) {
            fullNameText = fullNameText.replaceAll(subName.toString(),"\n");
        }
        // If any of the names were left unfound, then alert the user before asking if they still want to add the name
        if (!fullName.toString().equals(fullNameText)) {
            StringBuilder sb = new StringBuilder();
            sb.append("The following names are not in the database:\n");
            sb.append(fullNameText);
            sb.append("The full name will be added as: ");
            sb.append(fullName);
            Alert noNameAlert = new Alert(Alert.AlertType.CONFIRMATION, sb.toString(), ButtonType.YES, ButtonType.NO);
            noNameAlert.setTitle("Name Incomplete");
            noNameAlert.setHeaderText("Do you still want to add this incomplete name?");
            Optional<ButtonType> result = noNameAlert.showAndWait();
            if (result.get() == ButtonType.NO){
                return;
            }
        }
        _fullNameList.add(fullName);
    }

    private FullName addNameFromString(String nameText) {
        List<Name> names = new ArrayList<>();
        String[] splitLine = nameText.split("[ -]");
        List<String> unfoundNames = new ArrayList<>(Arrays.asList(splitLine));
        for (String word: splitLine) {
            for (Name name : _nameSayerModel.getDatabase()) {
                if (word.toLowerCase().equals(name.toString().toLowerCase())) {
                    // once a matching name is found for the word, it is added to the list of names and the word is
                    // removed from the unfounded words
                    names.add(name);
                    unfoundNames.remove(word);
                    break;
                }
            }
        }
        // removing unfound names from full name
        for (String unfound : unfoundNames) {
            nameText = nameText.replace(unfound, "");
        }
        nameText = nameText.replaceAll("[ -]*2"," ");
        if (!names.isEmpty()) {
            return new FullName(nameText,names);
        }
        return null;
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
        _currentSubname.updateRatingOfFile((File) _fileChooser.getValue(),rating);
        _fileChooser.getItems().clear();
        _fileChooser.setItems(FXCollections.observableArrayList(_currentSubname.getFiles()));
        _fileChooser.getSelectionModel().select(selected);
    }

    @FXML
    private void play() {
        AudioUtils au = new AudioUtils();
        if(_currentFullName == null){
            return;
        }
        au.playFile((File)_fileChooser.getValue());
    }

    @FXML
    private void shuffle() {
        Collections.shuffle(_playlistView.getItems());
    }
    @FXML
    private void sort() {
        Collections.sort(_playlistView.getItems());
    }

    @FXML
    private void remove() {
        _fullNameList.remove(_currentFullName);
    }

    @FXML
    private void rename() {
        TextInputDialog newName = new TextInputDialog(_playlist.toString());

        newName.setTitle("Rename Playlist");
        newName.setHeaderText("Enter a new name");
        newName.setContentText("Name:");

        Optional<String> result = newName.showAndWait();

        result.ifPresent(name -> {
            _playlist.rename(name);
        });
        refreshName();
    }

    private void refreshName() {
        _playlistBox.setText(_playlist.toString());
        _name.setText(_playlist.toString());
    }

    @FXML
    private void fileAdd() {
        File file = _fileDialog.showOpenDialog(_main.getStage());
        _fileDialog.setTitle("Find a playlist .txt file");
        _fileDialog.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        _fileDialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("TEXT", "*.txt")
        );
        if (file != null) {
            BufferedReader reader;
            List<String> notFound = new ArrayList<>();
            try {
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    FullName newName = addNameFromString(line);
                    if (newName == null || !line.equals(newName.toString())) {
                        notFound.add(line);
                    }
                    if (newName != null) {
                        _fullNameList.add(newName);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!notFound.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String line: notFound) {
                    sb.append(line + "\n");
                }
                Alert unfoundAlert = new Alert(Alert.AlertType.CONFIRMATION,sb.toString(), ButtonType.OK);
                unfoundAlert.setHeaderText("The following names included words not in the database:");
                unfoundAlert.showAndWait();
            }
        }
    }

    @FXML
    private void exit() {
        _playlist.setNames(_fullNameList);
        _playlist.updateFile();

        _nameSayerModel.writeGoodBadNames();
        _main.setSceneToStart();

        // Check if playlist already exists on the model
        for(Playlist p: _nameSayerModel.getPlaylists()){
            if(p == _playlist){
                return;
            }
        }
        _nameSayerModel.getPlaylists().add(_playlist);

    }

}

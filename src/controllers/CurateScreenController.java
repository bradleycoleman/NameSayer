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
    @FXML private ChoiceBox<File> _fileChooser;
    @FXML private ListView<FullName> _playlistView;
    @FXML private ListView<Name> _subnames;


    private Main _main;
    private FullName _currentFullName;
    private Name _currentSubname;
    private Playlist _playlist;
    private NameSayerModel _nameSayerModel = null;
    private FileChooser _fileDialog = new FileChooser();
    private AudioUtils _au;


    /**
     * Initializes the controller class.
     */
    public void initializeData(NameSayerModel nameSayerModel, Main main){
        _main = main;
        _nameSayerModel = nameSayerModel;
        // selected a name from the playlist will make the name options appear, deleting the name will make the options
        // change to another name, or disapper
        _playlistView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<FullName>() {
            @Override
            public void onChanged(Change<? extends FullName> c) {
                if (c.next() && c.wasAdded()) {
                    setName(_playlistView.getSelectionModel().getSelectedItem());
                } else if (c.wasRemoved()) {
                    if (_playlistView.getItems().isEmpty()) {
                        // this method removes the options if the input is null
                        setName(null);
                    } else {
                        // Selected an arbitrary other name
                        _playlistView.getSelectionModel().selectFirst();
                    }
                }
            }
        });
        // selecting a subname will change the options displayed
        _subnames.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<Name>() {
            @Override
            public void onChanged(Change<? extends Name> c) {
                if (c.next() ) {
                    _currentSubname = _subnames.getSelectionModel().getSelectedItem();
                    if (_currentSubname != null) {
                        _fileChooser.setItems(FXCollections.observableArrayList(_currentSubname.getFiles()));
                        int i = _subnames.getSelectionModel().getSelectedIndex();
                        // setting the file as the last selected one.
                        _fileChooser.getSelectionModel().select(_currentFullName.getAudioFiles().get(i));
                    }
                }
            }
        });
        // Making a File chooser selection change the preffered file for the current subname
        _fileChooser.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue observable, File oldFile, File newFile) {
                _currentFullName.setFileAtIndex(newFile, _subnames.getSelectionModel().getSelectedIndex());
            }
        });
        // Setting file chooser to display ratings of files from subname files
        _fileChooser.setConverter(new StringConverter<File>() {
            @Override
            public String toString(File file) {
                if (_currentSubname.getRating(file) == 2) {
                    return "✓ " + file.getName();
                } else if (_currentSubname.getRating(file) == 1) {
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
    }

    /**
     * Method called to initialize this screen with all the details of the entered playlist so that it can be edited
     * @param playlist The Playlist to be edited
     */
    public void editPlaylist(Playlist playlist) {
        // Options are initially invisible as no name is selected
        _nameOptions.setVisible(false);
        _playlist = playlist;
        refreshName();
        // setting the playlistview to show the full names from the playlist
        _playlistView.getItems().setAll(_playlist.getFullNames());
    }

    /**
     * Method called to initialize this screen with a new playlist
     * @param name the name this playlist is to be called
     */
    public void newPlaylist(String name) {
        // Options are initially invisible as no name is selected
        _nameOptions.setVisible(false);
        _playlist = new Playlist(name);
        _playlistView.getItems().clear();
        refreshName();
    }

    /**
     * Controls whether the Name Options tab is shown to the user, and set what details to be shown
     * @param name The FullName to be displayed
     */
    private void setName(FullName name) {
        _currentFullName = name;
        if (name != null) {
            _nameOptions.setVisible(true);
            _subnames.setItems(FXCollections.observableArrayList(name.getSubNames()));
            _subnames.getSelectionModel().selectFirst();
        } else {
            _nameOptions.setVisible(false);
        }
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
        // If any of the names were left unfound, then alert the user before asking if they still want to add the name
        if (!fullName.toString().replaceAll("[ -]","").equals(fullNameText.replaceAll("[ -]",""))) {
            StringBuilder sb = new StringBuilder();
            sb.append("The following names from");
            sb.append(fullNameText);
            sb.append(" are not in the database:\n");
            // removing all found subnames from the original fullnametext so the user knows what wasn't added
            for (Name subName: fullName.getSubNames()) {
                fullNameText = fullNameText.replaceAll("(?i)(" + subName.toString() + "[ -]*)","");
            }
            // adding line breaks so that the missing names are displayed as a list.
            fullNameText = fullNameText.replaceAll("[ -]+","\n");
            sb.append(fullNameText);
            sb.append("\n");
            sb.append("The full name will be added as: ");
            sb.append(fullName);
            Alert noNameAlert = new Alert(Alert.AlertType.CONFIRMATION, sb.toString(), ButtonType.YES, ButtonType.NO);
            noNameAlert.setTitle("Name Incomplete");
            noNameAlert.setHeaderText("Do you still want to add this incomplete name?");
            Optional<ButtonType> result = noNameAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.NO){
                return;
            }
        }
        _playlistView.getItems().add(fullName);
    }

    /**
     * Method to add a name to this playlist based on an inputted string. The name added will keep the formatting of
     * the string where possible to preserve space characters used.
     * @param nameText A string where subnames are seperated by ' ' or '-' characters
     * @return a FullName object, or null if the name cannot be made
     */
    private FullName addNameFromString(String nameText) {
        List<Name> names = new ArrayList<>();
        // subnames are split by one or more spaces or dashes
        String[] splitLine = nameText.split("[ -]+");
        // all subnames are marked as unfound initially
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
        // if two or more spaces exist in a row, replace with 1 space
        nameText = nameText.replaceAll("[ -]{2,}"," ");
        if (!names.isEmpty()) {
            return new FullName(nameText,names);
        }
        // if no names were found, return null
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
     * updates rating for name, then refreshes choices by removing and replacing from the choicebox.
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
        _au = new AudioUtils();
        if(_currentFullName == null){
            return;
        }
        _au.playFile((File)_fileChooser.getValue());
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
        _playlistView.getItems().remove(_currentFullName);
    }

    @FXML
    private void rename() {
        TextInputDialog newName = new TextInputDialog(_playlist.toString());

        newName.setTitle("Rename Playlist");
        newName.setHeaderText("Enter a new name");
        newName.setContentText("Name:");

        Optional<String> result = newName.showAndWait();

        result.ifPresent(name -> {

            // checking if the name is unique
            for (Playlist playlist : _nameSayerModel.getPlaylists()) {
                if (playlist.toString().equals(name)) {
                    Alert duplicateName = new Alert(Alert.AlertType.ERROR, "This name is already in use by " +
                            "another playlist", ButtonType.OK);
                    duplicateName.setHeaderText("Invalid Playlist Name");
                    duplicateName.setTitle("Could not rename Playlist");
                    duplicateName.showAndWait();
                    return;
                }
            }
            // checking if name is of the correct length and uses only the allowed characters
            if (name.matches("[A-Za-z0-9 ,-]{1,15}")) {
                // if it is ok, then rename the playlist
                _playlist.rename(name);
            } else {
                Alert invalidChar = new Alert(Alert.AlertType.ERROR, "Names must be between 1 and 15 \nword " +
                        "characters long");
                invalidChar.setHeaderText("Invalid Playlist Name");
                invalidChar.setTitle("Could not rename Playlist");
                invalidChar.showAndWait();
            }
        });
        refreshName();
    }

    /**
     * Sets the titled pane, and the label in the top left corner to have the correct name from the playlist object
     */
    private void refreshName() {
        _playlistBox.setText(_playlist.toString());
        _name.setText(_playlist.toString());
    }

    @FXML
    private void fileAdd() {
        // Showing a file chooser for the user to pick a .txt file
        _fileDialog.setTitle("Find a playlist .txt file");
        _fileDialog.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        _fileDialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("TEXT", "*.txt")
        );
        File file = _fileDialog.showOpenDialog(_main.getStage());
        int namesAdded = 0;
        // if a non-null file is chosen, the names will be added
        if (file != null) {
            BufferedReader reader;
            List<String> notFound = new ArrayList<>();
            try {
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    // for each line, attempt to add a new full name
                    FullName newName = addNameFromString(line);
                    // if the add fails, or the name cannot add completely, mark the name as notFound
                    if (newName == null || !line.equals(newName.toString())) {
                        notFound.add(line);
                    }
                    if (newName != null) {
                        namesAdded++;
                        _playlistView.getItems().add(newName);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // if any of the names weren't found, then tell the user
            if (!notFound.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                // each unfound line is listed
                for (String line: notFound) {
                    sb.append(line);
                    sb.append("\n");
                }
                sb.append("The parts of these names that the database does not include were not added");
                Alert unfoundAlert = new Alert(Alert.AlertType.WARNING,sb.toString(), ButtonType.OK);
                unfoundAlert.setHeaderText("The following names included words not in the database:");
                unfoundAlert.setTitle("Some names could not be added");
                unfoundAlert.showAndWait();
            }
            // tell the user the results of the fileAdd.
            Alert confirmAdd = new Alert(Alert.AlertType.CONFIRMATION, "Successfully added " + namesAdded +
                    " full names to the playlist", ButtonType.OK);
            confirmAdd.setHeaderText(null);
            confirmAdd.setTitle("Results of File Add");
            confirmAdd.showAndWait();
        }
    }

    @FXML
    private void exit() {
        _playlist.setNames(_playlistView.getItems());
        _playlist.updateFile();

        _nameSayerModel.writeGoodBadNames();
        _main.setSceneToStart();
        if (_au != null && _au.getClip() != null) {
            try {
                _au.getClip().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Check if playlist already exists on the model
        for(Playlist p: _nameSayerModel.getPlaylists()){
            if(p == _playlist){
                return;
            }
        }
        _nameSayerModel.getPlaylists().add(_playlist);

    }

}

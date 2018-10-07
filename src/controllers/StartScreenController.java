package controllers;

import data.FullName;
import data.Name;
import data.NameSayerModel;
import data.Playlist;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import main.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class StartScreenController {
    @FXML private Button _browse, _practice;
    @FXML private ListView _defaultNames;
    @FXML private GridPane _grid;
    private Main _main;
    private NameSayerModel _nameSayerModel;
    private Playlist _randomizedPlaylist;

    public void initializeData(NameSayerModel nameSayerModel, Main main) {
        _main = main;
        _nameSayerModel = nameSayerModel;

        Random rnd = new Random();

        _randomizedPlaylist = new Playlist("Randomized Playlist");
        // Generate 5 random full names
        for(int i = 0; i < 5; i++){
            List<Name> subnames = new ArrayList<Name>();
            subnames.add(_nameSayerModel.getDatabase().get(rnd.nextInt(_nameSayerModel.getDatabase().size())));
            subnames.add(_nameSayerModel.getDatabase().get(rnd.nextInt(_nameSayerModel.getDatabase().size())));

            FullName fullName = new FullName(subnames.get(0).toString()+" "+subnames.get(1).toString(), subnames);
            _randomizedPlaylist.getFullNames().add(fullName);
        }

        _defaultNames.setItems(FXCollections.observableArrayList(_randomizedPlaylist.getFullNames()));

    }

    @FXML
    private void changePath(){

    }

    @FXML
    private void badRecordings(){

    }

    @FXML
    private void practice(){
        _main.setSceneToPlay(_randomizedPlaylist);
    }

    @FXML
    private void browse() {
        _main.setSceneToBrowse();
    }

}

package main;

import controllers.PlayScreenController;
import controllers.StartScreenController;
import controllers.TestScreenController;
import data.NameSayerModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    private Stage _window;
    private Scene startScene, playScene, testScene;
    private StartScreenController _startScreenController;
    private PlayScreenController _playScreenController;
    private TestScreenController _testScreenController;
    private NameSayerModel _nameSayerModel;

    @Override
    public void start(Stage window) throws Exception{
        _window = window;
        // Load all the scenes
        FXMLLoader startPaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/startScreen.fxml"));
        Parent startPane = startPaneLoader.load();
        startScene = new Scene(startPane, 600, 400);

        FXMLLoader playPaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/playScreen.fxml"));
        Parent playPane = playPaneLoader.load();
        playScene = new Scene(playPane, 600, 400);

        FXMLLoader testPaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/testScreen.fxml"));
        Parent testPane = testPaneLoader.load();
        testScene = new Scene(testPane, 600, 400);

        window.setTitle("Name Sayer");

        // Inject the data model into PlayScreen and StartScreen
        _nameSayerModel = new NameSayerModel();

        _startScreenController = startPaneLoader.getController();
        _playScreenController = playPaneLoader.getController();
        _testScreenController = testPaneLoader.getController();

        _startScreenController.initializeData(_nameSayerModel, this);
        _playScreenController.initializeData(_nameSayerModel, this);

        // The first scene will be the playlist editing scene
        window.setScene(startScene);
        window.setResizable(false);
        window.show();
    }

    public Stage getStage(){
        return _window;
    }

    public void setSceneToStart(){
        _window.setScene(startScene);
    }

    public void setSceneToPlay(){
        _window.setScene(playScene);
        _playScreenController.startPractice();
    }

    public void setSceneToTest() {
        _window.setScene(testScene);
        _testScreenController.startTest(this);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

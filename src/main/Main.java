package main;

import controllers.PlayScreenController;
import controllers.CurateScreenController;
import controllers.StartScreenController;
import controllers.TestScreenController;
import data.NameSayerModel;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    private Stage _window;
    private Scene curateScene, playScene, testScene, startScene;
    private StartScreenController _startScreenController;
    private CurateScreenController _curateScreenController;
    private PlayScreenController _playScreenController;
    private TestScreenController _testScreenController;
    private NameSayerModel _nameSayerModel;

    @Override
    public void start(Stage window) throws Exception{
        _window = window;
        // Load all the scenes
        FXMLLoader startPaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/startScreen.fxml"));
        Parent startPane = startPaneLoader.load();
        startScene = new Scene(startPane, 800, 450);

        FXMLLoader curatePaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/curateScreen.fxml"));
        Parent curatePane = curatePaneLoader.load();
        curateScene = new Scene(curatePane, 600, 400);

        FXMLLoader playPaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/playScreen.fxml"));
        Parent playPane = playPaneLoader.load();
        playScene = new Scene(playPane, 600, 400);

        FXMLLoader testPaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/testScreen.fxml"));
        Parent testPane = testPaneLoader.load();
        testScene = new Scene(testPane, 600, 400);

        window.setTitle("Name Sayer");

        // Set the closing operation of the stage
        window.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Stage is closing");
                System.exit(0);
            }
        });


        // Inject the data model into PlayScreen and StartScreen
        _nameSayerModel = new NameSayerModel();

        _startScreenController = startPaneLoader.getController();
        _curateScreenController = curatePaneLoader.getController();
        _playScreenController = playPaneLoader.getController();
        _testScreenController = testPaneLoader.getController();

        _curateScreenController.initializeData(_nameSayerModel, this);
        _playScreenController.initializeData(_nameSayerModel, this);

        // The first scene will be the playlist editing scene
        window.setScene(startScene);
        window.setResizable(true);

        window.show();
    }

    public Stage getStage(){
        return _window;
    }

    public void setSceneToStart(){
        _window.setScene(curateScene);
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

package main;

import controllers.*;
import data.NameSayerModel;
import data.Playlist;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    private Stage _window;
    private Scene curateScene, playScene, testScene, startScene, browseScene;
    private StartScreenController _startScreenController;
    private BrowseScreenController _browseScreenController;
    private CurateScreenController _curateScreenController;
    private PlayScreenController _playScreenController;
    private TestScreenController _testScreenController;
    private NameSayerModel _nameSayerModel;

    @Override
    public void start(Stage window) throws Exception{
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        _window = window;
        // Load all the scenes
        FXMLLoader startPaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/startScreen.fxml"));
        Parent startPane = startPaneLoader.load();
        startScene = new Scene(startPane, screenSize.getWidth()-75, screenSize.getHeight() -75);

        FXMLLoader browsePaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/browseScreen.fxml"));
        Parent browsePane = browsePaneLoader.load();
        browseScene = new Scene(browsePane,screenSize.getWidth()-75, screenSize.getHeight()-75);

        FXMLLoader curatePaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/curateScreen.fxml"));
        Parent curatePane = curatePaneLoader.load();
        curateScene = new Scene(curatePane, screenSize.getWidth()-75, screenSize.getHeight()-75);

        FXMLLoader playPaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/playScreen.fxml"));
        Parent playPane = playPaneLoader.load();
        playScene = new Scene(playPane, screenSize.getWidth()-75, screenSize.getHeight()-75);

        FXMLLoader testPaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/testScreen.fxml"));
        Parent testPane = testPaneLoader.load();
        testScene = new Scene(testPane, screenSize.getWidth()-75, screenSize.getHeight()-75);

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
        _browseScreenController = browsePaneLoader.getController();
        _browseScreenController.initializeData(_nameSayerModel, this);
        _startScreenController.initializeData(_nameSayerModel, this);
        _curateScreenController.initializeData(_nameSayerModel, this);
        _playScreenController.initializeData(_nameSayerModel, this);

        // The first scene will be the playlist editing scene
        window.setScene(startScene);
        window.setResizable(true);
        window.setMaximized(true);
        window.show();
    }

    public Stage getStage(){
        return _window;
    }

    public void setSceneToCurateNew(String playlistName){
        _curateScreenController.newPlaylist(playlistName);
        _window.setScene(curateScene);
    }

    public void setSceneToCurateEdit(Playlist playlist) {
        _curateScreenController.editPlaylist(playlist);
        _window.setScene(curateScene);
    }

    public void setSceneToStart() {_window.setScene(startScene);}

    public void setSceneToBrowse() {
        _browseScreenController.update();
        _window.setScene(browseScene);
    }

    public void setSceneToPlay(Playlist playlist){
        _window.setScene(playScene);
        _playScreenController.startPractice(playlist);
    }

    public void setSceneToTest() {
        _window.setScene(testScene);
        _testScreenController.startTest(this);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

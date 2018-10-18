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
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class Main extends Application {

    private Stage _window;
    private Scene curateScene, playScene, testScene, startScene, browseScene, nameDatabaseScene;
    private StartScreenController _startScreenController;
    private BrowseScreenController _browseScreenController;
    private NameDatabaseScreenController _nameDatabaseScreenController;
    private CurateScreenController _curateScreenController;
    private PlayScreenController _playScreenController;
    private NameSayerModel _nameSayerModel;

    @Override
    public void start(Stage window) throws Exception{
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        _window = window;
        // Load all the scenes
        FXMLLoader startPaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/startScreen.fxml"));
        Parent startPane = startPaneLoader.load();
        startScene = new Scene(startPane, 800, 450);

        FXMLLoader browsePaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/browseScreen.fxml"));
        Parent browsePane = browsePaneLoader.load();
        browseScene = new Scene(browsePane,800, 450);

        FXMLLoader nameDatabasePaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/nameDatabaseScreen.fxml"));
        Parent nameDatabasePane = nameDatabasePaneLoader.load();
        nameDatabaseScene = new Scene(nameDatabasePane,800, 450);

        FXMLLoader curatePaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/curateScreen.fxml"));
        Parent curatePane = curatePaneLoader.load();
        curateScene = new Scene(curatePane, 800, 450);

        FXMLLoader playPaneLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlFiles/playScreen.fxml"));
        Parent playPane = playPaneLoader.load();
        playScene = new Scene(playPane, 800, 500);

        window.setTitle("Name Sayer");

        // Set the closing operation of the stage
        window.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.exit(0);
            }
        });


        // Inject the data model into PlayScreen and StartScreen
        _nameSayerModel = new NameSayerModel();

        _startScreenController = startPaneLoader.getController();
        _curateScreenController = curatePaneLoader.getController();
        _playScreenController = playPaneLoader.getController();
        _browseScreenController = browsePaneLoader.getController();
        _nameDatabaseScreenController = nameDatabasePaneLoader.getController();
        _browseScreenController.initializeData(_nameSayerModel, this);
        _startScreenController.initializeData(_nameSayerModel, this);
        _curateScreenController.initializeData(_nameSayerModel, this);
        _playScreenController.initializeData(_nameSayerModel, this);
        _nameDatabaseScreenController.initializeData(_nameSayerModel, this);

        // The first scene will be the playlist editing scene
        window.setScene(startScene);
        window.setResizable(true);
        window.setMinHeight(450);
        window.setMinWidth(800);
        window.show();
    }

    public Stage getStage(){
        return _window;
    }

    public void setSceneToCurateNew(String playlistName){
        _curateScreenController.newPlaylist(playlistName);
        changeAndResize(curateScene);
    }

    public void setSceneToCurateEdit(Playlist playlist) {
        _curateScreenController.editPlaylist(playlist);
        changeAndResize(curateScene);
    }

    public void setSceneToStart() {changeAndResize(startScene);}

    public void setSceneToBrowse() {
        _browseScreenController.update();
        changeAndResize(browseScene);
    }

    public void setSceneToNameDatabase() {
        changeAndResize(nameDatabaseScene);
    }

    public void setSceneToPlay(Playlist playlist){
        changeAndResize(playScene);
        _playScreenController.startPractice(playlist);
    }

    /**
     * This method will change the displayed scene while keeping the window size the same as it was before, and in the
     * same position it was before.
     * @param changeTo the Scene to be changed to.
     */
    private void changeAndResize(Scene changeTo) {
        double h = _window.getHeight();
        double w = _window.getWidth();
        double x = _window.getX();
        double y = _window.getY();
        _window.setScene(changeTo);
        _window.setHeight(h);
        _window.setWidth(w);
        _window.setX(x);
        _window.setY(y);
    }

    public void setSceneToTest() {
        changeAndResize(testScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public BrowseScreenController getBrowseScreenController() {
    	return _browseScreenController;
    }
}

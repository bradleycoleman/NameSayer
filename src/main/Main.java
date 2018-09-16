package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    public static Stage PRIMARY_STAGE;
    public static Scene START_SCENE;
    public static Parent START_SCREEN;
    public static Parent PLAY_SCREEN;
    private static List<Name> _names;

    @Override
    public void start(Stage primaryStage) throws Exception{

        PRIMARY_STAGE = primaryStage;

        START_SCREEN = FXMLLoader.load(getClass().getClassLoader().getResource("fxmlFiles/startScreen.fxml"));
        PLAY_SCREEN = FXMLLoader.load(getClass().getClassLoader().getResource("fxmlFiles/playScreen.fxml"));

        PRIMARY_STAGE.setTitle("Name Sayer");
        START_SCENE = new Scene(START_SCREEN, 600,400);
        PRIMARY_STAGE.setScene(START_SCENE);
        PRIMARY_STAGE.setResizable(false);
        PRIMARY_STAGE.show();
    }

    public static void startPractice(List<Name> names) {
        _names = names;
        PRIMARY_STAGE.setScene(new Scene(PLAY_SCREEN,600,400));
    }

    public static List<Name> getNames() { return _names; }

    public static void main(String[] args) {
        launch(args);
    }
}

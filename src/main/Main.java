package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage PRIMARY_STAGE;
    public static Stage SECONDARY_STAGE;
    public static Parent START_SCREEN;
    public static Parent PLAY_SCREEN;

    @Override
    public void start(Stage primaryStage) throws Exception{

        PRIMARY_STAGE = primaryStage;
        SECONDARY_STAGE = new Stage();

        START_SCREEN = FXMLLoader.load(getClass().getClassLoader().getResource("fxmlFiles/startScreen.fxml"));
        PLAY_SCREEN = FXMLLoader.load(getClass().getClassLoader().getResource("fxmlFiles/playScreen.fxml"));

        PRIMARY_STAGE.setTitle("Name Sayer");
        PRIMARY_STAGE.setScene(new Scene(START_SCREEN, 540,300));
        PRIMARY_STAGE.setResizable(false);
        PRIMARY_STAGE.show();

        SECONDARY_STAGE.setTitle("Practice makes perfect!");
        SECONDARY_STAGE.setScene(new Scene(PLAY_SCREEN,540,300));
        SECONDARY_STAGE.setResizable(false);
    }


    public static void main(String[] args) {
        launch(args);
    }
}

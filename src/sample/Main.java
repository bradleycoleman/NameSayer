package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent startingScreen = FXMLLoader.load(getClass().getResource("startScreen.fxml"));
        primaryStage.setTitle("Name Sayer");
        primaryStage.setScene(new Scene(startingScreen, 540,300));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

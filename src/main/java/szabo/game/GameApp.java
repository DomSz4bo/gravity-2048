package szabo.game;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class GameApp extends Application {


    @Override
    public void start(Stage primaryStage) {

        ViewManager viewManager = new ViewManager();
        Scene scene = new Scene(viewManager, 1000, 800);
        primaryStage.setScene(scene);

        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(500);

        primaryStage.setTitle("2048");
        primaryStage.getIcons().add(new Image("file:images/icon2048.png"));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

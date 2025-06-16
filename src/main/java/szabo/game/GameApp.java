package szabo.game;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class GameApp extends Application {
    private AppManager manager;

    @Override
    public void start(Stage primaryStage) {

        manager = new AppManager();
        Scene scene = new Scene(manager, 1000, 800);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());

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

    @Override
    public void stop() throws Exception {
        super.stop();
        System.out.println("Application stopped");
        manager.saveGame();
        manager.getLeaderboard().save();
    }
}

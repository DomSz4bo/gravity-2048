package szabo.game;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class GameApp extends Application {
    public static final int MIN_WIDTH = 400;
    public static final int MIN_HEIGHT = 400;
    private AppManager manager;

    @Override
    public void start(Stage primaryStage) {
        manager = new AppManager();
        Scene scene = new Scene(manager, 1000, 800);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
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
        manager.saveGame();
        manager.saveLeaderboard();
    }
}

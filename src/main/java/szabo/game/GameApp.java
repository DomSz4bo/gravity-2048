package szabo.game;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Main class of the game and entry point for the JavaFX application.
 * Initializes and configures the primary stage and handles application exit.
 */
public class GameApp extends Application {
    /**
     * The minimum width (in pixels) of the main application window.
     */
    public static final int MIN_WIDTH = 400;
    /**
     * The minimum height (in pixels) of the main application window.
     */
    public static final int MIN_HEIGHT = 400;

    private AppManager manager;

    /**
     * Initializes and displays the main window of the application.
     * <p>
     * Sets up the scene with an instance of {@link szabo.game.AppManager} as the root,
     * applies styling, configures window properties, and finally displays the window.
     * </p>
     *
     * @param primaryStage the main stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        manager = new AppManager();
        Scene scene = new Scene(manager, 1000, 800);
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("style.css")
        ).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setTitle("2048");
        primaryStage.getIcons().add(new Image("file:images/icon.png"));
        primaryStage.show();
    }


    /**
     * Called before application exit, saves game data to preserve user progress.
     */
    @Override
    public void stop() {
        manager.saveData();
    }

    /**
     * Launches the application.
     *
     * @param args the arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }

}

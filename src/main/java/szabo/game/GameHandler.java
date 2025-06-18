package szabo.game;

import javafx.animation.AnimationTimer;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;


/**
 * This class handles everything tied to the game itself, that is, game logic and game visualization.
 * <p>
 * It manages the interaction between player input in the game GUI and game's logic
 * and also the application manager. It also handles the saving and loading of game data.
 * </p>
 */
public class GameHandler {
    /**
     * Defines the ratio between the playground's width and height.
     */
    public static final double PLAYGROUND_RATIO = 0.7;
    /**
     * Defines the thickness of the playground's borders as a percentage of the corresponding dimension.
     */
    public static final double WALL_THICKNESS = 0.01;
    /**
     * Defines the size of blocks as a percentage of the playground's width.
     */
    public static final double BLOCK_SIZE = 0.23;
    /**
     * Defines the position of the 'game over' line as a percentage of the playground's height.
     */
    public static final double LINE_POSITION = 0.3;

    private static final String GAME_SAVE = "saved_game.dat";
    private static final int NANOS_IN_SECOND = 1_000_000_000;

    private final AppManager manager;
    private final GamePane gamePane;
    private final GameLogic gameLogic;
    private final AnimationTimer gameLoop;
    private final ReadOnlyBooleanWrapper existingGameProperty;
    private GameState gameState = null;

    /**
     * Initializes the games logic and GUI. Sets up the interactions
     * between the GUI, logic and application manager.
     *
     * @param appManager the manager to communicate with
     */
    public GameHandler(AppManager appManager) {
        manager = appManager;
        gamePane = new GamePane(this::returnToMenu);
        gameLogic = new GameLogic(this::gameOver);
        existingGameProperty = new ReadOnlyBooleanWrapper();
        updateExistingGameProperty();

        setupMouseControl();
        setupKeyboardControl();

        gameLogic.addMergeListener(
                (newValue, posX, posY) ->
                        gamePane.getPlayground().runEffect(newValue, posX, (1 - posY))
        );

        gameLoop = new AnimationTimer() {
            private long lastUpdate;

            /**
             * Starts the game loop.
             */
            @Override
            public void start() {
                super.start();
                lastUpdate = System.nanoTime();
            }

            /**
             * Updates the games logic and visualizes it in the GUI.
             *
             * @param now The timestamp of the current frame given in nanoseconds.
             */
            @Override
            public void handle(long now) {
                long deltaNanos = now - lastUpdate;
                double deltaSeconds = (double) deltaNanos / NANOS_IN_SECOND;
                gameLogic.update(deltaSeconds);
                gameState = gameLogic.createGameState();
                gamePane.paint(gameState, appManager.getLeaderboard());
                lastUpdate = now;
            }
        };
    }

    private void setupMouseControl() {
        gamePane.getPlayground().getPlaygroundPane().setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                double posX = event.getX() / gamePane.getPlayground().getPlaygroundPane().getWidth();
                gameLogic.handleMouseB1Dragged(posX);
            }
        });
        gamePane.getPlayground().getPlaygroundPane().setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                double posX = event.getX() / gamePane.getPlayground().getPlaygroundPane().getWidth();
                double posY = event.getY() / gamePane.getPlayground().getPlaygroundPane().getHeight();
                gameLogic.handleMouseB1Pressed(posX, (1 - posY));
            }
        });
        gamePane.getPlayground().getPlaygroundPane().setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                gameLogic.handleMouseB1Released();
            }
        });
    }

    private void setupKeyboardControl() {
        gamePane.getPlayground().getPlaygroundPane().setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT -> {
                    gameLogic.moveBlock(false, 0.01);
                    event.consume();
                }
                case RIGHT -> {
                    gameLogic.moveBlock(true, 0.01);
                    event.consume();
                }
                case SPACE, DOWN -> {
                    gameLogic.keyboardReleaseBlock();
                    event.consume();
                }
                case UP -> event.consume();
                case ESCAPE, BACK_SPACE -> returnToMenu();
            }
        });
    }

    /**
     * Starts the game by starting the game loop that periodically updates the game's logic
     * and visualizes the game in the GUI.
     *
     * @param useExisting if {@code true}, tries to load the last saved game,
     *                    else loads a new game.
     */
    public void startGame(boolean useExisting) {
        if (useExisting && gameState == null) {
            try {
                gameState = GameState.load(GAME_SAVE);
                gameLogic.loadGameState(gameState);
            } catch (ClassNotFoundException | IOException e) {
                System.err.println("Failed to load game: " + e.getMessage());
            }
        } else if (!useExisting) {
            gameState = null;
            gameLogic.resetLogic();
        }
        gameLoop.start();
    }

    private void gameOver() {
        gameLoop.stop();
        int finalScore = gameState.score();
        showMessageBasedOnScore(finalScore);
        gameState = null;
        removeGameSaveFile();
        returnToMenu();
    }

    private void showMessageBasedOnScore(int score) {
        if (manager.getLeaderboard().isNewLeaderboardScore(score)) {
            boolean success = false;
            String errorMessage = null;
            while (!success) {
                try {
                    String username = getUsername(score, errorMessage);
                    manager.getLeaderboard().addEntry(username, score);
                    success = true;
                } catch (IllegalArgumentException e) {
                    errorMessage = e.getMessage();
                }
            }
        } else {
            showGameOverMessage(score);
        }
    }

    private String getUsername(int score, String error) {
        var dialog = new TextInputDialog();
        dialog.setTitle("Leaderboard");
        String message = "Congratulations, you are on the leaderboard!\n" +
                "You reached an impressive score of " + score + ".";
        if (error != null) {
            message = message + "\n\n" + error;
        }
        dialog.setHeaderText(message);
        dialog.setContentText("Enter your name: ");
        dialog.setGraphic(createGraphic("file:images/leaderboard.png"));
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("file:images/icon.png"));
        var username = dialog.showAndWait();
        return username.orElse("Anonymous");
    }

    private void showGameOverMessage(int score) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Your final score was " + score + ".");
        alert.setGraphic(createGraphic("file:images/game-over.png"));
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("file:images/icon.png"));
        alert.getButtonTypes().setAll(ButtonType.FINISH);
        alert.showAndWait();
    }

    private ImageView createGraphic(String filepath) {
        Image img = new Image(filepath);
        ImageView imageView = new ImageView(img);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(100);
        return imageView;
    }

    /**
     * Returns the main pane of the game GUI.
     *
     * @return main game pane
     */
    public GamePane getGamePane() {
        return gamePane;
    }

    /**
     * Saves the current state of the game logic, if one exists.
     */
    public void saveGameState() {
        if (gameState != null) {
            gameState.save(GAME_SAVE);
        }
    }

    /**
     * Returns the existing game property which tracks whether there is a saved or active
     * game logic state that can be resumed.
     *
     * @return existing game property
     */
    public ReadOnlyBooleanProperty existingGameProperty() {
        return existingGameProperty.getReadOnlyProperty();
    }

    private void updateExistingGameProperty() {
        var file = new File(GAME_SAVE);
        existingGameProperty.set(file.exists() || gameState != null);
    }

    private void returnToMenu() {
        updateExistingGameProperty();
        gameLoop.stop();
        manager.showMenu();
    }

    private void removeGameSaveFile() {
        var file = new File(GAME_SAVE);
        if (file.exists()) {
            try {
                if (!file.delete()) {
                    System.err.println("Failed to delete save game");
                }
            } catch (SecurityException e) {
                System.err.println("Failed to delete save game: " + e.getMessage());
            }
        }
    }
}

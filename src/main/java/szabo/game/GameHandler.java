package szabo.game;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

// TODO create class for the constants

public class GameHandler {
    public static final double PLAYGROUND_RATIO = 0.7;      // width : height
    public static final double WALL_THICKNESS = 0.01;           // % of Playground
    public static final double BLOCK_SIZE = 0.23;           // % of Playground Width
    public static final double LINE_POSITION = 0.3;         // % of playground height
    private static final String GAME_SAVE = "saved_game.dat";
    private static final int NANOS_IN_SECOND = 1_000_000_000;

    private final AppManager manager;
    private final GamePane gamePane;
    private final GameLogic gameLogic;
    private final AnimationTimer animationTimer;
    private final ReadOnlyBooleanWrapper existingGameProperty;
    private GameState gameState = null;

    public GameHandler(AppManager appManager) {
        manager = appManager;
        gamePane = new GamePane(this::returnToMenu);
        gameLogic = new GameLogic(this::gameOver);
        existingGameProperty = new ReadOnlyBooleanWrapper();
        updateExistingGameProperty();

        setupMouseControl();
        setupKeyboardControl();

        gameLogic.setOnMerged(
                (newValue, posX, posY) -> {
                    System.out.println("Merging " + posX + " " + posY);
                    gamePane.getPlayground().runEffect(newValue, posX, (1 - posY));
                }
        );

        animationTimer = new AnimationTimer() {
            private long lastUpdate;
            @Override
            public void start() {
                super.start();
                lastUpdate = System.nanoTime();
            }

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
                    gameLogic.moveBlock(false);
                    event.consume();
                }
                case RIGHT -> {
                    gameLogic.moveBlock(true);
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
        animationTimer.start();
    }

    private void gameOver() {
        animationTimer.stop();
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
        stage.getIcons().add(new Image("file:images/icon2048-blank.png"));
        var username = dialog.showAndWait();
        return username.orElse("Anonymous");
    }

    private void showGameOverMessage(int score) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Your final score was " + score + ".");
        alert.setGraphic(createGraphic("file:images/game-over.png"));
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("file:images/icon2048-blank.png"));
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

    public GamePane getGamePane() {
        return gamePane;
    }
    public void saveGameState() {
        if (gameState != null) {
            gameState.save(GAME_SAVE);
        }
    }
    public ReadOnlyBooleanProperty existingGameProperty() {
        return existingGameProperty.getReadOnlyProperty();
    }

    private void updateExistingGameProperty() {
        var file = new File(GAME_SAVE);
        existingGameProperty.set(file.exists() || gameState != null);
    }

    private void returnToMenu() {
        updateExistingGameProperty();
        animationTimer.stop();
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

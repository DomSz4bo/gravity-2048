package szabo.game;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.File;
import java.io.IOException;

public class GameHandler {
    public static final double PLAYGROUND_RATIO = 0.7;      // width : height
    public static final double WALL_THICKNESS = 0.01;           // % of Playground
    public static final double BLOCK_SIZE = 0.18;           // % of Playground Width
    public static final double BLOCK_START_HEIGHT = 0.1;    // % of Playground Height
    private static final String GAME_SAVE = "saved_game.dat";

    private final BooleanProperty existingGameProperty;

    private final GamePane gamePane;
    private GameState gameState = null;
    private final GameLogic gameLogic;

    private final AnimationTimer animationTimer;

    public GameHandler(AppManager appManager) {
        super();
        gamePane = new GamePane(appManager);
        gameLogic = new GameLogic();
        existingGameProperty = new SimpleBooleanProperty();
        updateExistingGameProperty();
//        Timeline ok = new Timeline(new KeyFrame(Duration.millis(16), (event) -> {
//
//        }));
//        ok.setCycleCount(Timeline.INDEFINITE);
//        ok.play();
        animationTimer = new AnimationTimer() {
            // maybe limit fps or switch to Timeline
            private long lastUpdate;
            @Override
            public void start() {
                super.start();
                lastUpdate = System.currentTimeMillis();
            }

            @Override
            public void handle(long now) {
                long delta = now - lastUpdate;
                gameLogic.tick(delta);
                gameState = gameLogic.createGameState();
                gamePane.paint(gameState);
            }
        };
//        animationTimer
    }

    public void startGame(boolean useExisting) {
        if (useExisting && gameState == null) {
            try {
                gameState = GameState.load(GAME_SAVE);
            } catch (ClassNotFoundException | IOException e) {
                System.err.println("Failed to load game: " + e.getMessage());
                gameState = new GameState();
            }
        } else  {
            gameState = new GameState();
        }
//        gameLogic.loadFromState(gameState);
        // start animation
    }

    public GamePane getGamePane() {
        return gamePane;
    }
    public BooleanProperty existingGameProperty() {
        return existingGameProperty;
    }

    private void updateExistingGameProperty() {
        var file = new File(GAME_SAVE);
        existingGameProperty.set(file.exists() || gameState != null);
    }
}

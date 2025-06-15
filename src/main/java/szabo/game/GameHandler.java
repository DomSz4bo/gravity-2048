package szabo.game;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.io.File;
import java.io.IOException;
import java.util.List;

// TODO create class for the constants

public class GameHandler {
    public static final double PLAYGROUND_RATIO = 0.7;      // width : height
    public static final double WALL_THICKNESS = 0.01;           // % of Playground
    public static final double BLOCK_SIZE = 0.18;           // % of Playground Width
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

        setupMouseControl();
        setupKeyboardControl();

//        Timeline ok = new Timeline(new KeyFrame(Duration.millis(16), (event) -> {
//
//        }));
//        ok.setCycleCount(Timeline.INDEFINITE);
//        ok.play();
        animationTimer = new AnimationTimer() {
            // maybe limit fps or switch to Timeline
            private long lastUpdate;
            private long frameCount = 0;
            @Override
            public void start() {
                super.start();
                lastUpdate = System.nanoTime();
            }

            @Override
            public void handle(long now) {
                frameCount++;
//                System.out.println("hello frame:" + frameCount);
                long delta = now - lastUpdate;
                double deltaSeconds = delta / 1000000000.0;
                gameLogic.update(deltaSeconds);

                gameState = gameLogic.createGameState();

                gamePane.paint(gameState);

                lastUpdate = now;
            }
        };
//        animationTimer
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
            } else if (event.getButton() == MouseButton.SECONDARY) {
                gamePane.getPlayground().runEffect(event.getX(), event.getY());
            }
        });

        gamePane.getPlayground().getPlaygroundPane().setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                gameLogic.handleMouseB1Released();
            }
        });
    }

    // TODO exit to menu using ESCAPE
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
//                case ESCAPE ->
            }
        });
    }

    public void startGame(boolean useExisting) {
        if (useExisting && gameState == null) {
            try {
                gameState = GameState.load(GAME_SAVE);
                System.out.println("Started existing game");
            } catch (ClassNotFoundException | IOException e) {
                System.err.println("Failed to load game: " + e.getMessage());
//                gameState = new GameState();
            }
        } else  {
//            gameState = new GameState();
            System.out.println("Started new game");
        }
        // start animation
//        var block = new GameState.BlockState(new Point2D(0.2, 0.3), 0, 0, 0, 1, 2);
        animationTimer.start();
//        gamePane.paint(gameState);
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

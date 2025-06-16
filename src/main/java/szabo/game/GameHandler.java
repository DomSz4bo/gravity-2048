package szabo.game;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.MouseButton;

import java.io.File;
import java.io.IOException;

// TODO create class for the constants

public class GameHandler {
    public static final double PLAYGROUND_RATIO = 0.7;      // width : height
    public static final double WALL_THICKNESS = 0.01;           // % of Playground
    public static final double BLOCK_SIZE = 0.18;           // % of Playground Width
    public static final double LINE_POSITION = 0.2;  // % of playground height
    private static final String GAME_SAVE = "saved_game.dat";
    private static final int NANOS_IN_SECOND = 1_000_000_000;

    private final BooleanProperty existingGameProperty;

    private final GamePane gamePane;
    private GameState gameState = null;
    private final GameLogic gameLogic;
    private final AnimationTimer animationTimer;
    private final AppManager manager;

    public GameHandler(AppManager appManager) {
        super();
        manager = appManager;
        gamePane = new GamePane(this::returnToMenu);
        gameLogic = new GameLogic();
        existingGameProperty = new SimpleBooleanProperty();
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
                // TODO remove the effect generation
            } else if (event.getButton() == MouseButton.SECONDARY) {
                double posX = event.getX() / gamePane.getPlayground().getPlaygroundPane().getWidth();
                double posY = event.getY() / gamePane.getPlayground().getPlaygroundPane().getHeight();
                gamePane.getPlayground().runEffect(2, posX, posY);
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
//                case ESCAPE, BACK_SPACE ->
            }
        });
    }

    public void startGame(boolean useExisting) {
        if (useExisting && gameState == null) {
            try {
                gameState = GameState.load(GAME_SAVE);
                gameLogic.loadGameState(gameState);
                System.out.println("Loaded existing game");
            } catch (ClassNotFoundException | IOException e) {
                System.err.println("Failed to load game: " + e.getMessage());
            }
        } else if (!useExisting) {
            System.out.println("Started new game");
            gameState = null;
            gameLogic.resetLogic();
        }
        // start animation
        animationTimer.start();
    }

    public GamePane getGamePane() {
        return gamePane;
    }
    public void saveGameState() {
        if (gameState != null) {
            gameState.save(GAME_SAVE);
        }
    }
    public BooleanProperty existingGameProperty() {
        return existingGameProperty;
    }

    private void updateExistingGameProperty() {
        var file = new File(GAME_SAVE);
        existingGameProperty.set(file.exists() || gameState != null);
    }

    private void returnToMenu() {
        animationTimer.stop();
        manager.showMenu();
    }
}

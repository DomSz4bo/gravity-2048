package szabo.game;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.File;
import java.io.IOException;

public class GameHandler {
    private static final String GAME_SAVE = "saved_game.dat";

    private final BooleanProperty existingGameProperty;

    private final GamePane gamePane;
    private GameState gameState = null;

    public GameHandler(AppManager appManager) {
        super();
        gamePane = new GamePane(appManager);
        existingGameProperty = new SimpleBooleanProperty();
        updateExistingGameProperty();
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
    }

    public GameState getGameState() {
        return gameState;
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

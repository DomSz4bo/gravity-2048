package szabo.game;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.StackPane;

/**
 *
 */
public class AppManager extends StackPane {
    private static final String LEADERBOARD_FILE = "leaderboard.dat";
    private static final int LEADERBOARD_LIMIT = 10;

    private final Leaderboard leaderboard = new Leaderboard(LEADERBOARD_FILE, LEADERBOARD_LIMIT);
    private final GameHandler gameHandler = new GameHandler(this);
    private final MenuPane menuPane = new MenuPane(this);
    private final LeaderboardPane leaderboardPane = new LeaderboardPane(this::showMenu);

    public AppManager() {
        menuPane.maxWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(getWidth() / 2, GameApp.MIN_WIDTH * 0.8), widthProperty()
        ));
        leaderboardPane.maxWidthProperty().bind(widthProperty().multiply(0.6));
        leaderboardPane.maxHeightProperty().bind(heightProperty().multiply(0.8));
        leaderboardPane.updateWith(leaderboard);
        leaderboard.setOnChange(() -> leaderboardPane.updateWith(leaderboard));

        getStyleClass().add("mainWindow");
        getChildren().addAll(gameHandler.getGamePane(), menuPane,  leaderboardPane);
        showMenu();

        menuPane.loadButtons(gameHandler.existingGameProperty().get());
        gameHandler.existingGameProperty().addListener(
                (observable, oldValue, newValue) -> menuPane.loadButtons(newValue)
        );

    }

    public void loadGame(boolean useExisting) {
        gameHandler.startGame(useExisting);
        showGame();
    }

    private void showGame() {
        gameHandler.getGamePane().setVisible(true);
        menuPane.setVisible(false);
        leaderboardPane.setVisible(false);
    }

    public void showMenu() {
        menuPane.enableButtons();
        menuPane.setVisible(true);
        gameHandler.getGamePane().setVisible(false);
        leaderboardPane.setVisible(false);
    }

    public void showLeaderboard() {
        leaderboardPane.setVisible(true);
        menuPane.disableButtons();
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }
    public void saveLeaderboard() {
        leaderboard.save(LEADERBOARD_FILE);
    }

    public void saveGame() {
        gameHandler.saveGameState();
    }
}

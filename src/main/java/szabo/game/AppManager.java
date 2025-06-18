package szabo.game;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.StackPane;

/**
 * The manager class for the application's elements.
 * The main elements are the menu, game and leaderboard.
 * <p>
 * Handles interactions between elements and
 * provides methods for controlling the application.
 * </p>
 */
public class AppManager extends StackPane {
    private static final String LEADERBOARD_FILE = "leaderboard.dat";
    private static final int LEADERBOARD_LIMIT = 10;

    private final Leaderboard leaderboard = new Leaderboard(LEADERBOARD_FILE, LEADERBOARD_LIMIT);
    private final GameHandler gameHandler = new GameHandler(this);
    private final MenuPane menuPane = new MenuPane(this);
    private final LeaderboardPane leaderboardPane = new LeaderboardPane(this::showMenu);

    /**
     * Creates an instance of {@link AppManager}.
     * <p>
     * Initializes the application's elements. Sets up the necessary interactions
     * between elements and configures element dimensions.
     * </p>
     */
    public AppManager() {
        leaderboardPane.maxWidthProperty().bind(widthProperty().multiply(0.6));
        leaderboardPane.maxHeightProperty().bind(heightProperty().multiply(0.8));
        leaderboardPane.updateWith(leaderboard);
        leaderboard.setOnChange(() -> leaderboardPane.updateWith(leaderboard));

        gameHandler.existingGameProperty().addListener(
                (observable, oldValue, newValue) -> menuPane.loadButtons(newValue)
        );

        menuPane.loadButtons(gameHandler.existingGameProperty().get());
        menuPane.maxWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(getWidth() / 2, GameApp.MIN_WIDTH * 0.8), widthProperty()
        ));

        getStyleClass().add("mainWindow");
        getChildren().addAll(gameHandler.getGamePane(), menuPane,  leaderboardPane);
        showMenu();
    }

    /**
     * Starts and shows the game.
     * @param useExisting if {@code true}, tries to load the last saved game,
     *                    else loads a new game.
     */
    public void startGame(boolean useExisting) {
        gameHandler.startGame(useExisting);
        showGame();
    }

    /**
     * Makes the game visible. Hides leaderboard and menu visuals.
     */
    private void showGame() {
        gameHandler.getGamePane().setVisible(true);
        menuPane.setVisible(false);
        leaderboardPane.setVisible(false);
    }

    /**
     * Makes the menu visible and enables the menu's buttons.
     * Hides leaderboard and game visuals.
     */
    public void showMenu() {
        menuPane.enableButtons();
        menuPane.setVisible(true);
        gameHandler.getGamePane().setVisible(false);
        leaderboardPane.setVisible(false);
    }

    /**
     * Makes the leaderboard visible and disables the menu's buttons.
     * Application elements other than the leaderboard retain their visibility.
     */
    public void showLeaderboard() {
        leaderboardPane.setVisible(true);
        menuPane.disableButtons();
    }

    /**
     * Returns the active leaderboard containing high scores.
     *
     * @return the leaderboard
     */
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    /**
     * Saves leaderboard and game data.
     */
    public void saveData() {
        leaderboard.save(LEADERBOARD_FILE);
        gameHandler.saveGameState();
    }
}

package szabo.game;

import javafx.scene.layout.StackPane;

public class AppManager extends StackPane {
    private final Leaderboard leaderboard = new Leaderboard();

    private final GameHandler gameHandler;
    private final MenuPane menuPane;
    private final LeaderboardPane leaderboardPane;

    public AppManager() {
        super();
        menuPane = new MenuPane(this);
        gameHandler = new GameHandler(this);
        leaderboardPane = new LeaderboardPane();
        leaderboardPane.maxWidthProperty().bind(widthProperty().multiply(0.6));
        leaderboardPane.maxHeightProperty().bind(heightProperty().multiply(0.8));
        leaderboardPane.updateWith(leaderboard);

        leaderboard.setOnChange(() -> leaderboardPane.updateWith(leaderboard));

        getChildren().addAll(gameHandler.getGamePane(), menuPane,  leaderboardPane);
        showMenu();
        setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 50%, #0D2857, #070F33);");

        menuPane.loadButtons(gameHandler.existingGameProperty().get());
        gameHandler.existingGameProperty().addListener((observable, oldValue, newValue) -> {
            menuPane.loadButtons(newValue);
        });

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
        menuPane.setVisible(true);
        gameHandler.getGamePane().setVisible(false);
        leaderboardPane.setVisible(false);
    }

    public void showLeaderboard() {
        leaderboardPane.setVisible(true);
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    public void saveGame() {
        gameHandler.saveGameState();
    }
}

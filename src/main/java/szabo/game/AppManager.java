package szabo.game;

import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class AppManager extends StackPane {
    private final Leaderboard leaderboard = new Leaderboard();

    private final GameHandlerPane gameHandlerPane;
    private final MenuPane menuPane;
    private final LeaderboardPane leaderboardPane;

    public AppManager() {
        super();
        menuPane = new MenuPane(this);
        gameHandlerPane = new GameHandlerPane(this);

        leaderboardPane = new LeaderboardPane(this);
        leaderboardPane.maxWidthProperty().bind(widthProperty().multiply(0.6));
        leaderboardPane.maxHeightProperty().bind(heightProperty().multiply(0.8));

        getChildren().addAll(gameHandlerPane, menuPane,  leaderboardPane);
        showMenu();
//        setBackground(Background.fill(Color.web("#070F33")));
        setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 50%, #0D2857, #070F33);");

    }

    public void showGame() {
        gameHandlerPane.setVisible(true);
        menuPane.setVisible(false);
        leaderboardPane.setVisible(false);
    }

    public void showMenu() {
        menuPane.setVisible(true);
        gameHandlerPane.setVisible(false);
        leaderboardPane.setVisible(false);
    }

    public void showLeaderboard() {
        leaderboardPane.setVisible(true);
    }

    public MenuPane getMenuPane() {
        return menuPane;
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }


}

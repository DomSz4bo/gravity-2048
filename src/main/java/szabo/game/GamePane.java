package szabo.game;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class GamePane extends BorderPane {

    private static final String SCORE = "Score: ";
    private static final String HIGH_SCORE = "High Score: ";

//    private GameState gameState;
    private final Leaderboard leaderboard;
    private final Label scoreLabel = new Label();
    private final Label highScoreLabel = new Label();
    private final Playground playground = new Playground();

    private boolean beingDragged = false;

    public GamePane(AppManager manager) {
        super();
        leaderboard = manager.getLeaderboard();

//        setPadding(new Insets(0, 5, 0, 5));

        scoreLabel.setStyle("-fx-font-size: 25");
//        scoreLabel.setBackground(Background.fill(Color.BLUEVIOLET));
        scoreLabel.setMaxWidth(Double.MAX_VALUE);
        scoreLabel.setAlignment(Pos.CENTER);
        HBox.setHgrow(scoreLabel, Priority.ALWAYS);

        highScoreLabel.setStyle("-fx-font-size: 25");
//        highScoreLabel.setBackground(Background.fill(Color.RED));

        var exitButton = new Button("Back to Menu");
        exitButton.setStyle("-fx-font-size: 25");
        exitButton.setOnAction(e -> manager.showMenu());


        var scorePane = new HBox(highScoreLabel, scoreLabel, exitButton);
//        scorePane.setBackground(Background.fill(Color.ORANGE));
        scorePane.setAlignment(Pos.CENTER);
        setTop(scorePane);
        scorePane.prefHeightProperty().bind(heightProperty().divide(16));

        // ------------------------------------------------

        setCenter(playground);

    }

    public void paint(GameState gameState) {
        scoreLabel.setText(SCORE + gameState.getScore());
        highScoreLabel.setText(HIGH_SCORE + leaderboard.getHighScore());
    }

}

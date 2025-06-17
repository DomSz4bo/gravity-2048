package szabo.game;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.Optional;

public class GamePane extends BorderPane {

    private final Label scoreLabel = new Label();
    private final Label scoreToBeatLabel = new Label();
    private final Playground playground = new Playground();

    public GamePane(Runnable onExit) {
        scoreLabel.setStyle("-fx-font-size: 25");
        scoreLabel.setMaxWidth(Double.MAX_VALUE);
        scoreLabel.setAlignment(Pos.CENTER);
        HBox.setHgrow(scoreLabel, Priority.ALWAYS);

        scoreToBeatLabel.setStyle("-fx-font-size: 25");

        var exitButton = new Button("Back to Menu");
        exitButton.setStyle("-fx-font-size: 25");
        exitButton.setOnAction(event -> onExit.run());

        var scorePane = new HBox(scoreToBeatLabel, scoreLabel, exitButton);
        scorePane.setAlignment(Pos.CENTER);
        scorePane.prefHeightProperty().bind(heightProperty().divide(16));

        setCenter(playground);
        setTop(scorePane);

    }

    public void paint(GameState gameState, Leaderboard leaderboard) {
        Optional<Integer> scoreToBeat = findScoreToBeat(gameState.score(), leaderboard);
        String scoreToBeatText = scoreToBeat.map(i -> "Score to beat: " + i)
                                            .orElse("New High Score!");
        scoreToBeatLabel.setText(scoreToBeatText);
        scoreLabel.setText("Score: " + gameState.score());
        playground.paintBlocks(gameState.getAllBlocks());
    }

    private Optional<Integer> findScoreToBeat(int currentScore, Leaderboard leaderboard) {
        return leaderboard.getScores().reversed().stream()
                .filter(entry -> currentScore <= entry.score())
                .findFirst().map(Leaderboard.ScoreEntry::score);
    }

    public Playground getPlayground() {
        return playground;
    }

}

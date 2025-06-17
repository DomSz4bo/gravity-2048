package szabo.game;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;

import java.util.Optional;

public class GamePane extends BorderPane {

    private final Label scoreLabel = new Label();
    private final Label scoreToBeatLabel = new Label();
    private final Playground playground = new Playground();

    public GamePane(Runnable onExit) {
        scoreLabel.setMaxWidth(Double.MAX_VALUE);
        scoreLabel.setAlignment(Pos.CENTER);
        HBox.setHgrow(scoreLabel, Priority.ALWAYS);

        var exitButton = new Button("Back to Menu");
        exitButton.setStyle("-fx-font-size: 25");
        exitButton.setOnAction(event -> onExit.run());
        exitButton.paddingProperty().bind(Bindings.createObjectBinding(
                () -> {
                    double fontSize = exitButton.getFont().getSize();
                    double vPad = fontSize / 6;
                    double hPad = fontSize * 0.8;
                    return new Insets(vPad, hPad, vPad, hPad);
                }, exitButton.fontProperty()
        ));

        var scorePane = new HBox(scoreToBeatLabel, scoreLabel, exitButton);
        scorePane.setAlignment(Pos.CENTER);
        scorePane.paddingProperty().bind(Bindings.createObjectBinding(
                () -> {
                    double padding = widthProperty().get()/50;
                    return new Insets(padding/4, padding, padding/4, padding);
                }, widthProperty()
        ));

        setCenter(playground);
        setTop(scorePane);

        Platform.runLater(() -> {
            bindFontSize(scoreLabel);
            bindFontSize(scoreToBeatLabel);
            bindFontSize(exitButton);
        });
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

    private void bindFontSize(Labeled element) {
        var currentFont = element.getFont();
        element.fontProperty().bind(Bindings.createObjectBinding(
                () -> {
                    double widthBound = getWidth() * 0.025;
                    double heightBound = getHeight() * 0.035;
                    return Font.font(
                            currentFont.getFamily(),
                            Math.max(12, Math.min(widthBound, heightBound))
                    );
                }, widthProperty(), heightProperty()
        ));
    }

}

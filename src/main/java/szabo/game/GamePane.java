package szabo.game;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class GamePane extends BorderPane {

    private static final double PLAYGROUND_RATIO = 0.7;     // width : height
    private static final double LINE_POSITION = 0.2;  // % of playground height
    private static final String SCORE = "Score: ";
    private static final String HIGH_SCORE = "High Score: ";

    private GameState gameState;
    private final Leaderboard leaderboard;
    private final Label scoreLabel = new Label();
    private final Label highScoreLabel = new Label();

    public GamePane(AppManager manager) {
        super();
        leaderboard = manager.getLeaderboard();

//        setPadding(new Insets(0, 5, 0, 5));

        scoreLabel.setStyle("-fx-font-size: 25");
        scoreLabel.setBackground(Background.fill(Color.BLUEVIOLET));
        scoreLabel.setMaxWidth(Double.MAX_VALUE);
        scoreLabel.setAlignment(Pos.CENTER);
        HBox.setHgrow(scoreLabel, Priority.ALWAYS);

        highScoreLabel.setStyle("-fx-font-size: 25");
        highScoreLabel.setBackground(Background.fill(Color.RED));

        var exitButton = new Button("Back to Menu");
        exitButton.setStyle("-fx-font-size: 25");
        exitButton.setOnAction(e -> manager.showMenu());


        var scorePane = new HBox(highScoreLabel, scoreLabel, exitButton);
        scorePane.setBackground(Background.fill(Color.ORANGE));
        scorePane.setAlignment(Pos.CENTER);
        setTop(scorePane);
        scorePane.prefHeightProperty().bind(heightProperty().divide(16));

        // ------------------------------------------------

        var playgroundPane = new Pane();
        var container = new StackPane(playgroundPane);
        setCenter(container);
        playgroundPane.setBackground(Background.fill(Color.web("rgba(0, 0, 0, 0.4)")));
        playgroundPane.setBorder(new Border(new BorderStroke(
                Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(0, 0.01, 0.01, 0.01,
                        false, true, true, true)
        )));

        Line line = new Line(0, 20, playgroundPane.getWidth(), 20);
        line.setStroke(Color.WHITE);
        line.endXProperty().bind(playgroundPane.widthProperty());
        line.startYProperty().bind(playgroundPane.heightProperty().multiply(LINE_POSITION));
        line.endYProperty().bind(playgroundPane.heightProperty().multiply(LINE_POSITION));

        playgroundPane.getChildren().add(line);

        playgroundPane.heightProperty().addListener(
                e -> System.out.println("Width to Height: " + playgroundPane.getWidth() / playgroundPane.getHeight())
        );
        playgroundPane.widthProperty().addListener(
                e -> System.out.println("Width to Height: " + playgroundPane.getWidth() / playgroundPane.getHeight())
        );

        playgroundPane.maxWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(container.getWidth(), container.getHeight() * PLAYGROUND_RATIO),
                container.widthProperty(),  container.heightProperty()
        ));
        playgroundPane.maxHeightProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(container.getHeight(), container.getWidth() / PLAYGROUND_RATIO),
                container.heightProperty(),  container.widthProperty()
        ));
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    private void updateScoreLabel() {
        scoreLabel.setText(SCORE + gameState.getScore());
    }

    private void updateHighScoreLabel() {
        highScoreLabel.setText(HIGH_SCORE + leaderboard.getHighScore());
    }
}

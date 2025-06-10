package szabo.game;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class LeaderboardPane extends BorderPane {
    private final AppManager appManager;
    private final VBox scoreList = new VBox();
    private final DoubleBinding minDimension;

    public LeaderboardPane(AppManager appManager) {
        this.appManager = appManager;

        Label title =  new Label("Leaderboard");
        VBox titleVBox = new VBox(title);
        setTop(titleVBox);
        titleVBox.setAlignment(Pos.CENTER);
//        titleVBox.setBackground(Background.fill(Color.web("#FFC680")));

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> setVisible(false));
        var btnPane = new HBox(closeButton);
        btnPane.setAlignment(Pos.CENTER);
        setBottom(btnPane);

        scoreList.setAlignment(Pos.CENTER);
        scoreList.setSpacing(10);
        scoreList.setPadding(new Insets(10));
//        scoreList.getStyleClass().add("score-list");
        ScrollPane scrollPane = new ScrollPane(scoreList);
        scrollPane.setFitToWidth(true);
        setCenter(scrollPane);

        setBackground(Background.fill(Color.web("rgba(0, 0, 0, 0.9)")));

        minDimension = Bindings.createDoubleBinding(
                () -> Math.min(getWidth(), getHeight()),
                widthProperty(), heightProperty()
        );

        title.prefHeightProperty().bind(heightProperty().multiply(0.15));
        bindFontSizeToMinDimension(title, 20, 15);
//        title.setBackground(Background.fill(Color.LIGHTGRAY));

        btnPane.prefHeightProperty().bind(heightProperty().multiply(0.1));
//        btnPane.setBackground(Background.fill(Color.LIGHTGRAY));
        closeButton.paddingProperty().bind(Bindings.createObjectBinding(
                () -> {
                    double verticalPadding = minDimension.get() / 20;
                    double horizontalPadding = minDimension.get() / 100;
                    return new Insets(horizontalPadding, verticalPadding,
                            horizontalPadding, verticalPadding);
                },
                minDimension
        ));
        bindFontSizeToMinDimension(closeButton, 14, 30);

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        var entries = appManager.getLeaderboard().getScores();

        for (int i = 0; i < entries.size(); i++) {
            Leaderboard.ScoreEntry entry = entries.get(i);
            Label rank = new Label((i + 1) + ".");
            Label name = new Label(entry.name());
            Label score = new Label(String.valueOf(entry.score()));

            bindFontSizeToMinDimension(rank, 14, 30);
            bindFontSizeToMinDimension(name, 14, 30);
            bindFontSizeToMinDimension(score, 14, 30);

            HBox row = new HBox(10, rank, name, score);
            row.setAlignment(Pos.CENTER);
            row.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(name, Priority.ALWAYS);
            name.setMaxWidth(Double.MAX_VALUE);
            name.setAlignment(Pos.CENTER_LEFT);
            score.setAlignment(Pos.CENTER_RIGHT);
            score.setMinWidth(60);

            scoreList.getChildren().add(row);
        }
    }

    private void bindFontSizeToMinDimension(Labeled element, int minimum, int divFactor) {
        if (minimum <= 0) {
            throw new IllegalArgumentException("minimum must be greater than 0");
        }
        element.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(Math.max(minimum, minDimension.get() / divFactor)), minDimension
        ));
    }

}

package szabo.game;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;


public class LeaderboardPane extends BorderPane {
    private final VBox scoreList = new VBox();
    private final DoubleBinding minDimension;

    public LeaderboardPane(Runnable onClose) {
        Label title = new Label("Leaderboard");
        title.getStyleClass().add("leaderboard-title");
        VBox titleVBox = new VBox(title);
        setTop(titleVBox);
        titleVBox.setAlignment(Pos.CENTER);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> onClose.run());
        var btnPane = new HBox(closeButton);
        btnPane.setAlignment(Pos.CENTER);
        setBottom(btnPane);

        scoreList.setAlignment(Pos.CENTER);
        scoreList.setSpacing(10);
        scoreList.paddingProperty().bind(Bindings.createObjectBinding(
                () -> {
                    double horizontalPadding = getWidth() / 20;
                    return new Insets(0, horizontalPadding, 0, horizontalPadding);
                },
                widthProperty()
        ));
        ScrollPane scrollPane = new ScrollPane(scoreList);
        scrollPane.setFitToWidth(true);
        setCenter(scrollPane);
        getStyleClass().add("leaderboard-pane");

        minDimension = Bindings.createDoubleBinding(
                () -> Math.min(getWidth(), getHeight()),
                widthProperty(), heightProperty()
        );

        title.prefHeightProperty().bind(heightProperty().multiply(0.15));
        btnPane.prefHeightProperty().bind(heightProperty().multiply(0.1));
        closeButton.paddingProperty().bind(Bindings.createObjectBinding(
                () -> {
                    double verticalPadding = minDimension.get() / 20;
                    double horizontalPadding = minDimension.get() / 100;
                    return new Insets(horizontalPadding, verticalPadding,
                            horizontalPadding, verticalPadding);
                },
                minDimension
        ));

        Platform.runLater(() -> {
            bindFontSizeToMinDimension(title, 20, 15);
            bindFontSizeToMinDimension(closeButton, 14, 25);
        });
    }

    public void updateWith(Leaderboard leaderboard) {
        scoreList.getChildren().clear();
        var entries = leaderboard.getScores();
        for (int i = 0; i < entries.size(); i++) {
            Leaderboard.ScoreEntry entry = entries.get(i);
            scoreList.getChildren().add(new LeaderboardPaneEntry(entry, i + 1));
        }
    }

    private void bindFontSizeToMinDimension(Labeled element, int minimum, int divFactor) {
        if (minimum <= 0) {
            throw new IllegalArgumentException("minimum must be greater than 0");
        }
        var currentFontFamily = element.getFont().getFamily();
        element.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(
                        currentFontFamily,
                        Math.max(minimum, minDimension.get() / divFactor)
                ), minDimension
        ));
    }

    private class LeaderboardPaneEntry extends HBox {

        public LeaderboardPaneEntry(Leaderboard.ScoreEntry entry, int rank) {
            super(10);
            var rankLabel = new Label(rank + ".");
            var nameLabel = new Label(entry.name());
            var scoreLabel = new Label(String.valueOf(entry.score()));
            getChildren().addAll(rankLabel, nameLabel, scoreLabel);

            switch (rank) {
                case 1 -> addStyleClassToChildren("rank_1");
                case 2 -> addStyleClassToChildren("rank_2");
                case 3 -> addStyleClassToChildren("rank_3");
            }

            Platform.runLater(() -> {
                bindFontSizeToMinDimension(rankLabel, 14, 25);
                bindFontSizeToMinDimension(nameLabel, 14, 25);
                bindFontSizeToMinDimension(scoreLabel, 14, 25);
            });

            HBox.setHgrow(nameLabel, Priority.ALWAYS);
            nameLabel.setMaxWidth(Double.MAX_VALUE);
            nameLabel.setAlignment(Pos.CENTER_LEFT);
            scoreLabel.setAlignment(Pos.CENTER_RIGHT);
            scoreLabel.setMinWidth(60);

        }

        private void addStyleClassToChildren(String className) {
            getChildren().forEach(child -> child.getStyleClass().add(className));
        }
    }

}

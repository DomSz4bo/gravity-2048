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
        scoreList.paddingProperty().bind(Bindings.createObjectBinding(
                () -> {
                    double horizontalPadding = getWidth() / 20;
                    return new Insets(0, horizontalPadding, 0, horizontalPadding);
                },
                widthProperty()
        ));
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

        Platform.runLater(() -> {
            bindFontSizeToMinDimension(title, 20, 15);
            bindFontSizeToMinDimension(closeButton, 14, 30);
        });


        loadLeaderboard();
    }

    private void loadLeaderboard() {
        var entries = appManager.getLeaderboard().getScores();

        for (int i = 0; i < entries.size(); i++) {
            Leaderboard.ScoreEntry entry = entries.get(i);
            scoreList.getChildren().add(new LeaderboardPaneEntry(entry, i + 1));
        }
    }

    private void bindFontSizeToMinDimension(Labeled element, int minimum, int divFactor) {
        if (minimum <= 0) {
            throw new IllegalArgumentException("minimum must be greater than 0");
        }
//        System.out.println(element.getFont().getFamily());
        var currentFont = element.getFont();
        element.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(currentFont.getFamily(), Math.max(minimum, minDimension.get() / divFactor)),
                minDimension
        ));
    }

    private class LeaderboardPaneEntry extends HBox {

        public LeaderboardPaneEntry(Leaderboard.ScoreEntry entry, int rank) {
            super(10);
            var rankLabel = new Label(rank + ".");
            var nameLabel = new Label(entry.name());
            var scoreLabel = new Label(String.valueOf(entry.score()));
            getChildren().addAll(rankLabel, nameLabel, scoreLabel);

            Platform.runLater(() -> {
                bindFontSizeToMinDimension(rankLabel, 14, 30);
                bindFontSizeToMinDimension(nameLabel, 14, 30);
                bindFontSizeToMinDimension(scoreLabel, 14, 30);
            });


            HBox.setHgrow(nameLabel, Priority.ALWAYS);
            nameLabel.setMaxWidth(Double.MAX_VALUE);
            nameLabel.setAlignment(Pos.CENTER_LEFT);
            scoreLabel.setAlignment(Pos.CENTER_RIGHT);
            scoreLabel.setMinWidth(60);

            addStyleClassToChildren("leaderboard");

            switch (rank) {
                case 1 -> addStyleClassToChildren("rank_1");
                case 2 -> addStyleClassToChildren("rank_2");
                case 3 -> addStyleClassToChildren("rank_3");
            }
        }

        private void addStyleClassToChildren(String className) {
            getChildren().forEach(child -> child.getStyleClass().add(className));
        }
    }

}

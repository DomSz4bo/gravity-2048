package szabo.game;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MenuPane extends StackPane {
    private ViewManager viewManager;

    private ImageView titleImage = new ImageView();

    private VBox buttonContainer = new VBox();
    private Button playButton =  new Button("Play");
    private Button exitButton  =  new Button("Exit");
    private Button newGameButton =  new Button("New Game");
    private Button leaderboardButton =  new Button("Leaderboard");

    private Leaderboard leaderboard = new Leaderboard();



    public MenuPane(ViewManager viewManager) {
        super();
        setBackground(Background.fill(Color.web("#070F33")));
        this.viewManager = viewManager;

        playButton.setOnAction(e -> viewManager.showGame());
        newGameButton.setOnAction(e -> viewManager.showGame());
        leaderboardButton.setOnAction(e -> leaderboard.setVisible(true));
        exitButton.setOnAction(e -> System.exit(0));

        DoubleBinding minDimension =  Bindings.createDoubleBinding(() ->
            Math.min(getWidth(), getHeight()), heightProperty(), widthProperty()
        );

        var mainPane = new VBox();
        mainPane.setAlignment(Pos.CENTER);
        mainPane.getChildren().addAll(titleImage, buttonContainer);
        mainPane.spacingProperty().bind(minDimension.divide(20));
        mainPane.maxWidthProperty().bind(widthProperty().divide(2));
//        mainPane.setBackground(Background.fill(Color.web("rgba(255,198,128,1)")));

        Image img = new Image("file:images/icon2048.png");
        titleImage.setImage(img);
        titleImage.setPreserveRatio(true);
        titleImage.fitWidthProperty().bind(minDimension.divide(3));

        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(
                playButton, newGameButton, leaderboardButton, exitButton
        );
        buttonContainer.spacingProperty().bind(minDimension.divide(50));
        for (Node node : buttonContainer.getChildren()) {
            Button btn = (Button) node;
            btn.fontProperty().bind(Bindings.createObjectBinding(
                    () -> Font.font(Math.max(minDimension.get() / 35, 14)), minDimension
            ));
            btn.paddingProperty().bind(Bindings.createObjectBinding(
                    () -> {
                        double verticalPadding = minDimension.get() / 20;
                        double horizontalPadding = minDimension.get() / 100;
                        return new Insets(horizontalPadding, verticalPadding,
                                horizontalPadding, verticalPadding);
                    },
                    minDimension
            ));
            btn.prefWidthProperty().bind(mainPane.widthProperty().multiply(0.8));
        }

        leaderboard.setVisible(false);
        leaderboard.maxWidthProperty().bind(widthProperty().multiply(0.6));
        leaderboard.maxHeightProperty().bind(heightProperty().multiply(0.8));

        getChildren().addAll(mainPane, leaderboard);
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }
}

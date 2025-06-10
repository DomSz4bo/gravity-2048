package szabo.game;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class MenuPane extends VBox {

    public MenuPane(AppManager appManager) {
        super();
//        setBackground(Background.fill(Color.web("#070F33")));

        Button playButton = new Button("Play");
        Button newGameButton = new Button("New Game");
        Button leaderboardButton = new Button("Leaderboard");
        Button exitButton = new Button("Exit");
        VBox buttonContainer = new VBox();
        ImageView titleImage = new ImageView();

        playButton.setOnAction(e -> appManager.showGame());
        newGameButton.setOnAction(e -> appManager.showGame());
        leaderboardButton.setOnAction(e -> appManager.showLeaderboard());
        exitButton.setOnAction(e -> System.exit(0));

        DoubleBinding minDimension =  Bindings.createDoubleBinding(() ->
            Math.min(appManager.getWidth(), appManager.getHeight()),
            appManager.heightProperty(), appManager.widthProperty()
        );

        setAlignment(Pos.CENTER);
        getChildren().addAll(titleImage, buttonContainer);
        spacingProperty().bind(minDimension.divide(20));
        maxWidthProperty().bind(appManager.widthProperty().divide(2));
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
            btn.styleProperty().bind(Bindings.createStringBinding(
                    () -> String.format("-fx-font-size: %.2fpx;", Math.max(minDimension.get() / 35, 14)),
                    minDimension
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
            btn.prefWidthProperty().bind(widthProperty().multiply(0.8));
        }
    }

}

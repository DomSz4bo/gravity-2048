package szabo.game;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

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


        var menuPane = new VBox();
        menuPane.setAlignment(Pos.CENTER);
        menuPane.getChildren().addAll(titleImage, buttonContainer);
        menuPane.setSpacing(50);


        Image img = new Image("file:images/icon2048.png");
        titleImage.setImage(img);
        titleImage.setPreserveRatio(true);
        titleImage.setFitHeight(150);


        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(
                playButton, newGameButton, leaderboardButton, exitButton
        );

        leaderboard.setVisible(false);

        getChildren().addAll(menuPane, leaderboard);
    }
}

package szabo.game;

import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class GameHandlerPane extends StackPane {
    private static final double WIDTH_TO_HEIGHT_RATIO = 0.5;

    private AppManager appManager;
    private GamePane gamePane;

    public GameHandlerPane(AppManager appManager) {
        super();
        this.appManager = appManager;
        gamePane = new GamePane(appManager);
        setBackground(Background.fill(Color.web("#0D2857")));
        getChildren().add(gamePane);

//        DoubleBinding gameWidth

//        gamePane.maxWidthProperty().bind(widthProperty().divide(2));

        double width = getWidth();
        double height = getHeight();

        double gameWidth = (height * WIDTH_TO_HEIGHT_RATIO);
        double gameHeight = height;
        if (gameWidth > width) {
            gameWidth = width;
            gameHeight = width * 2;
        }
    }

    public void loadGame(GameState gameState) {
//        this.gameState = ;
    }

    public void pauseGame() {
    }
    public void resumeGame() {
    }


}

package szabo.game;

import javafx.scene.layout.StackPane;

public class GameHandlerPane extends StackPane {
    private static final double WIDTH_TO_HEIGHT_RATIO = 0.5;

    private final AppManager appManager;
    private final GamePane gamePane;

    public GameHandlerPane(AppManager appManager) {
        super();
        this.appManager = appManager;
        gamePane = new GamePane(appManager);
//        setBackground(Background.fill(Color.web("#0D2857")));
        getChildren().add(gamePane);

    }

    public void loadGame(GameState gameState) {
//        this.gameState = ;
    }



}

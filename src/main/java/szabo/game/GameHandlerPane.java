package szabo.game;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class GameHandlerPane extends StackPane {
    private static final double WIDTH_TO_HEIGHT_RATIO = 0.5;

    private final AppManager appManager;
    private final GamePane gamePane;

    public GameHandlerPane(AppManager appManager) {
        super();
        this.appManager = appManager;
        gamePane = new GamePane(appManager);
        setBackground(Background.fill(Color.web("#0D2857")));
        getChildren().add(gamePane);

//        DoubleBinding gameWidth

        gamePane.maxWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(getWidth(), getHeight() * WIDTH_TO_HEIGHT_RATIO),
                widthProperty(), heightProperty()
        ));
    }

    public void loadGame(GameState gameState) {
//        this.gameState = ;
    }

    public void pauseGame() {
    }
    public void resumeGame() {
    }


}

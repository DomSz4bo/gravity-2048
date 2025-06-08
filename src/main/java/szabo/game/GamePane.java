package szabo.game;

import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class GamePane extends Pane {
    private GameState gameState;
    private ViewManager viewManager;

    public GamePane(ViewManager viewManager) {
        super();
        this.viewManager = viewManager;
        setBackground(Background.fill(Color.web("#0D2857")));
    }

    public void loadGame(GameState gameState) {
//        this.gameState = ;
    }

    public void pauseGame() {
    }
    public void resumeGame() {
    }


}

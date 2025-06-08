package szabo.game;

import javafx.scene.layout.StackPane;

public class ViewManager extends StackPane {
    private final GamePane gamePane = new GamePane(this);
    private final MenuPane menuPane = new MenuPane(this);

    public ViewManager() {
        super();
        getChildren().addAll(gamePane, menuPane);
        showMenu();
    }

    public void showGame() {
        gamePane.setVisible(true);
        menuPane.setVisible(false);
    }

    public void showMenu() {
        menuPane.setVisible(true);
        gamePane.setVisible(false);
    }

    public MenuPane getMenuPane() {
        return menuPane;
    }

    public GamePane getGamePane() {
        return gamePane;
    }

}

package szabo.game;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class responsible for providing the main GUI for controlling the application manager's
 * basic functions. The basic functions include resuming an existing game or starting a new game,
 * showing the leaderboard and closing the application.
 */
public class MenuPane extends VBox {

    private final VBox buttonContainer = new VBox();
    private final List<Button> buttons = new ArrayList<>();

    /**
     * Creates a new {@link MenuPane}.
     * <p>
     * Initializes the menu GUI - creates necessary buttons, configures element dimensions
     * font scaling and title image.
     * </p>
     *
     * @param appManager the application manager to communicate with
     */
    public MenuPane(AppManager appManager) {
        Button playButton = new Button("Resume Game");
        Button newGameButton = new Button("New Game");
        Button leaderboardButton = new Button("Leaderboard");
        Button exitButton = new Button("Exit");
        ImageView titleImage = new ImageView();

        playButton.setOnAction(e -> appManager.startGame(true));
        newGameButton.setOnAction(e -> appManager.startGame(false));
        leaderboardButton.setOnAction(e -> appManager.showLeaderboard());
        exitButton.setOnAction(e -> Platform.exit());
        buttons.addAll(List.of(playButton, newGameButton, leaderboardButton, exitButton));

        DoubleBinding minDimension =  Bindings.createDoubleBinding(() ->
            Math.min(appManager.getWidth(), appManager.getHeight()),
            appManager.heightProperty(), appManager.widthProperty()
        );

        setAlignment(Pos.CENTER);
        getChildren().addAll(titleImage, buttonContainer);
        spacingProperty().bind(minDimension.divide(20));

        Image img = new Image(
                Objects.requireNonNull(
                        getClass().getResource("images/logo.png")
                ).toExternalForm()
        );
        titleImage.setImage(img);
        titleImage.setPreserveRatio(true);
        titleImage.fitWidthProperty().bind(minDimension.divide(2));

        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.spacingProperty().bind(minDimension.divide(40));
        for (Button btn : buttons) {
            btn.setOnMouseEntered(event -> btn.requestFocus());
            btn.styleProperty().bind(Bindings.createStringBinding(
                    () -> String.format(
                            "-fx-font-size: %.2fpx;",
                            Math.max(minDimension.get() / 30, 20)
                    ), minDimension
            ));
            btn.paddingProperty().bind(Bindings.createObjectBinding(
                    () -> {
                        double verticalPadding = minDimension.get() / 20;
                        double horizontalPadding = minDimension.get() / 100;
                        return new Insets(horizontalPadding, verticalPadding,
                                horizontalPadding, verticalPadding);
                    }, minDimension
            ));
            btn.prefWidthProperty().bind(widthProperty().multiply(0.8));
        }
    }

    /**
     * Visualizes the buttons.
     *
     * @param includeResume whether to include the resume game button
     */
    public void loadButtons(boolean includeResume) {
        buttonContainer.getChildren().clear();
        if (includeResume) {
            buttonContainer.getChildren().addAll(buttons);
        } else {
            buttonContainer.getChildren().addAll(buttons.subList(1, buttons.size()));
        }
    }

    /**
     * Disables all the buttons in the menu.
     */
    public void disableButtons() {
        buttons.forEach(btn -> btn.setDisable(true));
    }

    /**
     * Enables all the buttons in the menu.
     */
    public void enableButtons() {
        buttons.forEach(btn -> btn.setDisable(false));
    }

}

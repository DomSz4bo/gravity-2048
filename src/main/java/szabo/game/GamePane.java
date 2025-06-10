package szabo.game;

import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class GamePane extends BorderPane {


    public GamePane(AppManager handler) {
        super();

        var scoreLabel = new Label("Score: 0");
        scoreLabel.setStyle("-fx-font-size: 25");
        var scorePane = new VBox(scoreLabel);
        setTop(scorePane);

        var playgroundPane = new Pane();
        setCenter(playgroundPane);
        playgroundPane.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
//        setBackground(Background.fill(Color.web("rgba(0, 0, 0, 0.9)")));


    }
}

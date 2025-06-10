package szabo.game;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class GamePane extends BorderPane {


    public GamePane(AppManager handler) {
        super();

        var scoreLabel = new Label("Score: 0");
        scoreLabel.setStyle("-fx-font-size: 25");
        scoreLabel.setBackground(Background.fill(Color.BLUEVIOLET));
        scoreLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(scoreLabel, Priority.ALWAYS);


        var scorePane = new HBox(scoreLabel);
        scorePane.prefHeightProperty().bind(heightProperty().divide(16));
        scorePane.setBackground(Background.fill(Color.ORANGE));
        scorePane.setAlignment(Pos.CENTER);
        setTop(scorePane);

        var playgroundPane = new Pane();
        setCenter(playgroundPane);
        playgroundPane.setBackground(Background.fill(Color.RED));
        playgroundPane.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
//        setBackground(Background.fill(Color.web("rgba(0, 0, 0, 0.9)")));


    }
}

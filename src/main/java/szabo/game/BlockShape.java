package szabo.game;

import javafx.geometry.Point2D;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.HashMap;


public class BlockShape extends StackPane {

    private final Rectangle rectangle =  new Rectangle();
    private final Text text = new Text();
    private final int value;

    public BlockShape(int value) {
        super();
        if (Playground.getColor(value) == null) {
            throw new IllegalArgumentException("Value " + value + " is not a valid block value.");
        }
        rectangle.setFill(Playground.getColor(value));
        rectangle.setStroke(Color.BLACK);
        text.setFill(Color.BLACK);
        text.setText(Integer.toString(value));
        getChildren().addAll(rectangle, text);
        this.value = value;
    }

    public void setSize(double size) {
        double appliedSize = size * 0.97;   // to combat overlap of blocks
        rectangle.setWidth(appliedSize);
        rectangle.setHeight(appliedSize);
        rectangle.setStrokeWidth(size/100);
        text.setFont(Font.font("Verdana", FontWeight.BOLD, getFontSize(size)));
    }

    private double getFontSize(double blockSize) {
        if (value < 10_000) {
            return blockSize / 3;
        } else if (value < 100_000) {
            return blockSize / 4;
        } else {
            return blockSize / 5;
        }
    }

    public void setCenterPosition(double x, double y) {
        double xPos = x - rectangle.getWidth() / 2;
        double yPos = y - rectangle.getHeight() / 2;
        relocate(xPos, yPos);
    }

    public void setRotation(double radians) {
        double angle = Math.toDegrees(radians);
        rectangle.setRotate(angle);
    }

}

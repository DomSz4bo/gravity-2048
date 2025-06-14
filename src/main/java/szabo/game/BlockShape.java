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
    private final static String[] hexCodes = new String[] {
            "#FFE285", "#FFCD29", "#FFA333", "#ED6542", "#FC9FBE", "#A778FF",
            "#6D4DFF", "#61ABFF", "#1F87FF", "#00D483", "#20AD00", "#126100",
            "#8C2800", "#660000", "#870063", "#510061", "#510061"
    };
    private final static HashMap<Integer, Color> colors;
    static {
        colors = HashMap.newHashMap(hexCodes.length);
        int key = 1;
        for (String hexCode : hexCodes) {
            colors.put(key, Color.web(hexCode));
            key <<= 1;
        }
    }

    private int value;
    private final Rectangle rectangle =  new Rectangle();
    private final Text text = new Text();

    public BlockShape(int value) {
        super();
        if (!colors.containsKey(value)) {
            throw new IllegalArgumentException("Value " + value + " is not a valid block value.");
        }
        rectangle.setFill(colors.get(value));
        rectangle.setStroke(Color.BLACK);
        text.setFill(Color.BLACK);
        text.setText(Integer.toString(value));
        getChildren().addAll(rectangle, text);
        this.value = value;
    }

    public void setSize(double size) {
        rectangle.setWidth(size);
        rectangle.setHeight(size);
        rectangle.setStrokeWidth(size/100);
        text.setFont(Font.font("Verdana", FontWeight.BOLD, size / 3));
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

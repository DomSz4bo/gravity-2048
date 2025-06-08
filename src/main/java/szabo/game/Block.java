package szabo.game;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;


public class Block extends Rectangle {
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

    public Block(int width, int height, int value) {
        super(width, height);
        if (!colors.containsKey(value)) {
            throw new  IllegalArgumentException("Value " + value + " is not a valid block value.");
        }
        setFill(colors.get(value));
        setArcWidth(10);
        setArcHeight(10);
        this.value = value;
    }

    public void setPosition(double x, double y) {
        setX(x);
        setY(y);
    }
}

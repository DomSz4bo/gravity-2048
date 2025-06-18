package szabo.game;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;


/**
 * Provides the visual representation of a block in the playground.
 */
public class BlockShape extends StackPane {

    private final Rectangle rectangle = new Rectangle();
    private final Text text = new Text();
    private final int value;

    /**
     * Creates an instance of {@link BlockShape} with the given value and
     * color corresponding to this value.
     *
     * @param value the block's value
     */
    public BlockShape(int value) {
        if (Playground.getColor(value) == null) {
            throw new IllegalArgumentException("Value " + value + " is not a valid block value.");
        }
        Color color = Playground.getColor(value);
        rectangle.setFill(color);
        rectangle.setStroke((color.equals(Color.BLACK) ? Color.WHITE : Color.BLACK));
        text.setFill(getTextColor(color));
        text.setText(Integer.toString(value));
        getChildren().addAll(rectangle, text);
        this.value = value;
    }

    private Color getTextColor(Color color) {
        double limit = (double) 0xA0 / 0xFF;
        if (color.getRed() < limit && color.getGreen() < limit && color.getBlue() < limit) {
            return Color.WHITE;
        }
        return Color.BLACK;
    }

    /**
     * Sets the block's size to the given value.
     * @param size the desired size
     */
    public void setSize(double size) {
        double borderWidth = 0.02;
        double appliedSize = size * (1 - 0.02);
        rectangle.setWidth(appliedSize);
        rectangle.setHeight(appliedSize);
        rectangle.setStrokeWidth(size * borderWidth);
        text.setFont(Font.font("Verdana", FontWeight.BOLD, getFontSize(size)));
    }

    private double getFontSize(double blockSize) {
        if (value < 10_000) {
            return blockSize / 3.5;
        } else if (value < 100_000) {
            return blockSize / 4.5;
        } else {
            return blockSize / 5;
        }
    }

    /**
     * Positions the block so that it's center is located at the given point.
     * @param x the horizontal component
     * @param y the vertical component
     */
    public void setCenterPosition(double x, double y) {
        double blockX = x - rectangle.getWidth() / 2;
        double blockY = y - rectangle.getHeight() / 2;
        relocate(blockX, blockY);
    }

    /**
     * Sets the blocks rotation to the angle given in radians.
     * @param radians the angle to set
     */
    public void setRotation(double radians) {
        double angle = Math.toDegrees(radians);
        rectangle.setRotate(angle);
    }

}

package szabo.game;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the main visual component of the game's user interface where all gameplay occurs.
 * <p>
 * This class is responsible for visualizing all the blocks and the play area or playground itself.
 * The play area has a fixed aspect ratio and remains centered.
 * </p>
 */
public class Playground extends StackPane {
    private final static String[] hexCodes = new String[]{
            "#FFEBCC", "#F2B179", "#FFA333", "#FF6403", "#F23E3E", "#E572C8",
            "#8F6CE7", "#58B3F4", "#9DFFFD", "#48F2A0", "#38BD0F", "#931010",
            "#5B0658", "#3A005A", "#001375", "#094014", "#000000"
    };
    private final static HashMap<Integer, Color> colors;

    static {
        colors = HashMap.newHashMap(hexCodes.length);
        int key = 2;
        for (String hexCode : hexCodes) {
            colors.put(key, Color.web(hexCode));
            key <<= 1;
        }
    }

    /**
     * Returns the color for the given block value.
     *
     * @param blockValue the value to get the color for
     * @return assigned color
     */
    public static Color getColor(int blockValue) {
        return colors.get(blockValue);
    }

    private final Pane playgroundPane = new Pane();
    private final HashMap<Integer, BlockShape> blockMap = new HashMap<>();

    /**
     * Creates an instance of {@link Playground}.
     * <p>
     * Initializes and visualizes the unchanging parts of the play area.
     * </p>
     */
    public Playground() {
        getChildren().add(playgroundPane);
        playgroundPane.getStyleClass().add("playground-area");
        double wallThickness = GameHandler.WALL_THICKNESS;
        playgroundPane.setBorder(new Border(new BorderStroke(
                Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(
                        0, wallThickness, wallThickness, wallThickness,
                        false, true, true, true
                )
        )));
        playgroundPane.widthProperty().addListener(e -> resizeBlocks());
        playgroundPane.setFocusTraversable(true);
        paintDeathLine();
        fixAspectRatio();
    }

    private void fixAspectRatio() {
        playgroundPane.maxWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(getWidth(), getHeight() * GameHandler.PLAYGROUND_RATIO),
                widthProperty(), heightProperty()
        ));
        playgroundPane.maxHeightProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(getHeight(), getWidth() / GameHandler.PLAYGROUND_RATIO),
                heightProperty(), widthProperty()
        ));
    }

    private void paintDeathLine() {
        Line line = new Line(0, 20, playgroundPane.getWidth(), 20);
        line.setStroke(Color.WHITE);
        line.endXProperty().bind(playgroundPane.widthProperty());
        line.startYProperty().bind(playgroundPane.heightProperty()
                .multiply(GameHandler.LINE_POSITION));
        line.endYProperty().bind(playgroundPane.heightProperty()
                .multiply(GameHandler.LINE_POSITION));
        playgroundPane.getChildren().add(line);
    }

    private void resizeBlocks() {
        double blockSize = GameHandler.BLOCK_SIZE * playgroundPane.getWidth();
        blockMap.values().forEach(block -> block.setSize(blockSize));
    }

    /**
     * @return the Pane representing the play area
     */
    public Pane getPlaygroundPane() {
        return playgroundPane;
    }

    /**
     * Visualizes the given blocks within the play area.
     * @param blocks the blocks to visualize
     */
    public void paintBlocks(List<GameState.BlockState> blocks) {
        clearUnusedBlocks(blocks.stream().map(GameState.BlockState::id).collect(Collectors.toSet()));

        double blockSize = GameHandler.BLOCK_SIZE * playgroundPane.getWidth();
        for (GameState.BlockState blockState : blocks) {
            BlockShape visualBlock = blockMap.computeIfAbsent(
                    blockState.id(), id -> createAndAddBlock(blockState.value(), blockSize)
            );
            visualBlock.setCenterPosition(
                    blockState.posX() * playgroundPane.getWidth(),
                    playgroundPane.getHeight() * (1 - blockState.posY())
            );
            visualBlock.setRotation(-blockState.angleRadians());
        }
    }

    private void clearUnusedBlocks(Set<Integer> currentBlockIDs) {
        blockMap.keySet().removeIf(
                id -> {
                    if (!currentBlockIDs.contains(id)) {
                        BlockShape blockShape = blockMap.get(id);
                        playgroundPane.getChildren().remove(blockShape);
                        return true;
                    }
                    return false;
                }
        );
    }

    private BlockShape createAndAddBlock(int value, double size) {
        var block = new BlockShape(value);
        block.setSize(size);
        playgroundPane.getChildren().add(block);
        return block;
    }

    /**
     * Plays a {@link Confetti} effect at the given position and with
     * the Color corresponding to the block value.
     * <p>
     * Follows the JavaFX coordinate system so
     * position [0, 0] is the top left corner and [1, 1] is the lower right corner.
     * </p>
     *
     * @param blockValue the block value whose color is used
     * @param posX the horizontal position as a percentage of the playground's width
     * @param posY the vertical position as a percentage of the playground's height
     */
    public void runEffect(int blockValue, double posX, double posY) {
        int exp = 31 - Integer.numberOfLeadingZeros(blockValue);
        double mod = Math.min(1, exp / 15.0);
        double particleSize = playgroundPane.getWidth() / 100;
        double radius = playgroundPane.getWidth() / 60;
        double travel = playgroundPane.getWidth() / (1.7 + 3 * (1 - mod));
        double x = posX * playgroundPane.getWidth();
        double y = posY * playgroundPane.getHeight();
        int particleCount = 50 + (int) (200 * mod);
        Confetti confetti = new Confetti(
                particleSize, particleSize * 2, particleCount, playgroundPane,
                x, y, radius, travel, getColor(blockValue)
        );
        confetti.start();
    }

}

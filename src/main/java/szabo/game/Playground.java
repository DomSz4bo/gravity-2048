package szabo.game;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Playground extends StackPane {
    private final static String[] hexCodes = new String[] {
            "#FFE285", "#FFCD29", "#FFA333", "#ED6542", "#FC9FBE", "#A778FF",
            "#6D4DFF", "#61ABFF", "#1F87FF", "#00D483", "#20AD00", "#126100",
            "#8C2800", "#660000", "#870063", "#510061", "#510061", "#20488C"
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
    public static Color getColor(int key) {
        return colors.get(key);
    }

    private final Pane playgroundPane = new Pane();
    private final HashMap<Integer, BlockShape> blockMap = new HashMap<>();

    public Playground() {
        super();
        getChildren().add(playgroundPane);
        playgroundPane.setBackground(Background.fill(Color.web("rgba(0, 0, 0, 0.4)")));
        playgroundPane.setBorder(new Border(new BorderStroke(
                Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(
                        0, GameHandler.WALL_THICKNESS, GameHandler.WALL_THICKNESS, GameHandler.WALL_THICKNESS,
                        false, true, true, true
                )
        )));
        paintDeathLine();
        fixAspectRatio();
        testingEvents();
        playgroundPane.widthProperty().addListener(e -> resizeBlocks());
        playgroundPane.setFocusTraversable(true);
//        playgroundPane.setOnMouseClicked(event -> {
//            playgroundPane.requestFocus();
//            System.out.println("playgroundPane");
//        });
    }

    // TODO remove after testing
    private void testingEvents() {
        playgroundPane.heightProperty().addListener(
                e -> System.out.println(
                        "Width to Height: " + playgroundPane.getWidth() / playgroundPane.getHeight()
                        + "\nWidth=" + playgroundPane.getWidth() + " Height=" + playgroundPane.getHeight()
                )
        );
    }

    private void fixAspectRatio() {
        playgroundPane.maxWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(getWidth(), getHeight() * GameHandler.PLAYGROUND_RATIO),
                widthProperty(),  heightProperty()
        ));
        playgroundPane.maxHeightProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(getHeight(), getWidth() / GameHandler.PLAYGROUND_RATIO),
                heightProperty(),  widthProperty()
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
    // TODO resizing the Blocks
    private BlockShape createAndAddBlock(int value, double size) {
        var block = new BlockShape(value);
        block.setSize(size);
        playgroundPane.getChildren().add(block);
        return block;
    }

    public Pane getPlaygroundPane() {
        return playgroundPane;
    }

    public void paintBlocks(List<GameState.BlockState> blocks) {
        clearUnusedBlocks(blocks.stream().map(GameState.BlockState::id).collect(Collectors.toSet()));
        double blockSize = GameHandler.BLOCK_SIZE * playgroundPane.getWidth();
        for (GameState.BlockState blockState : blocks) {
            BlockShape visualBlock = blockMap.computeIfAbsent(
                    blockState.id(), id -> createAndAddBlock(blockState.value(), blockSize)
            );
            visualBlock.setCenterPosition(
                    blockState.position().getX() * playgroundPane.getWidth(),
                    playgroundPane.getHeight() * (1 - blockState.position().getY())
            );
            visualBlock.setRotation(-blockState.angleRadians());
        }
    }


    public void runEffect(int blockValue, double posX, double posY) {
        double particleSize = playgroundPane.getWidth() / 100;
        double radius = playgroundPane.getWidth() / 60;
        double travel = playgroundPane.getWidth() / 3;
        double x = posX * playgroundPane.getWidth();
        double y = posY * playgroundPane.getHeight();
        Confetti con = new Confetti(
                particleSize, particleSize * 2, 100, playgroundPane,
                x , y, radius, travel, getColor(blockValue)
        );
        con.runEffect();
    }

}

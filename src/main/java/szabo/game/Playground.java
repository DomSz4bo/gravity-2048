package szabo.game;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Playground extends StackPane {
    private static final double LINE_POSITION = 0.2;  // % of playground height

    private final Pane playgroundPane = new Pane();
    private boolean beingDragged = false;
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
        testingEvents();
    }

    // TODO remove after testing
    private void testingEvents() {
        playgroundPane.heightProperty().addListener(
                e -> System.out.println(
                        "Width to Height: " + playgroundPane.getWidth() / playgroundPane.getHeight()
                        + "\nWidth=" + playgroundPane.getWidth() + " Height=" + playgroundPane.getHeight()
                )
        );

        playgroundPane.maxWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(getWidth(), getHeight() * GameHandler.PLAYGROUND_RATIO),
                widthProperty(),  heightProperty()
        ));
        playgroundPane.maxHeightProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(getHeight(), getWidth() / GameHandler. PLAYGROUND_RATIO),
                heightProperty(),  widthProperty()
        ));

        var rec = new Rectangle(40, 40, 30, 30);
        rec.setFill(Color.GREEN);
        playgroundPane.getChildren().add(rec);

        playgroundPane.setOnMousePressed(e -> {
            beingDragged = rec.intersects(e.getX(), e.getY(), 1, 1);
            System.out.println(beingDragged);
        });
        playgroundPane.setOnMouseReleased(e -> beingDragged = false);
        playgroundPane.setOnMouseDragged(e -> {
            if (beingDragged) {
                rec.setX(Math.max(0, Math.min(e.getX(), playgroundPane.getWidth() - rec.getWidth())));
                rec.setY(Math.max(0, Math.min(e.getY(), playgroundPane.getHeight() - rec.getHeight())));
            }
        });
    }

    private void paintDeathLine() {
        Line line = new Line(0, 20, playgroundPane.getWidth(), 20);
        line.setStroke(Color.WHITE);
        line.endXProperty().bind(playgroundPane.widthProperty());
        line.startYProperty().bind(playgroundPane.heightProperty().multiply(LINE_POSITION));
        line.endYProperty().bind(playgroundPane.heightProperty().multiply(LINE_POSITION));
        playgroundPane.getChildren().add(line);
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

    private BlockShape createAndAddBlock(int value) {
        var block = new BlockShape(value);
        getChildren().add(block);
        return block;
    }

    public void paintBlocks(List<GameState.BlockState> blocks) {
        clearUnusedBlocks(blocks.stream().map(GameState.BlockState::id).collect(Collectors.toSet()));
        double blockSize = GameHandler.BLOCK_SIZE * playgroundPane.getWidth();
        for (GameState.BlockState block : blocks) {
            BlockShape visualBlock = blockMap.computeIfAbsent(
                    block.id(), id -> createAndAddBlock(block.id())
            );
            visualBlock.setCenterPosition(block.position());
            visualBlock.setWidth(blockSize);
            visualBlock.setHeight(blockSize);
        }
    }

}

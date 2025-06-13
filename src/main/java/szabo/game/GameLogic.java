package szabo.game;

import javafx.geometry.Point2D;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.world.World;

import java.util.Objects;
import java.util.stream.Stream;


// make coordinate space same as JavaFX coordinates ([0, 0] = TOP_LEFT_CORNER)

public class GameLogic {
    private static final double PHYSICS_HEIGHT = 1;  // meter height

    private final World<Body> world;
    private final double width, height;

    public GameLogic() {
        width = GameHandler.PLAYGROUND_RATIO * PHYSICS_HEIGHT;
        height = PHYSICS_HEIGHT;

        world = new World<>();
//        world.setBounds(new AxisAlignedBounds(width, height));
        createBlockContainer();
    }

    public void tick(long deltaTime) {
        world.update(deltaTime);
    }

    public void addBlock(int value, int positionX, int positionY) {
        var block = new BlockBody(value);
        // pull out into constants
        block.addFixture(Geometry.createSquare(GameHandler.BLOCK_SIZE * width), 1.0, 0.8, 0.35);
        block.translate(positionX * width, height * (1 - positionY));
        block.setMassType(MassType.NORMAL);
        block.setAngularDamping(0.5);   // also into constant
        world.addBody(block);
    }

    private void createBlockContainer() {
        double verticalWallThickness = GameHandler.WALL_THICKNESS * width;
        double horizontalWallThickness = GameHandler.WALL_THICKNESS * height;

        Body leftWall = new Body();
        leftWall.addFixture(Geometry.createRectangle(verticalWallThickness, height));  // experiment with friction, density etc.
        leftWall.setMassType(MassType.INFINITE);
        leftWall.translate(0 + verticalWallThickness / 2, height / 2);
//        leftWall.setUserData();

        Body rightWall = new Body();
        rightWall.addFixture(Geometry.createRectangle(verticalWallThickness, height));
        rightWall.setMassType(MassType.INFINITE);
        rightWall.translate(0 - verticalWallThickness / 2, height / 2);
//        rightWall.setUserData();

        Body floor = new Body();
        floor.addFixture(Geometry.createRectangle(width, horizontalWallThickness));
        floor.setMassType(MassType.INFINITE);
        floor.translate(width / 2, horizontalWallThickness / 2);
//        floor.setUserData();

        world.addBody(leftWall);
        world.addBody(rightWall);
        world.addBody(floor);
    }

    public GameState createGameState() {
        GameState gameState = new GameState();
        return gameState;
    }

    public void loadFromState(GameState gameState) {

    }

    private class BlockBody extends Body {
        private static int blockCount = 0;
        private final int number;
        private final int value;

        private BlockBody(int value) {
            super();
            number = blockCount++;
            this.value = value;
        }

        private Point2D getPercentagePosition() {
            return new Point2D(
                    getTransform().getTranslationX() / width,
                    getTransform().getTranslationY() / height
            );
        }

        private int getValue() {
            return value;
        }
        private int getID() {
            return number;
        }
    }
}

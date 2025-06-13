package szabo.game;

import javafx.geometry.Point2D;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

import java.util.UUID;


public class GameLogic {
    private static final double PHYSICS_HEIGHT = 1;  // meter height

    private final World<Body> world;
    private final double width = GameHandler.PLAYGROUND_RATIO * PHYSICS_HEIGHT;
    private final double height = PHYSICS_HEIGHT;
    private int score;

    public GameLogic() {
        score = 0;
        world = new World<>();
//        world.setBounds(new AxisAlignedBounds(width, height));
        createBlockContainer();
    }

    public void tick(long deltaTime) {
        world.update(deltaTime);
    }

    public void addBlock(int value, int positionX, int positionY) {
        var block = new BlockBody(value);
        block.translate(positionX * width, height * (1 - positionY));
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


    private class BlockBody extends Body {
        private static int blockCount = 0;
        private final int number;
        private final int value;

        private BlockBody(int value) {
            super();
            number = blockCount++;
            this.value = value;
            // config
            // TODO pull out into constants
            addFixture(Geometry.createSquare(GameHandler.BLOCK_SIZE * width), 1.0, 0.8, 0.35);
            setMassType(MassType.NORMAL);
            setAngularDamping(0.5);
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


    /* Maybe separate into a GameStateBuilder (physics -> state) and PhysicsLoader (state -> physics) class */
    public GameState createGameState() {
        return new GameState(
                world.getBodies().stream().filter(b -> b instanceof BlockBody)
                        .map(BlockBody.class::cast)
                        .map(GameLogic::createBlockState).toList(),
                null,
                score
        );
    }

    private static GameState.BlockState createBlockState(BlockBody block) {
        Vector2 linearVelocity = block.getLinearVelocity();
        return new GameState.BlockState(
                block.getPercentagePosition(),
                linearVelocity.x, linearVelocity.y,
                block.getAngularVelocity(),
                block.getValue(),
                block.getID()
        );
    }

    // TODO
    public static GameLogic loadFromGameState(GameState gameState) {
        return new GameLogic();
    }

}

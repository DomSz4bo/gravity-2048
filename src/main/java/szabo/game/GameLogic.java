package szabo.game;

import javafx.geometry.Point2D;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.Settings;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

import java.util.Random;


public class GameLogic {
    private static final double PHYSICS_HEIGHT = 5;         // height in meters - affects physics scale
    private static final long RELEASE_DELAY = 500;          // ms delay of next block generation
    public static final double BLOCK_START_HEIGHT = 0.9 * PHYSICS_HEIGHT;    // from top of Height

    private final World<Body> world;
    private final double width = GameHandler.PLAYGROUND_RATIO * PHYSICS_HEIGHT;
    private final double height = PHYSICS_HEIGHT;

    private int score;
    private int maxBlockValueExponent = 1;
    private BlockBody blockToPosition;
    private long releaseTime = System.currentTimeMillis();

    public GameLogic() {
        world = new World<>();
        score = 0;
        setNewBlockToPosition();
        System.out.println("World width: " + this.width + "   height: " + this.height);
        System.out.println("Gravity: " + world.getGravity());
        createBlockContainer();
    }

    public void update(double elapsedTime) {
        world.update(elapsedTime);
        if (blockToPosition == null) {
            long now = System.currentTimeMillis();
            long deltaTime = now - releaseTime;
            if (deltaTime > RELEASE_DELAY) {
                setNewBlockToPosition();
            }
        }
//        printBlocks();
    }

    public void addBlock(int value, double posX, double posY) {
        BlockBody block = new BlockBody(value);
        block.translate(posX * width, posY * height);
        world.addBody(block);
    }

    private void createBlockContainer() {
        double verticalWallThickness = GameHandler.WALL_THICKNESS * width;
        double horizontalWallThickness = GameHandler.WALL_THICKNESS * height;

        Body leftWall = new Body();
        leftWall.addFixture(Geometry.createRectangle(verticalWallThickness, height));  // experiment with friction, density etc.
        leftWall.setMassType(MassType.INFINITE);
        leftWall.translate(verticalWallThickness, height / 2);

        Body rightWall = new Body();
        rightWall.addFixture(Geometry.createRectangle(verticalWallThickness, height));
        rightWall.setMassType(MassType.INFINITE);
        rightWall.translate(width - verticalWallThickness, height / 2);

        Body floor = new Body();
        floor.addFixture(Geometry.createRectangle(width, horizontalWallThickness));
        floor.setMassType(MassType.INFINITE);
        floor.translate(width / 2, horizontalWallThickness);

        world.addBody(leftWall);
        world.addBody(rightWall);
        world.addBody(floor);
    }

    // TODO add block positioning and generating
    private BlockBody generateBlock() {
        return new BlockBody(generateBlockValue());
    }

    private final Random random = new Random();
    private int generateBlockValue() {
        int exp = random.nextInt(0, maxBlockValueExponent) + 1;
        return (1 << exp);
    }

    private void setNewBlockToPosition() {
        blockToPosition = generateBlock();
        blockToPosition.translate(width / 2, BLOCK_START_HEIGHT);
    }

    // TODO add collision and merging logic






    private class BlockBody extends Body {
        private static int blockCount = 0;
        private final int IDNumber = blockCount++;
        private final int value;
        private BlockBody(int value) {
            super();
            this.value = value;
            // config
            // TODO pull out into constants
            addFixture(Geometry.createSquare(GameHandler.BLOCK_SIZE * width), 1.0, 0.8, 0.3);
            setMass(MassType.NORMAL);
            setAngularDamping(2);
            setLinearDamping(1);
        }

        private Point2D getPercentagePosition() {
            return new Point2D(
                    getTransform().getTranslationX() / width,
                    getTransform().getTranslationY() / height
            );
        }

        private int getID() {
            return IDNumber;
        }
        private int getValue() {
            return value;
        }
    }



    // TODO remove - used only for debugging
    private void printBlocks() {
        for (Body b : world.getBodies()) {
            if (b instanceof BlockBody) {
                System.out.println("---\n");
                System.out.println(b.getTransform().getTranslation());
                System.out.println(b.getLinearVelocity());
                System.out.println(b.getAngularDamping());
                System.out.println("---\n");
            }
        }
    }



    /* Maybe separate into a GameStateBuilder (physics -> state) and PhysicsLoader (state -> physics) class */
    // TODO active block logic
    public GameState createGameState() {
        return new GameState(
                world.getBodies().stream().filter(b -> b instanceof BlockBody)
                        .map(BlockBody.class::cast)
                        .map(GameLogic::createBlockState).toList(),
                createBlockState(blockToPosition),
                score
        );
    }

    private static GameState.BlockState createBlockState(BlockBody block) {
        if (block == null) return null;
        Vector2 linearVelocity = block.getLinearVelocity();
        return new GameState.BlockState(
                block.getPercentagePosition(),
                block.getTransform().getRotationAngle(),
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

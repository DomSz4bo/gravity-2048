package szabo.game;

import javafx.geometry.Point2D;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;


public class GameLogic {
    private static final double PHYSICS_HEIGHT = 5;  // meter height

    private final World<Body> world;
    private final double width = GameHandler.PLAYGROUND_RATIO * PHYSICS_HEIGHT;
    private final double height = PHYSICS_HEIGHT;
    private int score;

    public GameLogic() {
        System.out.println("World width: " + this.width + "   height: " + this.height);
        score = 0;
        world = new World<>();
        System.out.println("Gravity: " + world.getGravity());
//        world.setBounds(new AxisAlignedBounds(width, height));
        createBlockContainer();
    }

    public boolean update(double elapsedTime) {
        world.update(elapsedTime);
//        printBlocks();
        return true;
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
//        leftWall.setUserData();

        Body rightWall = new Body();
        rightWall.addFixture(Geometry.createRectangle(verticalWallThickness, height));
        rightWall.setMassType(MassType.INFINITE);
        rightWall.translate(width - verticalWallThickness, height / 2);
//        rightWall.setUserData();

        Body floor = new Body();
        floor.addFixture(Geometry.createRectangle(width, horizontalWallThickness));
        floor.setMassType(MassType.INFINITE);
        floor.translate(width / 2, horizontalWallThickness);
//        floor.setUserData();

        System.out.println("floor: " + floor.getTransform().getTranslation());

        world.addBody(leftWall);
        world.addBody(rightWall);
        world.addBody(floor);
    }

    private class BlockBody extends Body {
        private static int blockCount = 0;
        private final int IDNumber = blockCount++;
        private final int value;
        private BlockBody(int value) {
            super();
            this.value = value;
            // config
            // TODO pull out into constants
            addFixture(Geometry.createSquare(GameHandler.BLOCK_SIZE * width), 1.0, 0.8, 0.35);
            setMass(MassType.NORMAL);
            setAngularDamping(0.7);
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
                null,
                score
        );
    }

    private static GameState.BlockState createBlockState(BlockBody block) {
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

    public static void main(String[] args) {
        var ok = new GameLogic();
        ok.addBlock(2, 0.5, 0.5);
        for (int i = 0; i < 10; i++) {
            ok.printBlocks();
            ok.update(0);
        }

    }

}

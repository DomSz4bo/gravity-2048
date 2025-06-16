package szabo.game;

import javafx.geometry.Point2D;
import org.dyn4j.collision.manifold.ManifoldPoint;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.BroadphaseCollisionData;
import org.dyn4j.world.ManifoldCollisionData;
import org.dyn4j.world.NarrowphaseCollisionData;
import org.dyn4j.world.World;
import org.dyn4j.world.listener.CollisionListener;

import java.util.*;


public class GameLogic {
    private static final double PHYSICS_HEIGHT = 5;         // height in meters - affects physics scale
    private static final long RELEASE_DELAY = 500;          // ms delay of next block generation
    private static final double BLOCK_START_HEIGHT = 0.9 * PHYSICS_HEIGHT;
    private static final int MAX_BLOCK_VALUE = 131072;

    private final World<Body> world;
    private final double width = GameHandler.PLAYGROUND_RATIO * PHYSICS_HEIGHT;
    private final double height = PHYSICS_HEIGHT;
    private final double blockSize = GameHandler.BLOCK_SIZE * width;

    private int score;
    private int highestBlockValue = 2;      // TODO maybe make it a property so a notification can be added
    private BlockBody blockToPosition;          // TODO maybe rename
    private long releaseTime = System.currentTimeMillis();
    private boolean beingDragged = false;

    private record CollisionRecord(
            BlockBody blockA, BlockBody blockB, Vector2 contactPoint
    ) implements Comparable<CollisionRecord> {
        @Override
        public int compareTo(CollisionRecord other) {
            return Double.compare(
                    getBlockCenterDistanceSquared(),
                    other.getBlockCenterDistanceSquared()
            );
        }
        private double getBlockCenterDistanceSquared() {
            Vector2 centerA = blockA.getTransform().getTranslation();
            Vector2 centerB = blockB.getTransform().getTranslation();
            double dx = centerA.x - centerB.x;
            double dy = centerA.y - centerB.y;
            return dx * dx + dy * dy;
        }
    }
//    private final Queue<CollisionRecord> mergeQueue = new ArrayDeque<>();
    private final PriorityQueue<CollisionRecord> mergeQueue = new PriorityQueue<>();

    public GameLogic() {
        world = new World<>();
        score = 0;
        setNewBlockToPosition();
        System.out.println("World width: " + this.width + "   height: " + this.height);
        System.out.println("Gravity: " + world.getGravity());
        createBlockContainer();

        CollisionListener<Body, BodyFixture>  collisionListener = new CollisionListener<>() {
            @Override
            public boolean collision(BroadphaseCollisionData<Body, BodyFixture> collision) {
                return true;
            }
            @Override
            public boolean collision(NarrowphaseCollisionData<Body, BodyFixture> collision) {
                return true;
            }
            @Override
            public boolean collision(ManifoldCollisionData<Body, BodyFixture> collision) {
                Body body1 = collision.getBody1();
                Body body2 = collision.getBody2();
                if (body1 instanceof BlockBody block1 && body2 instanceof BlockBody block2
                    && block1.getValue() == block2.getValue() && block1.getValue() < MAX_BLOCK_VALUE) {
                    double contactX = 0;
                    double contactY = 0;
                    for (ManifoldPoint p : collision.getManifold().getPoints()) {
                        contactX += p.getPoint().x;
                        contactY += p.getPoint().y;
                    }
                    int numberOfPoints = collision.getManifold().getPoints().size();
                    contactX /= numberOfPoints;
                    contactY /= numberOfPoints;
                    var collisionInfo = new CollisionRecord(
                            block1, block2, new Vector2(contactX, contactY)
                    );
                    System.out.println("collisionInfo: " + block1.getID() + " " + block2.getID() + " at:" + frameNumber);
                    mergeQueue.add(collisionInfo);
//                    return false;
                }
                return true;
            }

        };
        world.addCollisionListener(collisionListener);
    }

    private long frameNumber = 0;
    public void update(double elapsedTime) {
        frameNumber++;
        world.update(elapsedTime);
        if (!mergeQueue.isEmpty()) {
            mergeQueuedCollisions();
        }

//        System.out.println("Elapsed time: " + elapsedTime);
        if (blockToPosition == null) {
            long now = System.currentTimeMillis();
            long deltaTime = now - releaseTime;
            if (deltaTime > RELEASE_DELAY) {
                setNewBlockToPosition();
            }
        }
//        printBlocks();
    }

    private void addBlock(int value, double posX, double posY) {
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

    private BlockBody generateBlock() {
//        return new BlockBody(generateBlockValue());
        return new BlockBody(2);
    }

    private final Random random = new Random();
    private int generateBlockValue() {
//        int exp = random.nextInt(integerLog2(highestBlockValue)) + 1;
//        return (1 << exp);
        return random.nextInt(10) < 7 ? 2 : 4;
    }

    private void setNewBlockToPosition() {
        blockToPosition = generateBlock();
        blockToPosition.translate(width / 2, BLOCK_START_HEIGHT);
    }


    // MOUSE CONTROL
    private double dragOffsetX;
    public void handleMouseB1Pressed(double posX, double posY) {
        if (blockToPosition == null) return;
        double worldX = posX * width;
        double worldY = posY * height;
        if (blockToPosition.contains(new Vector2(worldX, worldY))) {
            System.out.println("===========HIT=============: " + worldX + ", " + worldY);
            beingDragged = true;
            dragOffsetX = worldX - blockToPosition.getTransform().getTranslationX();
        }
    }
    public void handleMouseB1Released() {
        if (beingDragged) {
            releasePositionedBlock();
            beingDragged = false;
        }

    }
    public void handleMouseB1Dragged(double posX) {
        if (beingDragged) {
            double worldX = posX * width - dragOffsetX;
            blockToPosition.getTransform().setTranslation(
                    getWallConstrainedX(worldX),
                    BLOCK_START_HEIGHT
            );
        }
    }

    // KEYBOARD CONTROL
    public void moveBlock(boolean right) {
        if (!beingDragged && blockToPosition != null) {
            double blockX = blockToPosition.getTransform().getTranslationX();
            double movementDistance = width / 100;
            double movedX = blockX + (right ? movementDistance : -movementDistance);
            blockToPosition.getTransform().setTranslation(
                    getWallConstrainedX(movedX),
                    BLOCK_START_HEIGHT
            );
        }
    }
    public void keyboardReleaseBlock() {
        if (!beingDragged && blockToPosition != null) {
            releasePositionedBlock();
        }
    }

    private double getWallConstrainedX(double worldX) {
        double wallOffset = GameHandler.WALL_THICKNESS * width + blockSize / 2;
        return Math.min(width - wallOffset , Math.max(wallOffset, worldX));
    }


    private void releasePositionedBlock() {
        world.addBody(blockToPosition);
        releaseTime = System.currentTimeMillis();
        blockToPosition = null;
    }


    //  BLOCK MERGING
    @FunctionalInterface
    public interface MergeListener {
        void onMerged(int newValue, double posX, double posY);
    }

    private final List<MergeListener> mergeListeners = new ArrayList<>();

    public void setOnMerged(MergeListener listener) {
        mergeListeners.add(listener);
    }

    /**
     * Dequeues and merges every collision enqueued in <code>mergeQueue</code>.
     */
    private void mergeQueuedCollisions() {
        Set<BlockBody> mergedBlocks = new HashSet<>();
        while (!mergeQueue.isEmpty()) {
            CollisionRecord collision = mergeQueue.poll();
            if (mergedBlocks.contains(collision.blockA()) || mergedBlocks.contains(collision.blockB())) {
                continue;
            }
            BlockBody mergedBlock = mergeCollidingBlocks2(collision);
            mergedBlocks.add(collision.blockA());
            mergedBlocks.add(collision.blockB());
            System.out.println("Collision A=" + collision.blockA().getValue() + " B= " + collision.blockB().getValue()
            + " Created: " + mergedBlock.getValue());
            world.removeBody(collision.blockA());
            world.removeBody(collision.blockB());
            world.addBody(mergedBlock);
            mergeListeners.forEach(mergeListener -> mergeListener.onMerged(
                    mergedBlock.getValue(),
                    collision.contactPoint().x / width,
                    collision.contactPoint().y / height
            ));
            score += mergedBlock.getValue();
        }
    }

    /**
     * Merges colliding blocks and creates a new block with value equal to sum of block values
     * and velocity equal to the average velocities of the blocks.
     * The new block is positioned to the collision contact point.
     * @param collision the collision information
     * @return block created by merging
     */
    private BlockBody mergeCollidingBlocks(CollisionRecord collision) {
        BlockBody blockA = collision.blockA();
        BlockBody blockB = collision.blockB();

        int newValue = blockA.getValue() + blockB.getValue();
        Vector2 linearVel = new Vector2();
        linearVel.add(blockA.getLinearVelocity()).add(blockB.getLinearVelocity()).divide(2);
        double angularVel = (blockA.getAngularVelocity() + blockB.getAngularVelocity()) / 2;

        BlockBody mergedBlock = new BlockBody(newValue);
        mergedBlock.setLinearVelocity(linearVel);
        mergedBlock.setAngularVelocity(angularVel);
        mergedBlock.translate(collision.contactPoint());

        highestBlockValue = Math.max(highestBlockValue, newValue);
        return mergedBlock;
    }

    private BlockBody mergeCollidingBlocks2(CollisionRecord collision) {
        BlockBody followedBlock = getHigherVelocityBlock(collision.blockA(),  collision.blockB());
        BlockBody absorbedBlock = (followedBlock == collision.blockA()) ? collision.blockB() : collision.blockA();

        int newValue = followedBlock.getValue() + absorbedBlock.getValue();

        BlockBody mergedBlock = new BlockBody(newValue);
        mergedBlock.setLinearVelocity(followedBlock.getLinearVelocity().copy());
        mergedBlock.setAngularVelocity(followedBlock.getAngularVelocity());
        mergedBlock.translate(followedBlock.getTransform().getTranslation().copy());

        highestBlockValue = Math.max(highestBlockValue, newValue);
        return mergedBlock;
    }

    private BlockBody getHigherVelocityBlock(BlockBody block1, BlockBody block2) {
        double velocityMagnitude1 = block1.getLinearVelocity().getMagnitude();
        double velocityMagnitude2 = block2.getLinearVelocity().getMagnitude();
        return (velocityMagnitude1 > velocityMagnitude2) ? block1 : block2;
    }


    private int integerLog2(int value) {
        if (Integer.bitCount(value) != 1)
            return -1;
        int power = 0;
        while ((value >>>= 1) != 0) {
            power++;
        }
        return power;
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
            addFixture(Geometry.createSquare(blockSize), 1.0, 0.8, 0.3);
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

        @Override
        public int hashCode() {
            return Objects.hash(IDNumber, value);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            BlockBody blockBody = (BlockBody) o;
            return IDNumber == blockBody.IDNumber && value == blockBody.value;
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

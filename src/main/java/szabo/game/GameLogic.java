package szabo.game;

import org.dyn4j.collision.manifold.ManifoldPoint;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.BroadphaseCollisionData;
import org.dyn4j.world.ManifoldCollisionData;
import org.dyn4j.world.NarrowphaseCollisionData;
import org.dyn4j.world.World;
import org.dyn4j.world.listener.CollisionListener;

import java.util.*;


public class GameLogic {
    private static final double PHYSICS_SCALE = 3;            // height in meters - affects physics scale
    private static final long NEW_BLOCK_DELAY = 750;          // ms delay of next block generation
    private static final int MAX_BLOCK_VALUE = 131072;

    // BLOCK BEHAVIOUR
    public static final double FRICTION = 0.8;
    private static final double RESTITUTION = 0.3;
    private static final double ANGULAR_DAMPING = 2.0;
    private static final double LINEAR_DAMPING = 1.0;
    public static final double DENSITY = 1.0;

    private final World<Body> world;
    private final double width = GameHandler.PLAYGROUND_RATIO * PHYSICS_SCALE;
    private final double height = PHYSICS_SCALE;
    private final double blockSize = GameHandler.BLOCK_SIZE * width;

    private int score;
    private int highestBlockValue = 2;          // TODO not used - maybe remove
    private BlockBody blockToPosition;          // TODO maybe RENAME
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
    private final Runnable onGameOver;

    public GameLogic(Runnable onGameOver) {
        world = new World<>();
        score = 0;
        this.onGameOver = onGameOver;
        initializeNewBlockToPosition();
        createBlockContainer();

        CollisionListener<Body, BodyFixture> collisionListener = new CollisionListener<>() {
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
                        && block1.getValue() == block2.getValue()
                        && block1.getValue() < MAX_BLOCK_VALUE) {
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
                    mergeQueue.add(collisionInfo);
                }
                return true;
            }

        };
        world.addCollisionListener(collisionListener);
    }

    public void update(double elapsedTime) {
        world.update(elapsedTime);
        if (!mergeQueue.isEmpty()) {
            mergeQueuedCollisions();
        }
        if (blockToPosition == null) {
            long now = System.currentTimeMillis();
            long deltaTime = now - releaseTime;
            if (deltaTime > NEW_BLOCK_DELAY) {
                initializeNewBlockToPosition();
            }
        }
//        printBlocks();
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
        return new BlockBody(generateBlockValue());
    }

    private final Random random = new Random();

    private int generateBlockValue() {
//        int exp = random.nextInt(integerLog2(highestBlockValue)) + 1;
//        return (1 << exp);
        return random.nextInt(10) < 7 ? 2 : 4;
    }

    private int integerLog2(int value) {
        if (value <= 0) {
            return -1;
        }
        return 31 - Integer.numberOfLeadingZeros(value);
    }

    private void initializeNewBlockToPosition() {
        blockToPosition = generateBlock();
        double blockY = height * (1 - GameHandler.LINE_POSITION / 2);
        blockToPosition.translate(width / 2, blockY);
    }


    // MOUSE CONTROL
    private double dragOffsetX;

    public void handleMouseB1Pressed(double posX, double posY) {
        if (blockToPosition == null) return;
        double worldX = posX * width;
        double worldY = posY * height;
        if (blockToPosition.contains(new Vector2(worldX, worldY))) {
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
            double y = blockToPosition.getTransform().getTranslationY();
            blockToPosition.getTransform().setTranslation(getWallConstrainedX(worldX), y);
        }
    }

    // KEYBOARD CONTROL
    public void moveBlock(boolean right) {
        if (!beingDragged && blockToPosition != null) {
            double blockX = blockToPosition.getTransform().getTranslationX();
            double movementDistance = width / 100;
            double movedX = blockX + (right ? movementDistance : -movementDistance);
            double y = blockToPosition.getTransform().getTranslationY();
            blockToPosition.getTransform().setTranslation(getWallConstrainedX(movedX), y);
        }
    }

    public void keyboardReleaseBlock() {
        if (!beingDragged && blockToPosition != null) {
            releasePositionedBlock();
        }
    }

    private double getWallConstrainedX(double worldX) {
        double wallOffset = GameHandler.WALL_THICKNESS * width + blockSize / 2;
        return Math.min(width - wallOffset, Math.max(wallOffset, worldX));
    }


    private void releasePositionedBlock() {
        if (gameOver()) {
            onGameOver.run();
        } else {
            world.addBody(blockToPosition);
            releaseTime = System.currentTimeMillis();
            blockToPosition = null;
        }
    }

    private boolean gameOver() {
        double lineY = (1 - GameHandler.LINE_POSITION) * height;
        return world.getBodies().stream()
                .filter(body -> body instanceof BlockBody)
                .anyMatch(block -> block.createAABB().getMaxY() >= lineY);
    }


    //  BLOCK MERGING
    @FunctionalInterface
    public interface MergeListener {
        void onMerged(int newBlockValue, double posX, double posY);
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
            if (mergedBlocks.contains(collision.blockA())
                    || mergedBlocks.contains(collision.blockB())) {
                continue;
            }
            BlockBody mergedBlock = mergeCollidingBlocks(collision);
            mergedBlocks.add(collision.blockA());
            mergedBlocks.add(collision.blockB());
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
     *
     * @param collision the collision information
     * @return block created by merging
     */
    private BlockBody mergeCollidingBlocks(CollisionRecord collision) {
        BlockBody followedBlock = getHigherVelocityBlock(collision.blockA(), collision.blockB());
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

    private class BlockBody extends Body {
        private static int blockCount = 0;
        private final int IDNumber = blockCount++;
        private final int value;

        private BlockBody(int value) {
            this.value = value;
            addFixture(Geometry.createSquare(blockSize), DENSITY, FRICTION, RESTITUTION);
            setAngularDamping(ANGULAR_DAMPING);
            setLinearDamping(LINEAR_DAMPING);
            setMass(MassType.NORMAL);
        }

        private double getPosX() {
            return getTransform().getTranslationX() / width;
        }

        private double getPosY() {
            return getTransform().getTranslationY() / height;
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
                block.getPosX(), block.getPosY(),
                block.getTransform().getRotationAngle(),
                linearVelocity.x, linearVelocity.y,
                block.getAngularVelocity(),
                block.getValue(),
                block.getID()
        );
    }

    private void clearWorldBlocks() {
        world.getBodies().stream().filter(b -> b instanceof BlockBody)
                .toList().forEach(world::removeBody);
    }

    private BlockBody createBlockFromState(GameState.BlockState blockState) {
        BlockBody block = new BlockBody(blockState.value());
        block.translate(blockState.posX() * width, blockState.posY() * height);
        block.getTransform().setRotation(blockState.angleRadians());
        block.setLinearVelocity(blockState.velocityX(), blockState.velocityY());
        block.setAngularVelocity(blockState.angularVelocity());
        return block;
    }

    public void loadGameState(GameState gameState) {
        clearWorldBlocks();
        gameState.releasedBlocks().forEach(
                block -> world.addBody(createBlockFromState(block))
        );
        if (gameState.activeBlock() == null) {
            releaseTime = System.currentTimeMillis();
        } else {
            blockToPosition = createBlockFromState(gameState.activeBlock());
        }
        score = gameState.score();
    }

    public void resetLogic() {
        clearWorldBlocks();
        blockToPosition = null;
        score = 0;
    }


}

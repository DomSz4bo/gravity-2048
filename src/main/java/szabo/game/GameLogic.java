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


/**
 * Handles the game's logic. This includes new block generation and positioning,
 * simulating the physics of block movement and collisions, merging colliding blocks with the same value,
 * checking for game over.
 */
public class GameLogic {
    /**
     * Defines what block generation methods to use.
     * If {@code true} only 2 and 4 blocks generate with 90% and 10% probability respectively.
     * Else blocks with values lower (equal if highest is 2)  than the highest value among
     * existing block values are generated with equal probability.
     */
    private static final boolean CLASSIC_GENERATION = false;

    // PHYSICS BEHAVIOUR
    /**
     * Defines the height of the playground in meters.
     */
    private static final double PHYSICS_SCALE = 3;
    private static final double BLOCK_DENSITY = 1.0;
    private static final double BLOCK_FRICTION = 0.8;
    private static final double BLOCK_RESTITUTION = 0.3;
    private static final double ANGULAR_DAMPING = 2.0;
    private static final double LINEAR_DAMPING = 1.0;

    private static final long NEW_BLOCK_DELAY = 750;
    private static final int MAX_BLOCK_VALUE = 131072;

    private final World<Body> world;
    private final double width = GameHandler.PLAYGROUND_RATIO * PHYSICS_SCALE;
    private final double height = PHYSICS_SCALE;
    private final double blockSize = GameHandler.BLOCK_SIZE * width;

    private int score;
    private int highestBlockValue = 2;
    private BlockBody blockToPosition;
    private long releaseTime = System.currentTimeMillis();
    private boolean beingDragged = false;

    /**
     * Provides a record of a collision between two blocks.
     *
     * @param blockA       block involved
     * @param blockB       other block involved
     * @param contactPoint the point of contact between the blocks
     */
    private record CollisionRecord(
            BlockBody blockA, BlockBody blockB, Vector2 contactPoint
    ) implements Comparable<CollisionRecord> {
        /**
         * Compares collision records based on the distance between the centers of the involved blocks.
         *
         * @param other the collision record to be compared.
         * @return the value {@code 0} if the center distances are equal;
         * a value less than {@code 0} if the center distance of this collision is lower;
         * and a value greater than {@code 0} if the center distance of this collision is greater
         * than of {@code other}.
         */
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

    private final PriorityQueue<CollisionRecord> mergeQueue = new PriorityQueue<>();
    private final Runnable onGameOver;

    /**
     * Creates an instance of {@link GameLogic}.
     * <p>
     * Initializes game logic, the physics world used for simulating
     * the physics of the game and sets up merging.
     * </p>
     *
     * @param onGameOver the action to perform when the game is over
     */
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

            /**
             * Adds collisions between blocks with the same value to a queue for further processing
             * during merging handling.
             *
             * @param collision the manifold collision data
             * @return {@code true} - lets the world further process the collision
             */
            @Override
            public boolean collision(ManifoldCollisionData<Body, BodyFixture> collision) {
                Body body1 = collision.getBody1();
                Body body2 = collision.getBody2();
                if (body1 instanceof BlockBody block1 && body2 instanceof BlockBody block2
                        && block1.getValue() == block2.getValue()
                        && block1.getValue() < MAX_BLOCK_VALUE) {
                    Vector2 contactPoint = averagePoint(collision.getManifold().getPoints());
                    var collisionInfo = new CollisionRecord(
                            block1, block2, contactPoint
                    );
                    mergeQueue.add(collisionInfo);
                }
                return true;
            }

            private Vector2 averagePoint(List<ManifoldPoint> points) {
                double averageX = 0;
                double averageY = 0;
                for (ManifoldPoint p : points) {
                    averageX += p.getPoint().x;
                    averageY += p.getPoint().y;
                }
                averageX /= points.size();
                averageY /= points.size();
                return new Vector2(averageX, averageY);
            }

        };
        world.addCollisionListener(collisionListener);
    }

    /**
     * Updates the physics world and logic of the game.
     *
     * @param elapsedTime the elapsed time in seconds
     */
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
    }

    private void createBlockContainer() {
        double verticalWallThickness = GameHandler.WALL_THICKNESS * width;
        double horizontalWallThickness = GameHandler.WALL_THICKNESS * height;

        Body leftWall = new Body();
        leftWall.addFixture(Geometry.createRectangle(verticalWallThickness, height));
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
        if (CLASSIC_GENERATION) {
            return random.nextInt(10) < 9 ? 2 : 4;
        } else {
            if (highestBlockValue == 2) return 2;
            int exp = random.nextInt(1, integerLog2(highestBlockValue));
            return 1 << exp;
        }
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

    /**
     * Handles input from the left mouse button (B1). If the mouse press occurred on
     * the new generated block, dragging of this block is initiated.
     * <p>
     * The position [0, 0] is the bottom left corner and [1, 1] is the upper right corner.
     * </p>
     *
     * @param posX horizontal position of the mouse press as a percentage of playground width
     * @param posY vertical position of the mouse press as a percentage of playground height
     */
    public void handleMouseB1Pressed(double posX, double posY) {
        if (blockToPosition == null) return;
        double worldX = posX * width;
        double worldY = posY * height;
        if (blockToPosition.contains(new Vector2(worldX, worldY))) {
            beingDragged = true;
            dragOffsetX = worldX - blockToPosition.getTransform().getTranslationX();
        }
    }

    /**
     * Handles input from the left mouse button (B1) being released.
     * If the new generated block was being dragged, it is released and starts falling.
     */
    public void handleMouseB1Released() {
        if (beingDragged) {
            releasePositionedBlock();
            beingDragged = false;
        }

    }

    /**
     * Handles input from the left mouse button (B1) being dragged.
     * If dragging was initiated, the new generated block is repositioned.
     * <p>
     * The position [0, 0] is the bottom left corner and [1, 1] is the upper right corner.
     * </p>
     *
     * @param posX horizontal position of the mouse as a percentage of playground width
     */
    public void handleMouseB1Dragged(double posX) {
        if (beingDragged) {
            double worldX = posX * width - dragOffsetX;
            double y = blockToPosition.getTransform().getTranslationY();
            blockToPosition.getTransform().setTranslation(getWallConstrainedX(worldX), y);
        }
    }

    // KEYBOARD CONTROL
    /**
     * Moves new generated block in the provided direction and amount.
     *
     * @param right  if {@code true} block moves right, else moves left
     * @param amount distance to move as a percentage of playground width
     */
    public void moveBlock(boolean right, double amount) {
        if (!beingDragged && blockToPosition != null) {
            double blockX = blockToPosition.getTransform().getTranslationX();
            double movementDistance = width * amount;
            double movedX = blockX + (right ? movementDistance : -movementDistance);
            double y = blockToPosition.getTransform().getTranslationY();
            blockToPosition.getTransform().setTranslation(getWallConstrainedX(movedX), y);
        }
    }

    /**
     * Releases the new generated block if it exists.
     */
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
    /**
     * Interface for merge listeners.
     */
    @FunctionalInterface
    public interface MergeListener {
        /**
         * The operation to run.
         *
         * @param newBlockValue the value of the block created by merging
         * @param posX          horizontal position of the merge as a percentage of playground width
         * @param posY          vertical position of the merge as a percentage of playground height
         */
        void onMerged(int newBlockValue, double posX, double posY);
    }

    private final List<MergeListener> mergeListeners = new ArrayList<>();

    /**
     * Adds merge listener to the existing listeners. Merge listeners execute
     * when two blocks with the same value merge into a new block with double the value.
     *
     * @param listener the action to perform on merge
     */
    public void addMergeListener(MergeListener listener) {
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
     * Merges colliding blocks and creates a new block with value equal to sum of the colliding blocks
     * values. The new block inherits the physical attributes (position, velocity, etc.) of the block
     * with the greater linear velocity.
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

    /**
     * This class represents the blocks in the logic and physics world of the game.
     */
    private class BlockBody extends Body {
        private static int blockCount = 0;
        private final int IDNumber = blockCount++;
        private final int value;

        private BlockBody(int value) {
            this.value = value;
            addFixture(
                    Geometry.createSquare(blockSize),
                    BLOCK_DENSITY,
                    BLOCK_FRICTION,
                    BLOCK_RESTITUTION
            );
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

        /**
         * Two blocks equal if they have the same ID and value.
         *
         * @param o other object
         * @return {@code true} if {@code o} is a {@link BlockBody} equal to this block.
         */
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            BlockBody blockBody = (BlockBody) o;
            return IDNumber == blockBody.IDNumber && value == blockBody.value;
        }
    }

    /**
     * Captures and returns the current state of the game logic and physics
     * in an instance of {@link GameState}.
     *
     * @return the captured game state
     */
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

    /**
     * Replaces existing blocks and logic with information captured in the provided
     * game state.
     *
     * @param gameState the state to load
     */
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
        Optional<Integer> highestValue = gameState.releasedBlocks().stream()
                .map(GameState.BlockState::value).max(Comparator.naturalOrder());
        highestBlockValue = highestValue.orElse(2);
    }

    /**
     * Resets this game logic to it's initial state.
     * Removes all existing blocks and sets score to 0.
     */
    public void resetLogic() {
        clearWorldBlocks();
        blockToPosition = null;
        score = 0;
        highestBlockValue = 2;
    }


}

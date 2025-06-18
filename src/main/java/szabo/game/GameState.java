package szabo.game;

import java.io.*;
import java.util.List;
import java.util.stream.Stream;


/**
 * This record is used to capture the state of the game's logic and physics.
 *
 * @param releasedBlocks blocks which have been released into the playground
 * @param activeBlock    the last block to be generated (not by merging) and
 *                       the player has to position and release
 * @param score          the achieved score
 */
public record GameState(List<BlockState> releasedBlocks,
                        BlockState activeBlock, int score) implements Serializable {
    /**
     * Creates an instance of GameState and captures the given game state.
     *
     * @param releasedBlocks blocks which have been released into the playground
     * @param activeBlock    the last block to be generated (not by merging) and
     *                       *                    the player has to position and release
     * @param score          the achieved score
     */
    public GameState(List<BlockState> releasedBlocks, BlockState activeBlock, int score) {
        this.activeBlock = activeBlock;
        this.releasedBlocks = List.copyOf(releasedBlocks);
        this.score = score;
    }

    /**
     * Returns an unmodifiable List of every block state captured in this game state.
     * If the {@code activeBlock} is not {@code null} it is included as
     * the last member of the list.
     *
     * @return {@code List} of all block states
     */
    public List<BlockState> getAllBlocks() {
        if (activeBlock == null) {
            return releasedBlocks;
        }
        return Stream.concat(releasedBlocks.stream(), Stream.of(activeBlock)).toList();
    }

    /**
     * Reads and returns {@code GameState} which was written to the given file.
     *
     * @param gameSaveFile the file to read
     * @return the {@code GameState} read from the file
     */
    public static GameState load(String gameSaveFile) throws ClassNotFoundException, IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(gameSaveFile))) {
            return (GameState) ois.readObject();
        }
    }

    /**
     * Writes this instance of {@code GameState} to the given file.
     *
     * @param gameSaveFile the file to write to
     */
    public void save(String gameSaveFile) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(gameSaveFile))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.err.println("Failed to save Saved Game: " + e.getMessage());
        }
    }

    /**
     * This record captures the state of a block in the game.
     *
     * @param posX            horizontal position of the block's center as a percentage of playground width
     * @param posY            vertical position of the block's center as a percentage of playground height
     * @param angleRadians    the angle of the block's rotation
     * @param velocityX       the X component of the block's linear velocity vector
     * @param velocityY       the Y component of the block's linear velocity vector
     * @param angularVelocity the angular velocity of the block
     * @param value           the value of the block
     * @param id              the id of the block
     */
    public record BlockState(
            double posX, double posY, double angleRadians, double velocityX, double velocityY,
            double angularVelocity, int value, int id
    ) implements Serializable {
    }
}

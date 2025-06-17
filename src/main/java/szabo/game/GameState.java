package szabo.game;

import java.io.*;
import java.util.List;
import java.util.stream.Stream;


public record GameState(List<BlockState> releasedBlocks,
                        BlockState activeBlock, int score) implements Serializable {
    public GameState(List<BlockState> releasedBlocks, BlockState activeBlock, int score) {
        this.activeBlock = activeBlock;
        this.releasedBlocks = List.copyOf(releasedBlocks);
        this.score = score;
    }

    public List<BlockState> getAllBlocks() {
        if (activeBlock == null) {
            return releasedBlocks;
        }
        return Stream.concat(releasedBlocks.stream(), Stream.of(activeBlock)).toList();
    }

    public static GameState load(String gameSaveFile) throws ClassNotFoundException, IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(gameSaveFile))) {
            return (GameState) ois.readObject();
        }
    }

    public void save(String gameSaveFile) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(gameSaveFile))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.err.println("Failed to save Saved Game: " + e.getMessage());
        }
    }

    public record BlockState(
            double posX, double posY, double angleRadians, double velocityX, double velocityY,
            double angularVelocity, int value, int id
    ) implements Serializable { }
}

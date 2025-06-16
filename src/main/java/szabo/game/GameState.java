package szabo.game;

import javafx.geometry.Point2D;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameState implements Serializable {
    private final BlockState activeBlock;
    private final List<BlockState> blocks;
    private final int score;

    public GameState() {
        activeBlock = null;
        blocks = List.of();
        score = 0;
    }
    public GameState(List<BlockState> blocks, BlockState activeBlock, int score) {
        this.activeBlock = activeBlock;
        this.blocks = List.copyOf(blocks);
        this.score = score;
    }
    public List<BlockState> getAllBlocks() {
        if (activeBlock == null) {
            return blocks;
        }
        return Stream.concat(blocks.stream(), Stream.of(activeBlock)).toList();
    }
    public int getScore() {
        return score;
    }
    public BlockState getActiveBlock() {
        return activeBlock;
    }
    public List<BlockState> getBlocks() {
        return blocks;
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

    public record BlockState(double posX, double posY, double angleRadians,
                             double velocityX, double velocityY,
                             double angularVelocity, int value, int id) implements Serializable {

    }

    @Override
    public String toString() {
        return "GameState{" +
                "activeBlock=" + activeBlock +
                ", blocks=" + blocks +
                ", score=" + score +
                '}';
    }
}

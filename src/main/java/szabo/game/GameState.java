package szabo.game;

import java.io.*;
import java.util.ArrayList;

public class GameState implements Serializable {
    private final ArrayList<BlockInfo> blocks = new ArrayList<>();
    private int score = 0;

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

    public int getScore() {
        return score;
    }


    static class BlockInfo implements Serializable {
        private double x, y;
        private double velocityY = 0;
        private final int value;

        public BlockInfo(double x, double y, int value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }
    }
}

package szabo.game;

import java.io.Serializable;
import java.util.ArrayList;

public class GameState implements Serializable {
    private ArrayList<BlockInfo> blocks;
    private int score;

    public GameState() {


    }


    static class BlockInfo {
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

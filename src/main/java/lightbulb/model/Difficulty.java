// src/main/java/view/Difficulty.java
package lightbulb.model;

public enum Difficulty {
    EASY(8,  8,  4, 3, 2, 0),
    MEDIUM(10,10, 6, 6, 3, 0),
    HARD(12, 12, 8, 9, 4, 180);

    public final int rows, cols, bulbs, extraEdges, shuffleTurns, timeLimitSec;
    Difficulty(int rows, int cols, int bulbs, int extraEdges, int shuffleTurns, int Limit) {
        this.rows = rows;
        this.cols = cols;
        this.bulbs = bulbs;
        this.extraEdges = extraEdges;
        this.shuffleTurns = shuffleTurns;
        this.timeLimitSec = Limit;
    }
}
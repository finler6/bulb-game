// src/main/java/view/Difficulty.java
package lightbulb.model;

/**
 * Výčtový typ Difficulty definuje různé úrovně obtížnosti hry (EASY, MEDIUM, HARD).
 * Každá úroveň obtížnosti má přiřazené specifické parametry, jako jsou rozměry
 * herní desky, počet žárovek, časový limit atd.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
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
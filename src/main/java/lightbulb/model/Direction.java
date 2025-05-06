package lightbulb.model;

/**
 * Výčtový typ Direction reprezentuje čtyři základní směry (UP, RIGHT, DOWN, LEFT)
 * na herní desce. Poskytuje metody pro rotaci směru a určení opačného směru
 * a také pro získání posunů v řádcích a sloupcích.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public enum Direction {
    UP, RIGHT, DOWN, LEFT;

    /** Turn the direction to multiple-of-90°. */
    public Direction rotate(int degrees) {
        int steps = ((degrees / 90) % 4 + 4) % 4;           // 0‒3
        return values()[(this.ordinal() + steps) % 4];
    }

    /** Opposite side. */
    public Direction opposite() {
        return values()[(this.ordinal() + 2) % 4];
    }

    /** Offsets to move to a neighboring cell */
    public int dRow() { return switch (this) {
        case UP    -> -1;
        case DOWN  ->  1;
        default    ->  0;
    }; }
    public int dCol() { return switch (this) {
        case LEFT  -> -1;
        case RIGHT ->  1;
        default    ->  0;
    }; }
}

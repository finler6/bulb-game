package lightbulb.model;

import java.util.EnumSet;

/** Wire: four forms — I, L, T, X. */
public class Wire extends Element {

    private final WireType type;

    public Wire(WireType type) {
        this.type = type;
    }

    /** for GUI. */
    public WireType getWireType() {
        return type;
    }

    @Override
    public EnumSet<Direction> getConnections() {
        EnumSet<Direction> base = switch (type) {
            case I -> EnumSet.of(Direction.UP, Direction.DOWN);
            case L -> EnumSet.of(Direction.UP, Direction.RIGHT);
            case T -> EnumSet.of(Direction.UP, Direction.RIGHT, Direction.LEFT);
            case X -> EnumSet.allOf(Direction.class);
        };
        EnumSet<Direction> rotated = EnumSet.noneOf(Direction.class);
        for (Direction d : base) rotated.add(d.rotate(rotation));
        return rotated;
    }


    @Override
    public String toString() {
        return "Wire{" + type + ", rot=" + rotation + '}';
    }
}

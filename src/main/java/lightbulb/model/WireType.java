package lightbulb.model;

import java.util.EnumSet;

/**
 * Types of wires: L, I, T, X.
 */
public enum WireType {
    L(EnumSet.of(Direction.UP, Direction.RIGHT)),
    I(EnumSet.of(Direction.UP, Direction.DOWN)),
    T(EnumSet.of(Direction.UP, Direction.LEFT, Direction.RIGHT)),
    X(EnumSet.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT));

    private final EnumSet<Direction> initialDirections;

    WireType(EnumSet<Direction> initialDirections) {
        this.initialDirections = initialDirections;
    }

    public EnumSet<Direction> getInitialDirections() {
        return initialDirections.clone();
    }
}
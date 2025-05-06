package lightbulb.model;

import java.util.EnumSet;

/**
 * Výčtový typ WireType definuje různé typy vodičů (L, I, T, X),
 * které se mohou objevit ve hře. Každý typ má specifický tvar
 * a způsob, jakým propojuje strany buňky.
 *
 * @author Gleb Litvinchuk (xlitvi02)
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
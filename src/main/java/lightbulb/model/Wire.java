package lightbulb.model;

import java.util.EnumSet;

/**
 * Třída Wire reprezentuje herní prvek vodiče. Vodiče slouží k propojení
 * ostatních prvků na herní desce a mohou mít různé typy (WireType),
 * které určují, jaké strany buňky propojují.
 * Dědí od třídy Element.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
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

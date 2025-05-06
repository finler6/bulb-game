// model/Bulb.java
package lightbulb.model;

import java.util.EnumSet;

public class Bulb extends Element {

    /** Lamp base output (as if rotation == 0). */
    private final Direction baseLead;

    public Bulb(Direction baseLead) {
        this.baseLead = baseLead;
    }

    /** Actual output taking into account the current rotation. */
    public Direction getLead() {
        return baseLead.rotate(rotation);
    }

    /** Lamp does NOT pass current further, so empty set. */
    @Override
    public EnumSet<Direction> getConnections() {
        return EnumSet.noneOf(Direction.class);
    }

    /** you can twist the light bulb */
    @Override
    public boolean isRotatable() { return true; }

    /* if the UI draws a lamp output, you can calculate it like this: */
    public Direction getVisualInput() {
        return baseLead.rotate(rotation);
    }

    @Override
    public String toString() { return "Bulb{lead=" + getLead() + ", rot=" + rotation + '}'; }
}

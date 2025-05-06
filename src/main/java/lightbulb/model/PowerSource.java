package lightbulb.model;

import java.util.EnumSet;

/** Power source: has a one side input */
public class PowerSource extends Element {

    @Override
    public EnumSet<Direction> getConnections() {
        return EnumSet.of(
                Direction.UP.rotate(rotation),
                Direction.RIGHT.rotate(rotation),
                Direction.DOWN.rotate(rotation),
                Direction.LEFT.rotate(rotation)
        );
    }

    @Override public String toString() { return "PowerSource{rot=" + rotation + '}'; }
}

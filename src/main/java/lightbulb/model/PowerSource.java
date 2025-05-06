package lightbulb.model;

import java.util.EnumSet;

/**
 * Třída PowerSource reprezentuje herní prvek zdroje elektrické energie.
 * Je to výchozí bod pro napájení elektrického obvodu.
 * Dědí od třídy Element.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
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

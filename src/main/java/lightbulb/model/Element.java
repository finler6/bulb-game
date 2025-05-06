package lightbulb.model;

import java.util.EnumSet;

/**
 * Base class of any element in the field (wire, lamp, source).
 * Stores a single rotation angle and provides general rotation operations.
 */
public abstract class Element {

    /** 0, 90, 180 or 270° clockwise. */
    protected int rotation = 0;

    public int  getRotation()          { return rotation; }
    public void setRotation(int deg)   { rotation = ((deg % 360) + 360) % 360; }
    public void rotate()               { rotation = (rotation + 90) % 360; }

    /** Whether you can spin (default is yes). */
    public boolean isRotatable()       { return true; }

    /** Which sides of the element are connected after taking rotation. */
    public abstract EnumSet<Direction> getConnections();
}

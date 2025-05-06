package lightbulb.model;

import java.util.EnumSet;

/**
 * Abstraktní třída Element je základní třídou pro všechny interaktivní prvky,
 * které se mohou nacházet na herní desce (např. vodiče, žárovky, zdroj).
 * Definuje společné vlastnosti jako je rotace a informace o připojeních.
 *
 * @author Gleb Litvinchuk (xlitvi02)
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

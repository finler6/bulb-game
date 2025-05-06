package lightbulb.model;

/** A class representing a cell of the playing field. */
public class Cell {

    private Element element;
    /** Orientation in which the element should stand in the solved pattern (0/90/180/270). */
    private int correctRotation = 0;

    public Cell(Element element) {
        this.element = element;
    }

    public Element getElement()              { return element; }
    public void    setElement(Element el)    { this.element = el; }

    public int  getCorrectRotation()         { return correctRotation; }
    public void setCorrectRotation(int deg)  { correctRotation = ((deg % 360) + 360) % 360; }

    /** Current angle (0/90/180/270). */
    public int getRotation() {
        return (element != null) ? element.getRotation() : 0;
    }

    public void setRotation(int deg) {
        if (element == null) return;
        int target = ((deg % 360) + 360) % 360;
        while (element.getRotation() != target) {
            element.rotate();
        }
    }

    public int getRemainingTurns(Board b, int r, int c) {

        if(!b.isVital(r,c)) return 0;

        /* elements that don't spin */
        if (element == null || element instanceof Bulb ||
                element instanceof PowerSource ||
                (element instanceof Wire w && w.getWireType()==WireType.X))
            return 0;

        int need = b.getSolutionRotation(r,c);
        int curr = getRotation();

        /* I-wire is 180° symmetrical */
        if (element instanceof Wire w && w.getWireType()==WireType.I)
            if ((need%180)==(curr%180)) return 0;

        int diff = (need - curr + 360) % 360;
        return diff / 90;
    }


    /** Rotate the element 90° clockwise. */
    public void rotate() {
        if (element != null && element.isRotatable()) {
            element.rotate();
        }
    }

    @Override public String toString() {
        return "Cell{element=" + element +
                ", rot=" + getRotation() +
                ", correct=" + correctRotation + '}';
    }
}

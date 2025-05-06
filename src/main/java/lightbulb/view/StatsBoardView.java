// view/StatsBoardView.java
package lightbulb.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import lightbulb.model.*;

public class StatsBoardView extends BoardView {

    private int[][] made, need;

    public StatsBoardView(Board board) {
        super(board, null);
        setMouseTransparent(true);
    }

    @Override
    protected void updateButton(Button btn, Cell cell, int r, int c) {
        if (made == null) {
            made = getBoard().getMadeMatrix();
            need = getBoard().getRemainingTurnsMatrix();
        }

        super.updateButton(btn, cell, r, c);

        int cycle = 4;
        Element el = cell.getElement();
        if (el instanceof Wire w && w.getWireType() == WireType.I) cycle = 2;
        if (el instanceof Wire w && w.getWireType() == WireType.X) cycle = 1;
        if (el == null) cycle = 1;

        int m = made[r][c]  % cycle;
        int n = need[r][c];

        int total = (m + n) % cycle;
        if (total == 0 && (m + n) > 0) total = cycle;

        if (total == 0) return;

        String color = (m > n) ? "#c62828"    // unnecessary clicks → red
                : "#1565c0";   // minimal → blue

        Label lbl = new Label(m + "/" + total);
        lbl.setStyle("""
            -fx-font-weight:bold;
            -fx-text-fill: %s;
            -fx-font-size: 13px;
        """.formatted(color));

        ((StackPane) btn.getGraphic()).getChildren().add(lbl);
    }
}

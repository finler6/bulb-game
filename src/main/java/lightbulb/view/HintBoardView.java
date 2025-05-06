// view/HintBoardView.java
package lightbulb.view;

import lightbulb.controller.GameController;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import lightbulb.model.Board;
import lightbulb.model.BoardListener;
import lightbulb.model.Cell;

/**
 * Třída HintBoardView je specializovaná verze BoardView určená pro zobrazení
 * nápovědy v okně s подсказками. Zobrazuje počet otáček potřebných
 * k dosažení správné pozice pro každý otočný prvek.
 * Je neinteraktivní a implementuje BoardListener pro aktualizace.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public class HintBoardView extends BoardView implements BoardListener {

    public HintBoardView(Board board, GameController gc) {
        super(board, gc);
        setMouseTransparent(true);
        board.addListener(this);
    }

    @Override
    public void onBoardChanged(Board board) {
        refresh();
    }

    @Override
    protected void updateButton(Button btn, Cell cell, int r, int c) {
        super.updateButton(btn, cell, r, c);

        int n = cell.getRemainingTurns(getBoard(), r, c);
        if (n==0 || !cell.getElement().isRotatable()) return;

        Label lbl = new Label(String.valueOf(n));
        lbl.setStyle("""
        -fx-font-weight: bold;
        -fx-text-fill: #c62828;
        -fx-font-size: 16px;
    """);

        ((StackPane) btn.getGraphic()).getChildren().add(lbl);
    }
}

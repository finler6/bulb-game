// view/HintWindow.java
package lightbulb.view;

import lightbulb.controller.GameController;
import javafx.stage.Stage;
import lightbulb.model.Board;

/**
 * Třída HintWindow představuje samostatné okno (JavaFX Stage) pro zobrazení
 * herní desky s nápovědami (HintBoardView). Umožňuje uživateli vidět,
 * kolik otočení chybí k vyřešení jednotlivých prvků.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public class HintWindow extends Stage {

    public HintWindow(Board board, GameController gc) {
        setTitle("Hints");

        HintBoardView hbv = new HintBoardView(board, gc);
        setScene(new javafx.scene.Scene(hbv));

        /* close the listener for X */
        setOnHidden(e -> board.removeListener(hbv));
    }
}

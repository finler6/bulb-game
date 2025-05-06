// view/HintWindow.java
package lightbulb.view;

import lightbulb.controller.GameController;
import javafx.stage.Stage;
import lightbulb.model.Board;

public class HintWindow extends Stage {

    public HintWindow(Board board, GameController gc) {
        setTitle("Hints");

        HintBoardView hbv = new HintBoardView(board, gc);
        setScene(new javafx.scene.Scene(hbv));

        /* close the listener for X */
        setOnHidden(e -> board.removeListener(hbv));
    }
}

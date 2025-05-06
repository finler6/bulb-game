// view/StatsWindow.java
package lightbulb.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lightbulb.model.Board;

/**
 * Třída StatsWindow představuje samostatné okno (JavaFX Stage) pro zobrazení
 * statistik hry na konci hry. Využívá StatsBoardView k vizualizaci
 * detailních statistik pro jednotlivé buňky.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public class StatsWindow extends Stage {

    public StatsWindow(Board board){
        setTitle("Cell statistics (made / total)");

        StatsBoardView boardView = new StatsBoardView(board);

        /* a little “legend” at the bottom */
        Label hint = new Label("""
                m —  how many TURNS did you make
                t — the bare minimum that was needed
                red — there are excessive clicks
        """);
        hint.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");
        BorderPane.setMargin(hint, new Insets(6,10,8,10));

        BorderPane root = new BorderPane(boardView);
        root.setBottom(hint);

        setScene(new Scene(root));
        setResizable(false);
    }
}

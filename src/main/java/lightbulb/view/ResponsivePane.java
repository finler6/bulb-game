// view/ResponsivePane.java
package lightbulb.view;

import javafx.beans.binding.Bindings;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;

/**
 * Třída ResponsivePane je vlastní JavaFX panel (dědí od StackPane),
 * který zajišťuje, že jeho obsah (typicky BoardView) se bude škálovat
 * tak, aby se vešel do dostupného prostoru rodičovského kontejneru
 * při zachování poměru stran.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public class ResponsivePane extends StackPane {

    public ResponsivePane(BoardView board) {
        Group g = new Group(board);
        getChildren().add(g);

        setMinSize(0, 0);
        setPrefSize(0, 0);

        double boardW = board.getCols() * 80;
        double boardH = board.getRows() * 80;

        g.scaleXProperty().bind(
                Bindings.createDoubleBinding(
                        () -> Math.min(getWidth() / boardW, getHeight() / boardH),
                        widthProperty(), heightProperty()));
        g.scaleYProperty().bind(g.scaleXProperty());
    }
}

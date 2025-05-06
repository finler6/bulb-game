package lightbulb.model;

/** Receives a notification whenever the board content has changed. */
@FunctionalInterface
public interface BoardListener {
    void onBoardChanged(Board board);
}

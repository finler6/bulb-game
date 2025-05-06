// RotateReplayCmd.java
package lightbulb.controller.replay;

import lightbulb.model.Board;
import lightbulb.model.command.Command;

/**
 * Třída RotateReplayCmd představuje příkaz pro přehrávání akce otočení herního prvku.
 * Uchovává informace o předchozím a následujícím stavu otočení a hodnotě časovače
 * pro přesnou rekonstrukci herního stavu během přehrávání.
 * Implementuje rozhraní Command.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public class RotateReplayCmd implements Command {
    private final Board board;
    private final int r, c, prev, next;
    private final int timerValue;

    public RotateReplayCmd(Board b, int r, int c, int prev, int next, int timerVal){
        board=b; this.r=r; this.c=c; this.prev=prev; this.next=next;
        this.timerValue = timerVal;
    }

    @Override public void execute() { apply(next); }
    @Override public void undo()    { apply(prev); }

    private void apply(int angle){
        board.getCell(r,c).setRotation(angle);
    }

    public int getTimerValue() {
        return timerValue;
    }
}
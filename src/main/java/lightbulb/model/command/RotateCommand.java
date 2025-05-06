package lightbulb.model.command;

import lightbulb.model.Board;
import lightbulb.model.Cell;
import java.time.Instant;

/**
 * Třída RotateCommand reprezentuje konkrétní příkaz pro akci otočení
 * herního prvku v buňce. Umožňuje vykonání a zrušení této akce,
 * což je využíváno pro funkcionalitu undo/redo a logování.
 * Implementuje rozhraní Command.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public class RotateCommand implements Command {

    private final Board board;
    private final int r, c;
    private final int prevRot, newRot;
    private final long timestamp;
    private final int timerValue;

    public RotateCommand(Board b, int r, int c, int currentTimerValue) {
        this.board = b; this.r = r; this.c = c;
        this.timerValue = currentTimerValue;
        this.timestamp = Instant.now().toEpochMilli();

        Cell cell = b.getCell(r, c);
        this.prevRot = cell.getRotation();

        board.rotateCell(r,c);
        board.incMade(r,c);
        this.newRot = cell.getRotation();
    }

    @Override public void execute() {
        while (board.getCell(r, c).getRotation() != newRot)
            board.rotateCell(r, c);
    }
    @Override public void undo() {
        board.decMade(r,c);
        while (board.getCell(r, c).getRotation() != prevRot)
            board.rotateCell(r, c);
    }

    public String toJson() {
        return String.format(
                "{\"type\":\"rotate\",\"r\":%d,\"c\":%d,\"prev\":%d,\"next\":%d,\"ts\":%d,\"timer\":%d}",
                r, c, prevRot, newRot, timestamp, timerValue);
    }

    public int getTimerValue() {
        return timerValue;
    }
}

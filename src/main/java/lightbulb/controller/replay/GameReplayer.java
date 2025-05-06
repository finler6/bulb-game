package lightbulb.controller.replay;

import com.google.gson.*;
import lightbulb.controller.GameHistory;
import lightbulb.model.command.Command;
import lightbulb.model.persistence.BoardSerializer;
import lightbulb.model.Board;
import lightbulb.model.Difficulty;
import lightbulb.model.LevelData;
import lightbulb.model.command.RotateCommand;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Třída GameReplayer umožňuje načítání a přehrávání zaznamenaných herních seancí
 * z logovacích souborů. Podporuje krokování vpřed a vzad v záznamu
 * a také možnost pokračovat ve hře od aktuálního bodu přehrávání.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public final class GameReplayer {

    private final Board board;
    private final Difficulty difficulty;
    private final Deque<Command> future = new ArrayDeque<>();
    private final Deque<Command> past   = new ArrayDeque<>();
    private int initialTimerValue;
    private int currentTimerValue;

    private GameReplayer(Board b, Difficulty diff){
        board = b;
        difficulty = diff;
        initialTimerValue = (difficulty.timeLimitSec == 0) ? 0 : difficulty.timeLimitSec;
        currentTimerValue = initialTimerValue;
    }

    public static GameReplayer load(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        if (lines.isEmpty()) {
            throw new IOException("Log file is empty: " + path);
        }

        LevelData levelData = BoardSerializer.fromJson(lines.getFirst());
        GameReplayer rep = new GameReplayer(levelData.board(), levelData.difficulty());

        Gson g = new Gson();
        List<Command> commandsInOrder = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            try {
                JsonObject jo = g.fromJson(lines.get(i), JsonObject.class);
                if (!"rotate".equals(jo.get("type").getAsString())) continue;

                int timerVal = rep.initialTimerValue;
                if (jo.has("timer")) {
                    timerVal = jo.get("timer").getAsInt();
                } else {
                    System.err.println("Warning: Log line " + (i+1) + " missing 'timer' field.");
                }

                commandsInOrder.add(
                        new RotateReplayCmd(rep.board,
                                jo.get("r").getAsInt(),
                                jo.get("c").getAsInt(),
                                jo.get("prev").getAsInt(),
                                jo.get("next").getAsInt(),
                                timerVal));
            } catch (JsonSyntaxException | IllegalStateException | NullPointerException jsonEx) {
                System.err.println("Warning: Skipping invalid line in log file " + path + " at line " + (i+1) + ": " + lines.get(i) + " - Error: " + jsonEx.getMessage());
            }
        }
        rep.future.clear();
        for (int i = commandsInOrder.size() - 1; i >= 0; i--) {
            rep.future.push(commandsInOrder.get(i));
        }

        return rep;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getCurrentTimerValue() { return currentTimerValue; }

    public boolean stepForward(){
        if(future.isEmpty()) return false;
        Command cmd = future.pop();
        if (cmd instanceof RotateReplayCmd replayCmd) {
            replayCmd.execute();
            this.currentTimerValue = replayCmd.getTimerValue();
            past.push(replayCmd);
            board.fireUpdateExtern();
            return true;
        } else {
            cmd.execute();
            past.push(cmd);
            board.fireUpdateExtern();
            return true;
        }
    }

    public boolean stepBack(){
        if(past.isEmpty()) return false;
        Command cmd = past.pop();

        if (cmd instanceof RotateReplayCmd replayCmd) {
            replayCmd.undo();
            future.push(replayCmd);

            if (!past.isEmpty() && past.peek() instanceof RotateReplayCmd previousCmd) {
                this.currentTimerValue = previousCmd.getTimerValue();
            } else {
                this.currentTimerValue = this.initialTimerValue;
            }
            board.fireUpdateExtern();
            return true;
        } else {
            cmd.undo();
            future.push(cmd);
            board.fireUpdateExtern();
            return true;
        }
    }

    public void resumePlay(){
        future.clear();
        GameHistory.reset();
        GameHistory.getInstance().snapshot(board, this.difficulty);
    }

    public Board getBoard(){ return board; }
}

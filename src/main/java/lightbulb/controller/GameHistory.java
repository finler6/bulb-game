// controller/GameHistory.java
package lightbulb.controller;

import lightbulb.model.persistence.BoardSerializer;
import lightbulb.log.GameLog;
import lightbulb.model.Board;
import lightbulb.model.command.Command;
import lightbulb.model.command.RotateCommand;
import lightbulb.model.Difficulty;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Třída GameHistory implementuje funkcionalitu zpět (undo) a znovu (redo)
 * pomocí návrhového vzoru Command. Zajišťuje také logování herních akcí
 * a počátečního stavu herní desky pro možnost pozdějšího přehrání.
 * Jedná se o singleton.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public final class GameHistory {

    /* ---------- singleton ---------- */
    private static volatile GameHistory INSTANCE;

    /** Get (or lazily create) the history of the current game */
    public static GameHistory getInstance() {
        if (INSTANCE == null) {
            synchronized (GameHistory.class) {
                if (INSTANCE == null) {
                    try { INSTANCE = new GameHistory(); }
                    catch (IOException e) {
                        throw new RuntimeException("Cannot init GameHistory", e);
                    }
                }
            }
        }
        return INSTANCE;
    }

    /** Start a new batch - close the old log and create a new one */
    public static synchronized void reset() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
    /* ---------- /singleton ---------- */

    /* ---------- common fields ---------- */
    private final Deque<Command> undo = new ArrayDeque<>();
    private final Deque<Command> redo = new ArrayDeque<>();
    private final GameLog log;
    private boolean initSaved = false;
    /* ---------- /fields ---------- */

    public void snapshot(Board b, Difficulty difficulty) {
        if (initSaved) return;
        if (log == null) {
            System.err.println("ERROR: GameLog is not initialized in GameHistory!");
            return;
        }
        if (b == null || difficulty == null) {
            System.err.println("ERROR: Attempted to snapshot null board or difficulty!");
            return;
        }
        log.write(BoardSerializer.toJson(b, difficulty));
        log.flush();
        initSaved = true;
    }

    /** private-constructor */
    private GameHistory() throws IOException {
        Path dir = Paths.get("src/main/resources/logs");
        Files.createDirectories(dir);

        String sessionName = "gamelog_" + Instant.now().toEpochMilli();
        log = new GameLog(dir.resolve(sessionName + ".jsonl").toString());
    }

    /* ---------- API ---------- */
    public void doRotate(Board b, int r, int c, int currentTimerValue) {
        if (log == null || b == null) {
            System.err.println("ERROR: Log or Board is null in doRotate!");
            return;
        }
        if (!initSaved) {
            System.err.println("ERROR: doRotate called before initial snapshot!");
            return;
        }

        RotateCommand cmd = new RotateCommand(b, r, c, currentTimerValue);
        undo.push(cmd);
        redo.clear();

        log.write(cmd.toJson());
        log.flush();
    }

    public void undo() {
        if (!undo.isEmpty()) {
            Command c = undo.pop();
            c.undo();
            redo.push(c);
        }
    }

    public void redo() { if (!redo.isEmpty()) { Command c = redo.pop(); c.execute(); undo.push(c);} }
    public void close() { try { log.close(); } catch (IOException ignore) {} }
    /* ---------- /API ---------- */
}

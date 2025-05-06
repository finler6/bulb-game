package lightbulb.controller;

import lightbulb.model.*;

import java.util.*;

/**
 * Třída GameController spravuje herní logiku, aktualizuje stav připojení
 * jednotlivých prvků na herní desce a vyhodnocuje podmínky vítězství ve hře.
 * Zajišťuje interakci mezi modelem (herní deskou) a uživatelským rozhraním.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public class GameController {
    private Board board;
    // Array where we will store whether the cell is connected or not
    private boolean[][] connected;
    // Flag to enable/disable detailed logging
    private static final boolean DETAILED_LOGGING = false;

    private final GameHistory history;

    public GameController(Board board) {
        this.board = board;
        this.connected = new boolean[board.getRows()][board.getCols()];

        this.history = GameHistory.getInstance();
    }

    public void rotate(int r,int c){
        updateConnections();
    }

    public void undo() { history.undo(); updateConnections(); }
    public void redo() { history.redo(); updateConnections(); }

    /**
     * Finds the PowerSource on the field.
     * Assumes there is only one PowerSource in the game.
     */
    private int[] findPowerSource() {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Element e = board.getCell(r, c).getElement();
                if (e instanceof PowerSource) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    /**
     * Updates the connection information of all cells by starting a bypass
     * from a cell with a power source.
     */
    public void updateConnections() {
        if (DETAILED_LOGGING) System.out.println("\n--- Running updateConnections ---");

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                connected[r][c] = false;
            }
        }
        if (DETAILED_LOGGING) System.out.println("LOG: 'connected' array reset.");

        int[] sourcePos = findPowerSource();
        if (sourcePos == null) {
            if (DETAILED_LOGGING) System.out.println("LOG: Power source not found. Exiting updateConnections.");
            return;
        }
        if (DETAILED_LOGGING) System.out.printf("LOG: Power source found at (%d, %d)\n", sourcePos[0], sourcePos[1]);

        // Start BFS/DFS
        Queue<int[]> queue = new LinkedList<>();
        queue.add(sourcePos);
        connected[sourcePos[0]][sourcePos[1]] = true;
        if (DETAILED_LOGGING) System.out.printf("LOG: Added source (%d, %d) to queue. Marked as connected.\n", sourcePos[0], sourcePos[1]);

        if (DETAILED_LOGGING) System.out.println("LOG: Starting BFS loop...");
        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int row = pos[0];
            int col = pos[1];
            if (DETAILED_LOGGING) System.out.printf("\nLOG: Polled (%d, %d) from queue.\n", row, col);

            Cell cell = board.getCell(row, col);
            Element element = cell.getElement();
            if (element == null) {
                if (DETAILED_LOGGING) System.out.printf("LOG: Element at (%d, %d) is null. Skipping neighbors.\n", row, col);
                continue;
            }

            EnumSet<Direction> cellDirs = getDirections(element);
            if (DETAILED_LOGGING) System.out.printf("LOG: Cell (%d, %d) [Type: %s, Rot: %d] connects towards: %s\n",
                    row, col, element.getClass().getSimpleName(), cell.getRotation(), cellDirs);

            // For each neighboring direction we check if we can “go” there
            for (Direction dir : cellDirs) {
                int nr = row + dirRow(dir);
                int nc = col + dirCol(dir);
                if (!inBounds(nr, nc)) continue;

                Cell    neighCell = board.getCell(nr, nc);
                Element neighElem = neighCell.getElement();
                if (neighElem == null) continue;

                Direction opp = oppositeDirection(dir);

                if (neighElem instanceof Bulb bulb) {
                    if (bulb.getLead() == opp) {
                        if (!connected[nr][nc]) {
                            connected[nr][nc] = true;
                            if (DETAILED_LOGGING)
                                System.out.printf("LOG: Bulb at (%d,%d) lit from %s.\n", nr, nc, opp);
                        }
                    } else if (DETAILED_LOGGING) {
                        System.out.printf("LOG: Bulb at (%d,%d) NOT lit – lead %s, came from %s.\n",
                                nr, nc, bulb.getLead(), opp);
                    }
                    continue;
                }

                if (neighElem.getConnections().contains(opp) && !connected[nr][nc]) {
                    connected[nr][nc] = true;
                    queue.add(new int[]{nr, nc});
                }
            }
        }
        if (DETAILED_LOGGING) System.out.println("LOG: BFS loop finished.");
        if (DETAILED_LOGGING) System.out.println("--- updateConnections finished ---");
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < board.getRows() && c >= 0 && c < board.getCols();
    }


    public boolean isConnected(int row, int col) {
        if (row < 0 || row >= board.getRows() || col < 0 || col >= board.getCols()) {
            return false;
        }
        return connected[row][col];
    }

    /** true if at least one lamp is already lit */
    public boolean anyBulbLit() {
        for (int r = 0; r < board.getRows(); r++)
            for (int c = 0; c < board.getCols(); c++)
                if (board.getCell(r,c).getElement() instanceof Bulb && connected[r][c])
                    return true;
        return false;
    }


    /**
     * Auxiliary method: extract connection directions
     * for element (Wire, Bulb, PowerSource).
     */
    private EnumSet<Direction> getDirections(Element elem) {
        if (elem instanceof Wire)         return ((Wire) elem).getConnections();
        if (elem instanceof PowerSource)  return EnumSet.allOf(Direction.class);
        return EnumSet.noneOf(Direction.class);
    }


    /**
     * Directional -> line offset.
     */
    private int dirRow(Direction d) {
        switch (d) {
            case UP: return -1;
            case DOWN: return 1;
            default: return 0;
        }
    }

    /**
     * Directional -> column offset.
     */
    private int dirCol(Direction d) {
        switch (d) {
            case LEFT: return -1;
            case RIGHT: return 1;
            default: return 0;
        }
    }

    /**
     * Returns the opposite direction.
     */
    private Direction oppositeDirection(Direction d) {
        switch (d) {
            case UP:    return Direction.DOWN;
            case DOWN:  return Direction.UP;
            case LEFT:  return Direction.RIGHT;
            case RIGHT: return Direction.LEFT;
        }
        return d;
    }

    /**
     * Checks if the game is won.
     * Win condition: all bulbs on the field must be connected to the source.
     * @return true if all bulbs are connected, otherwise false.
     */
    public boolean isGameWon() {
        boolean bulbFound = false;
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Element element = board.getCell(r, c).getElement();
                if (element instanceof Bulb) {
                    bulbFound = true;
                    if (!connected[r][c]) {
                        if (DETAILED_LOGGING) System.out.printf("LOG: Win condition check failed: Bulb at (%d, %d) is not connected.\n", r, c);
                        return false;
                    } else {
                        if (DETAILED_LOGGING) System.out.printf("LOG: Win condition check: Bulb at (%d, %d) is connected.\n", r, c);
                    }
                }
            }
        }
        if (!bulbFound) {
            if (DETAILED_LOGGING) System.out.println("LOG: Win condition check failed: No bulbs found on the board.");
            return false;
        }

        if (DETAILED_LOGGING) System.out.println("LOG: Win condition check passed: All bulbs are connected!");
        return true;
    }
}
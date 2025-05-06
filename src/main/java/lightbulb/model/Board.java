package lightbulb.model;

import java.util.ArrayList;
import java.util.List;

import java.util.Arrays;
import java.util.Queue;
import java.util.ArrayDeque;

/**
 * Třída Board reprezentuje herní desku jako dvourozměrné pole buněk (Cell).
 * Uchovává stav jednotlivých buněk, spravuje posluchače změn na desce,
 * pamatuje si řešení úrovně a informace o počtu provedených tahů.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public class Board {

    private final List<BoardListener> listeners = new ArrayList<>();
    public  void addListener   (BoardListener l) { listeners.add(l); }
    public  void removeListener(BoardListener l) { listeners.remove(l); }
    private void fireUpdate() {
        for (BoardListener l : listeners) l.onBoardChanged(this);
    }

    public void fireUpdateExtern() { fireUpdate(); }

    private final int rows, cols;
    private final Cell[][] cells;
    private final int[][]   solution;
    private final boolean[][] vital;
    private final int[][] made;

    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        cells = new Cell[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                cells[r][c] = new Cell(null);
        solution = new int[rows][cols];
        vital    = new boolean[rows][cols];
        made = new int[rows][cols];
    }

    public int  getRows()             { return rows; }
    public int  getCols()             { return cols; }
    public Cell getCell(int r,int c)  { return cells[r][c]; }

    public void setCell(int r, int c, Cell cell) {
        cells[r][c] = cell;
        fireUpdate();
    }

    /** Turns the cage and notifies listeners. */
    public void rotateCell(int r, int c) {
        cells[r][c].rotate();
        fireUpdate();
    }

    public int[][] getRemainingTurnsMatrix() {
        int[][] out = new int[rows][cols];
        for (int r=0;r<rows;r++)
            for (int c=0;c<cols;c++)
                out[r][c] = cells[r][c].getRemainingTurns(this,r,c);
        return out;
    }


    public void rememberSolution() {
        for (int r=0;r<rows;r++)
            for (int c=0;c<cols;c++)
                solution[r][c] = cells[r][c].getRotation();
        computeVital();
    }

    public boolean isVital(int r,int c){ return vital[r][c]; }

    public int getSolutionRotation(int r,int c) { return solution[r][c]; }

    public void incMade(int r,int c){ made[r][c]++; }
    public int  getMade(int r,int c){ return made[r][c]; }
    public int[][] getMadeMatrix(){ return made; }

    /* ------------ private BFS by solved field ------------- */
    private void computeVital(){
        for (boolean[] row : vital) Arrays.fill(row,false);

        int srcR=-1,srcC=-1;
        for(int r=0;r<rows;r++)
            for(int c=0;c<cols;c++)
                if(cells[r][c].getElement() instanceof PowerSource){
                    srcR=r; srcC=c;
                }
        if(srcR==-1) return;

        Queue<int[]> q = new ArrayDeque<>();
        vital[srcR][srcC]=true; q.add(new int[]{srcR,srcC});

        while(!q.isEmpty()){
            int[] p=q.poll(); int r=p[0], c=p[1];
            Element e = cells[r][c].getElement();
            if(e==null) continue;
            for(Direction d : e.getConnections()){
                int nr=r+d.dRow(), nc=c+d.dCol();
                if(nr<0||nr>=rows||nc<0||nc>=cols) continue;
                Element n = cells[nr][nc].getElement();
                if(n==null || !n.getConnections().contains(d.opposite())) continue;
                if(!vital[nr][nc]){
                    vital[nr][nc]=true;
                    q.add(new int[]{nr,nc});
                }
            }
        }
    }
    /* ------------ /BFS ------------- */

    public void placeElement(int r, int c, String token) {
        Element el = switch (token) {
            case "Bulb"         -> new Bulb(Direction.UP);
            case "PowerSource"  -> new PowerSource();
            case "Wire_L"       -> new Wire(WireType.L);
            case "Wire_I"       -> new Wire(WireType.I);
            case "Wire_T"       -> new Wire(WireType.T);
            case "Wire_X"       -> new Wire(WireType.X);
            case "EMPTY"        -> null;
            default -> throw new IllegalArgumentException("Unknown element: " + token);
        };
        cells[r][c].setElement(el);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++)
                sb.append(cells[r][c]).append(' ');
            sb.append('\n');
        }
        return sb.toString();
    }

    public void decMade(int r,int c){
        if (made[r][c] > 0) {
            made[r][c]--;
        }
    }

}

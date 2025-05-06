package lightbulb.model;

import lightbulb.controller.GameController;

import java.util.*;

/**
 * Třída LevelGenerator je zodpovědná za generování nových herních úrovní.
 * Vytváří herní desku se zdrojem, žárovkami a vodiči tak, aby existovalo
 * alespoň jedno řešení, a následně prvky náhodně pootočí.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public final class LevelGenerator {

    private final Random rnd = new Random();

    /* PUBLIC FACTORY METHOD */
    public Board generate(int rows, int cols,
                          int bulbs,
                          int extraEdges,
                          int maxShuffleTurns) {

        Board board = new Board(rows, cols);

        /* 1) source                                                    */
        Point src = randomFree(rows, cols, Set.of());
        board.getCell(src.r, src.c).setElement(new PowerSource());

        /* 2) lamps - put only where there is an available neighbor around it  */
        Set<Point> lamps = new HashSet<>();
        while (lamps.size() < bulbs) {
            Point p;
            do {
                p = randomFree(rows, cols, lamps, src);
            } while (!hasFreeNeighbour(p, rows, cols, lamps, src));
            lamps.add(p);
            board.getCell(p.r, p.c).setElement(new Bulb(Direction.UP));
        }

        /* 3)-5) construction of the solved level  */
        buildSpanningTree(board, src, lamps);
        addExtraEdges(board, extraEdges);
        /* 4.5) add crosses */
        addCrossJunctions(board,(rows*cols)/20);
        materialiseWires(board);


        /* 6) beautifully orient the lamps       */
        orientBulbs(board);

        /* 6.5) guarantee the input of each lamp               */
        if (!ensureBulbHasInput(board))
            return generate(rows, cols, bulbs, extraEdges, maxShuffleTurns);

        if (!isSolved(board))
            return generate(rows, cols, bulbs, extraEdges, maxShuffleTurns);

        board.rememberSolution();

        /* 7) shuffle until it all goes out. */
        shuffleUntilDark(board, maxShuffleTurns, bulbs, extraEdges);

        return board;
    }

    /*Building a basic DFS tree*/
    private void buildSpanningTree(Board b, Point start, Set<Point> targets) {
        boolean[][] seen = new boolean[b.getRows()][b.getCols()];
        dfs(b, start, seen, new HashSet<>(targets));
    }

    private void dfs(Board b, Point v,
                     boolean[][] seen,
                     Set<Point> todo) {

        seen[v.r][v.c] = true;
        todo.remove(v);
        if (todo.isEmpty()) return;

        List<Direction> dirs = new ArrayList<>(List.of(Direction.values()));
        Collections.shuffle(dirs, rnd);

        for (Direction d : dirs) {
            int nr = v.r + d.dRow(), nc = v.c + d.dCol();
            if (!inBounds(b, nr, nc) || seen[nr][nc]) continue;

            addConn(b, v.r, v.c, d);
            addConn(b, nr, nc, d.opposite());

            dfs(b, new Point(nr, nc), seen, todo);
            if (todo.isEmpty()) return;
        }
    }

    /*adding extraEdges*/
    private void addExtraEdges(Board b, int extra) {
        record Edge(int r,int c,Direction d){}
        List<Edge> cand = new ArrayList<>();

        for (int r = 0; r < b.getRows(); r++)
            for (int c = 0; c < b.getCols(); c++) {
                Element e = b.getCell(r,c).getElement();
                if (e == null) continue;

                for (Direction d : List.of(Direction.RIGHT, Direction.DOWN)) {
                    int nr = r + d.dRow(), nc = c + d.dCol();
                    if (!inBounds(b,nr,nc)) continue;
                    Element n = b.getCell(nr,nc).getElement();
                    if (n == null) continue;

                    boolean already = e.getConnections().contains(d) &&
                            n.getConnections().contains(d.opposite());
                    if (!already) cand.add(new Edge(r,c,d));
                }
            }

        Collections.shuffle(cand, rnd);
        int k = Math.min(extra, cand.size());
        for (int i = 0; i < k; i++) {
            Edge e = cand.get(i);
            addConn(b, e.r, e.c, e.d);
            addConn(b, e.r + e.d.dRow(),
                    e.c + e.d.dCol(),
                    e.d.opposite());
        }
    }

    private final Map<Point, EnumSet<Direction>> pending = new HashMap<>();

    private void remember(int r,int c,Direction d){
        pending.computeIfAbsent(new Point(r,c),k->EnumSet.noneOf(Direction.class))
                .add(d);
    }

    private void materialiseWires(Board b){
        for (var e : pending.entrySet()){
            Point p = e.getKey();
            EnumSet<Direction> dirs = e.getValue();
            if (dirs.isEmpty()) continue;

            Wire w;
            switch (dirs.size()){
                case 1 -> {
                    w = new Wire(WireType.L);
                    for (int i=0;i<4 && !w.getConnections().containsAll(dirs);i++)
                        w.rotate();
                }
                case 2 -> {
                    boolean straight = (dirs.contains(Direction.UP)&&dirs.contains(Direction.DOWN)) ||
                            (dirs.contains(Direction.LEFT)&&dirs.contains(Direction.RIGHT));
                    w = new Wire(straight?WireType.I:WireType.L);
                    while (!w.getConnections().equals(dirs)) w.rotate();
                }
                case 3 -> {
                    w = new Wire(WireType.T);
                    while (!w.getConnections().equals(dirs)) w.rotate();
                }
                case 4 -> w = new Wire(WireType.X);
                default -> throw new IllegalStateException();
            }
            while (!rotationIsInside(b,p.r,p.c,w)) w.rotate();
            b.getCell(p.r,p.c).setElement(w);
        }
        pending.clear();
    }


    private void orientBulbs(Board b){
        for(int r=0;r<b.getRows();r++)
            for(int c=0;c<b.getCols();c++){
                if(!(b.getCell(r,c).getElement() instanceof Bulb bulb)) continue;

                for(Direction d:Direction.values()){
                    int nr=r+d.dRow(), nc=c+d.dCol();
                    if(!inBounds(b,nr,nc)) continue;
                    Element n = b.getCell(nr,nc).getElement();
                    if(n!=null && n.getConnections().contains(d.opposite())){
                        while(bulb.getVisualInput()!=d) bulb.rotate();
                        break;
                    }
                }
            }
    }

    /* SHUFFLE*/
    private void shuffleUntilDark(Board b,
                                  int maxTurns,
                                  int bulbs,
                                  int extraEdges) {

        final int MAX_SHUFFLE_ATTEMPTS = 100;
        if (maxTurns <= 0) return;

        GameController gc = new GameController(b);

        for (int attempt = 0; attempt < MAX_SHUFFLE_ATTEMPTS; attempt++) {

            boolean[][] locked = new boolean[b.getRows()][b.getCols()];
            for (int r = 0; r < b.getRows(); r++)
                for (int c = 0; c < b.getCols(); c++)
                    if (b.getCell(r, c).getElement() instanceof Bulb) {
                        locked[r][c] = true;
                    }


            /* random safe rotation */
            for (int r = 0; r < b.getRows(); r++)
                for (int c = 0; c < b.getCols(); c++) {

                    if (locked[r][c]) continue;
                    Element e = b.getCell(r,c).getElement();
                    if (e == null || e instanceof Bulb) continue;

                    List<Integer> ok = new ArrayList<>();
                    for (int t=0; t<4; t++) {
                        if (rotationIsInside(b,r,c,e)) ok.add(t);
                        e.rotate();
                    }
                    for (int t=0; t<4; t++) e.rotate();

                    if (ok.size() <= 1) continue;
                    ok.remove(Integer.valueOf(0));
                    int turns = ok.get(rnd.nextInt(
                            Math.min(ok.size(), maxTurns)));
                    for (int i=0; i<turns; i++) e.rotate();
                }

            /* made sure the bulbs still have an input */
            ensureBulbHasInput(b);

            /* reconnected  */
            gc.updateConnections();
            if (!gc.anyBulbLit()) return;
        }

        /* If 100 attempts did not help - regenerate the level again */
        generate(b.getRows(), b.getCols(), bulbs, extraEdges, maxTurns);
    }


    /** Adds the specified number of X-units
     * (inner cells that will have all 4 sides) */
    private void addCrossJunctions(Board b, int howMany) {

        List<Point> interior = new ArrayList<>();
        for (int r = 1; r < b.getRows()-1; r++)
            for (int c = 1; c < b.getCols()-1; c++)
                interior.add(new Point(r,c));

        Collections.shuffle(interior, rnd);
        int made = 0;

        for (Point p : interior) {
            if (made >= howMany) break;

            /* skip if the cell is Bulb / PowerSource */
            Element center = b.getCell(p.r,p.c).getElement();
            if (center instanceof Bulb || center instanceof PowerSource) continue;

            /* adding missing 4 sides */
            for (Direction d : Direction.values()) {
                int nr = p.r + d.dRow(), nc = p.c + d.dCol();
                addConn(b, p.r, p.c, d);                       // to center
                addConn(b, nr, nc, d.opposite());              // to neighbor
            }
            made++;
        }
    }

    /** true, if in current orientation all lamps under power */
    private boolean isSolved(Board b) {
        GameController gc = new GameController(b);
        gc.updateConnections();
        return gc.isGameWon();
    }

    /**
     * Guarantee: all lamps have an input,
     * @return true – all ok; false – fix needed
     */
    private boolean ensureBulbHasInput(Board b) {

        WireType[] CANDIDATES = { WireType.L, WireType.T, WireType.I, WireType.X };
        boolean allOk = true;

        for (int r = 0; r < b.getRows(); r++)
            for (int c = 0; c < b.getCols(); c++) {

                if (!(b.getCell(r,c).getElement() instanceof Bulb)) continue;
                if (hasInput(b, r, c)) continue;
                boolean fixed = false;

                /* 1. Trying to put a NEW wire on an empty neighbor */
                for (Direction d : Direction.values()) {
                    int nr = r + d.dRow(), nc = c + d.dCol();
                    if (!inBounds(b,nr,nc)) continue;
                    Cell neigh = b.getCell(nr,nc);
                    if (neigh.getElement() != null) continue;

                    Wire w = new Wire(WireType.L);
                    for (int rot=0; rot<4; rot++, w.rotate())
                        if (w.getConnections().contains(d.opposite()) &&
                                rotationIsInside(b,nr,nc,w)) {
                            neigh.setElement(w);
                            fixed = true;
                            break;
                        }
                    if (fixed) break;
                }

                /* 2. Turning the existing wire didn't help? - Let's change its type */
                if (!fixed)
                    for (Direction d : Direction.values()) {
                        int nr = r + d.dRow(), nc = c + d.dCol();
                        if (!inBounds(b,nr,nc)) continue;
                        Cell neigh = b.getCell(nr,nc);
                        if (!(neigh.getElement() instanceof Wire old)) continue;

                        EnumSet<Direction> mustKeep = EnumSet.copyOf(old.getConnections());

                        for (WireType t : CANDIDATES) {
                            Wire w = new Wire(t);
                            for (int rot=0; rot<4; rot++, w.rotate())
                                if (w.getConnections().containsAll(mustKeep) &&
                                        w.getConnections().contains(d.opposite()) &&
                                        rotationIsInside(b,nr,nc,w)) {
                                    neigh.setElement(w);
                                    fixed = true;
                                    break;
                                }
                            if (fixed) break;
                        }
                        if (fixed) break;
                    }

                if (!fixed) allOk = false;
            }

        return allOk;
    }

    private boolean hasInput(Board b,int r,int c){
        for (Direction d : Direction.values()) {
            int nr = r + d.dRow(), nc = c + d.dCol();
            if (!inBounds(b,nr,nc)) continue;
            Element neigh = b.getCell(nr,nc).getElement();
            if (neigh!=null && neigh.getConnections().contains(d.opposite()))
                return true;
        }
        return false;
    }

    private boolean inBounds(Board b,int r,int c){
        return r>=0 && r<b.getRows() && c>=0 && c<b.getCols();
    }

    private boolean rotationIsInside(Board b,int r,int c,Element e){
        for(Direction d:e.getConnections()){
            int nr=r+d.dRow(), nc=c+d.dCol();
            if(!inBounds(b,nr,nc)) return false;
        }
        return true;
    }

    /* check if a lamp-cell has a free neighbour*/
    private boolean hasFreeNeighbour(Point p,
                                     int rows,int cols,
                                     Set<Point> occupied,
                                     Point src){
        for(Direction d:Direction.values()){
            int nr=p.r+d.dRow(), nc=p.c+d.dCol();
            if(nr<0||nr>=rows||nc<0||nc>=cols) continue;
            Point n=new Point(nr,nc);
            if(!occupied.contains(n) && !n.equals(src)) return true;
        }
        return false;
    }

    private void addConn(Board b,int r,int c,Direction need){
        Element e = b.getCell(r,c).getElement();
        if(e instanceof Bulb || e instanceof PowerSource) return;
        remember(r,c,need);
    }

    private Point randomFree(int rows,int cols,
                             Set<Point> busy,
                             Point... extra){
        Point p;
        do{
            p=new Point(rnd.nextInt(rows),rnd.nextInt(cols));
        }while(busy.contains(p)||Arrays.asList(extra).contains(p));
        return p;
    }

    /* small record-wrap for coordinates */
    private record Point(int r,int c){}
}

package lightbulb.view;

import lightbulb.controller.GameController;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

import lightbulb.model.*;


public class BoardView extends GridPane {

    private static final String IMG_RESOURCE_BASE_PATH = "/img/";

    private static Image IMG_BULB_OFF;
    private static Image IMG_BULB_ON;
    private static Image IMG_SOURCE;
    private static Image IMG_EMPTY;

    private static final Map<WireType, Image> WIRE_ON  = new EnumMap<>(WireType.class);
    private static final Map<WireType, Image> WIRE_OFF = new EnumMap<>(WireType.class);

    private static String currentSkinName = GameSettings.getCurrentSkinPath();

    static {
        loadAllImages();
    }

    public static final double CELL = 80;

    /* ------------------------------------------------------------------ */

    private final Board board;
    private final GameController gameController;
    private final boolean interactive;
    private final Button[][] cellBtns;
    private boolean[][] cached;

    // callback on win
    private Runnable onWin;

    private BiConsumer<Integer, Integer> rotateRequestHandler;

    public void setOnRotateRequest(BiConsumer<Integer, Integer> handler) {
        this.rotateRequestHandler = handler;
    }

    public void setOnWin(Runnable cb) {
        this.onWin = cb;
    }

    public Runnable getOnWin() {
        return onWin;
    }

    protected Board getBoard() {
        return board;
    }

    public int getRows() { return board.getRows(); }
    public int getCols() { return board.getCols(); }

    private static Image loadImageFromFile(String skinFolder, String fileName) {
        String fullPath = IMG_RESOURCE_BASE_PATH + skinFolder + "/" + fileName;
        try {
            InputStream stream = BoardView.class.getResourceAsStream(fullPath);
            if (stream == null) {
                System.err.println("Cannot load image resource: " + fullPath);
                // Attempt to load from standard skin, if current skin is not standard and file is not found
                if (!"standart".equals(skinFolder)) {
                    System.err.println("Attempting fallback to 'standart' skin for: " + fileName);
                    return loadImageFromFile("standart", fileName);
                }
                return null;
            }
            return new Image(stream);
        } catch (Exception e) {
            System.err.println("Error loading image: " + fullPath + " - " + e.getMessage());
            return null;
        }
    }

    private static void loadAllImages() {
        IMG_BULB_OFF = loadImageFromFile(currentSkinName, "bulb_off.png");
        IMG_BULB_ON  = loadImageFromFile(currentSkinName, "bulb_on.png");
        IMG_SOURCE   = loadImageFromFile(currentSkinName, "power_src.png");
        IMG_EMPTY    = loadImageFromFile(currentSkinName, "empty_pole.png");

        WIRE_ON.clear();
        WIRE_OFF.clear();
        for (WireType t : WireType.values()) {
            WIRE_ON.put(t, loadImageFromFile(currentSkinName, t.name() + "_wire.png"));
            WIRE_OFF.put(t, loadImageFromFile(currentSkinName, t.name() + "_wire_off.png"));
        }
    }

    public static void updateSkin(String newSkinName) {
        if (newSkinName != null && !newSkinName.equals(currentSkinName) && GameSettings.isValidSkin(newSkinName)) {
            currentSkinName = newSkinName;
            loadAllImages();
        } else if (newSkinName == null || !GameSettings.isValidSkin(newSkinName)) {
            System.err.println("BoardView.updateSkin called with invalid skin name: " + newSkinName);
        }
    }

    public BoardView(Board board, GameController gc) {
        this.board = board;
        this.gameController = gc;
        this.interactive = (gc != null);

        setHgap(0);
        setVgap(0);

        int R = board.getRows(), C = board.getCols();
        cellBtns = new Button[R][C];

        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                Button btn = new Button();
                btn.getStyleClass().add("cell-btn");
                btn.setMinSize(CELL, CELL);
                btn.setPrefSize(CELL, CELL);
                btn.setMaxSize(CELL, CELL);

                btn.setGraphic(new StackPane());
                btn.setFocusTraversable(false);

                if (interactive) {
                    final int rr = r, cc = c;
                    btn.setOnAction(e -> {
                        Cell cell = board.getCell(rr, cc);
                        if (cell.getElement() != null && cell.getElement().isRotatable()) {
                            if (rotateRequestHandler != null) {
                                rotateRequestHandler.accept(rr, cc);
                            } else {
                                System.err.println("Обработчик вращения (rotateRequestHandler) не установлен в BoardView!");
                            }
                        }
                    });
                }
                cellBtns[r][c] = btn;
                add(btn, c, r);
            }
        }

        if (interactive) {
            gameController.updateConnections();
        }
        refresh();
    }

    private boolean isPowered(int r,int c){
        if (interactive) return gameController.isConnected(r,c);
        if (cached==null) cached = BoardTools.computePowered(board);
        return cached[r][c];
    }

    protected void updateButton(Button btn, Cell cell, int row, int col) {
        StackPane stack = (StackPane) btn.getGraphic();
        stack.getChildren().clear();

        Element el = cell.getElement();
        boolean powered = isPowered(row, col);

        Image imgToSet = null;
        double rot = 0;

        if (el instanceof PowerSource) {
            imgToSet = IMG_SOURCE;
            rot = el.getRotation();
        } else if (el instanceof Bulb) {
            imgToSet = powered ? IMG_BULB_ON : IMG_BULB_OFF;
            rot = el.getRotation();
        } else if (el instanceof Wire w) {
            imgToSet = powered ? WIRE_ON.get(w.getWireType()) : WIRE_OFF.get(w.getWireType());
            rot = w.getRotation();
        } else {
            imgToSet = IMG_EMPTY;
        }

        if (imgToSet != null) {
            ImageView iv = new ImageView(imgToSet);
            iv.setFitWidth(CELL + 1);
            iv.setFitHeight(CELL + 1);
            iv.setPreserveRatio(false);
            iv.setRotate(rot);
            stack.getChildren().add(iv);
        }
    }

    public void refresh() {
        cached = null;
        for (int r = 0; r < board.getRows(); r++)
            for (int c = 0; c < board.getCols(); c++)
                updateButton(cellBtns[r][c], board.getCell(r, c), r, c);
    }
}

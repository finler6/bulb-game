package lightbulb.view;

import lightbulb.controller.GameController;
import lightbulb.controller.GameHistory;
import lightbulb.controller.replay.GameReplayer;
import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.nio.file.Files;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import javafx.scene.Group;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URL;
import javafx.application.Platform;

import lightbulb.model.Board;
import lightbulb.model.Difficulty;
import lightbulb.model.LevelGenerator;
import lightbulb.model.persistence.BoardSerializer;
import lightbulb.model.LevelData;

/**
 * Třída MainWindow je hlavním vstupním bodem JavaFX aplikace.
 * Spravuje zobrazení jednotlivých scén (hlavní menu, výběr obtížnosti,
 * herní obrazovka, obrazovka přehrávání, nastavení, statistiky),
 * inicializuje hru a řídí celkový tok uživatelského rozhraní.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public class MainWindow extends Application {

    /* -------------------- fiels ------------------------------------------------ */
    private Stage primaryStage;
    private Scene scene;

    private VBox       mainMenuPane;
    private VBox       difficultyMenuPane;
    private VBox settingsPane;
    private BorderPane gamePane;
    private ScrollPane levelSelectionScrollPane;
    private TilePane levelTilePane;
    private VBox       levelChoiceRootPane;
    private HintWindow hintWin;
    private Board currentBoard;
    private BoardView  gameBoardView;

    private Timeline        clock;
    private AtomicInteger   counter;
    private Difficulty currentDiff;

    private final String lightThemePath = "/styles/light-theme.css";
    private final String darkThemePath = "/styles/dark-theme.css";

    /* -------------------- start app ------------------------------------ */
    @Override public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        buildMainMenu();  buildDifficultyMenu(); buildSettingsPane(); buildLevelSelectionPane();

        try {
            Image appIcon = new Image(getClass().getResourceAsStream("/icon.png"));
            primaryStage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.err.println("Error loading application icon: " + e.getMessage());
        }

        scene = new Scene(mainMenuPane, 720, 720);
        applyCurrentTheme();

        primaryStage.setScene(scene);
        primaryStage.setTitle("Light-Bulb Game");
        primaryStage.show();
    }

    /* ==================== M E N U ============================================ */

    private void buildMainMenu() {

        Button startBtn = new Button("Start");
        Button loadBtn = new Button("Replay log");
        Button openLvlBtn = new Button("Choose Level");
        Button settingsBtn = new Button("Settings");

        String menuButtonStyleClass = "main-menu-button";
        double preferredButtonWidth = 250;
        double preferredButtonHeight = 50;

        Button[] menuButtons = {startBtn, openLvlBtn, loadBtn, settingsBtn};

        for (Button btn : menuButtons) {
            btn.getStyleClass().add(menuButtonStyleClass);
            btn.setPrefSize(preferredButtonWidth, preferredButtonHeight);
            btn.setMaxWidth(Double.MAX_VALUE);
        }

        startBtn.setOnAction(e -> showDifficultyMenu());
        loadBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON Lines", "*.jsonl", "*.log"));

            Path logsDirectoryPath = Paths.get("src", "main", "resources", "logs");
            File logsDirectory = logsDirectoryPath.toFile();

            if (logsDirectory.exists() && logsDirectory.isDirectory()) {
                fc.setInitialDirectory(logsDirectory);
            } else {
                System.err.println("Logs directory not found at: " + logsDirectory.getAbsolutePath() +
                        ". Opening default directory.");
            }

            File f = fc.showOpenDialog(primaryStage);
            if (f != null) {
                openReplay(f.toPath());
            }
        });
        openLvlBtn.setOnAction(e -> showLevelSelectionPane());
        settingsBtn.setOnAction(e -> showSettingsPane());

        mainMenuPane = new VBox(20, menuButtons);
        mainMenuPane.setAlignment(Pos.CENTER);
        mainMenuPane.setPadding(new Insets(40));
    }

    private void buildDifficultyMenu() {
        Button easyBtn = new Button("Easy");
        Button mediumBtn = new Button("Medium");
        Button hardBtn = new Button("Hard");
        Button backBtn = new Button("Back");

        String difficultyButtonStyleClass = "main-menu-button";
        String backButtonStyleClass = "secondary-menu-button";

        double preferredButtonWidth = 250;
        double preferredButtonHeight = 50;

        Button[] difficultyButtons = {easyBtn, mediumBtn, hardBtn};
        for (Button btn : difficultyButtons) {
            btn.getStyleClass().add(difficultyButtonStyleClass);
            btn.setPrefSize(preferredButtonWidth, preferredButtonHeight);
            btn.setMaxWidth(Double.MAX_VALUE);
        }

        backBtn.getStyleClass().add(backButtonStyleClass);

        easyBtn.setOnAction(e -> startGeneratedGame(Difficulty.EASY));
        mediumBtn.setOnAction(e -> startGeneratedGame(Difficulty.MEDIUM));
        hardBtn.setOnAction(e -> startGeneratedGame(Difficulty.HARD));
        backBtn.setOnAction(e -> showMainMenu());

        VBox.setMargin(backBtn, new Insets(20, 0, 0, 0));

        difficultyMenuPane = new VBox(15, easyBtn, mediumBtn, hardBtn, backBtn);
        difficultyMenuPane.setAlignment(Pos.CENTER);
        difficultyMenuPane.setPadding(new Insets(40));
    }

    /* ----------- openReplay() with button Back ---------------- */
    private void openReplay(Path log){
        try {
            GameReplayer rep = GameReplayer.load(log);

            BoardView replayView = new BoardView(rep.getBoard(), null);
            ResponsivePane canvas = new ResponsivePane(replayView);

            BorderPane pane = new BorderPane(canvas);

            Button stepBack = new Button("◀");
            Button stepFwd  = new Button("▶");
            Button play     = new Button("Play");
            Button backMenu = new Button("Back");

            stepBack.setOnAction(e -> { if (rep.stepBack())    replayView.refresh(); });
            stepFwd .setOnAction(e -> { if (rep.stepForward()) replayView.refresh(); });

            play.setOnAction(e -> {
                rep.resumePlay();
                this.currentDiff = rep.getDifficulty();
                int timerValue = rep.getCurrentTimerValue();
                launchGame(rep.getBoard(), timerValue);
            });

            backMenu.setOnAction(e -> showMainMenu());

            HBox bar = new HBox(10, backMenu, stepBack, stepFwd, play);
            bar.setAlignment(Pos.CENTER);
            pane.setTop(bar);

            scene.setRoot(pane);
        } catch (IOException ex){
            new Alert(Alert.AlertType.ERROR,
                    "Can't load log:\n" + ex.getMessage()).showAndWait();
        }
    }

    private void handleUndo() {
        GameHistory.getInstance().undo();
        if (currentBoard != null) {
            GameController gc = new GameController(currentBoard);
            gc.updateConnections();
            if (gameBoardView != null) gameBoardView.refresh();
        }
    }

    private void handleRedo() {
        GameHistory.getInstance().redo();
        if (currentBoard != null) {
            GameController gc = new GameController(currentBoard);
            gc.updateConnections();
            if (gameBoardView != null) gameBoardView.refresh();
        }
    }

    private void handleRotateRequest(int r, int c) {
        if (currentBoard == null) return;
        int currentTime = (counter != null) ? counter.get() : 0;
        GameHistory.getInstance().doRotate(currentBoard, r, c, currentTime);
    }


    /* -------------------- Generate new game ----------------------------- */

    private void startGeneratedGame(Difficulty diff){
        LevelGenerator gen = new LevelGenerator();
        Board board = gen.generate(
                diff.rows, diff.cols, diff.bulbs, diff.extraEdges, diff.shuffleTurns);
        currentDiff = diff;
        launchGame(board);
    }

    /* -------------------- Launch game ----------------------------------- */

    private void launchGame(Board board, int initialTimerValue){
        /* stop old tamer */
        if (clock != null) clock.stop();
        currentBoard = board;

        /* log */
        GameHistory.reset();
        GameHistory.getInstance().snapshot(currentBoard, currentDiff);

        /* MVC */
        GameController gc  = new GameController(board);
        this.gameBoardView = new BoardView(currentBoard, gc);
        this.gameBoardView.setOnRotateRequest((r, c) -> {
            handleRotateRequest(r, c);

            if (gc != null) {
                gc.updateConnections();
            } else {
                System.err.println("gc is null в лямбде обработчика вращения!");
            }

            this.gameBoardView.refresh();

            if (gc != null && gc.isGameWon() && this.gameBoardView.getOnWin() != null) {
                this.gameBoardView.getOnWin().run();
            }
        });
        ResponsivePane canvas = new ResponsivePane(this.gameBoardView);

        /* toolbar */
        Button giveUp  = new Button("Give Up");
        Button hintsBtn= new Button("Hints");
        Button saveBtn = new Button("Save level");
        Label  timeLbl = new Label();

        HBox toolbar = new HBox(10, giveUp, hintsBtn, saveBtn, timeLbl);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10));

        /* --------- Save level --------- */
        saveBtn.setOnAction(e -> {
            try {
                Path mapsDir = Paths.get("src", "main", "resources", "maps");
                Files.createDirectories(mapsDir);

                int nextLevelNumber = 1;
                Pattern levelPattern = Pattern.compile("level (\\d+)\\.json", Pattern.CASE_INSENSITIVE);

                if (Files.exists(mapsDir) && Files.isDirectory(mapsDir)) {
                    try (var stream = Files.list(mapsDir)) {
                        nextLevelNumber = stream
                                .map(path -> path.getFileName().toString())
                                .map(levelPattern::matcher)
                                .filter(Matcher::matches)
                                .map(matcher -> Integer.parseInt(matcher.group(1)))
                                .max(Integer::compareTo)
                                .map(max -> max + 1)
                                .orElse(1);
                    } catch (IOException listEx) {
                        System.err.println("Error listing maps directory: " + listEx.getMessage());
                        nextLevelNumber = 1;
                    }
                }

                String newFileName = "Level " + nextLevelNumber + ".json";
                Path newFilePath = mapsDir.resolve(newFileName);

                Files.writeString(newFilePath, BoardSerializer.toJson(currentBoard, currentDiff));
                showToast("Level saved as: " + newFileName);

            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR, "Can't save level:\n" + ex.getMessage()).showAndWait();
                ex.printStackTrace();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "An unexpected error occurred while saving the level:\n" + ex.getMessage()).showAndWait();
                ex.printStackTrace();
            }
        });


        /* ---- Hints ---- */
        hintWin = new HintWindow(board, gc);
        hintWin.initOwner(primaryStage);
        hintsBtn.setOnAction(e -> {
            if (hintWin.isShowing()) hintWin.toFront(); else hintWin.show();
        });

        /* ---- timer ---- */
        int limit = currentDiff.timeLimitSec;
        counter = new AtomicInteger(initialTimerValue);
        updateTimeLabel(timeLbl, counter.get(), limit);

        clock = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            int val;
            if (limit == 0) {
                val = counter.incrementAndGet();
            } else {
                val = counter.updateAndGet(x -> (x > 0) ? x - 1 : 0);
            }
            updateTimeLabel(timeLbl, val, limit);

            if (limit > 0 && val == 0) {
                showEndScreen(false, limit);
            }
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        /* ---- Give Up button ---- */
        giveUp.setOnAction(e -> {
            int currentCounterValue = counter.get();
            int elapsed = (limit == 0) ? currentCounterValue : (limit - currentCounterValue);
            showEndScreen(false, elapsed);
        });

        /* ---- Win ---- */
        this.gameBoardView.setOnWin(() -> {
            int currentCounterValue = counter.get();
            int elapsed = (limit == 0) ? currentCounterValue : (limit - currentCounterValue);
            showEndScreen(true, elapsed);
        });

        gamePane = new BorderPane(canvas);
        gamePane.setTop(toolbar);
        scene.setRoot(gamePane);

        String[] undoChars = {"z", "я"};
        String[] redoChars = {"y", "н"};
        String[] saveChars = {"s", "ы"};

        for (String ch : undoChars)
            scene.getAccelerators().put(
                    new KeyCharacterCombination(ch, KeyCombination.SHORTCUT_DOWN),
                    this::handleUndo);

        for (String ch : redoChars){
            scene.getAccelerators().put(
                    new KeyCharacterCombination(ch, KeyCombination.SHORTCUT_DOWN),
                    this::handleRedo);
            scene.getAccelerators().put(
                    new KeyCharacterCombination(ch, KeyCombination.SHORTCUT_DOWN,
                            KeyCombination.SHIFT_DOWN),
                    this::handleRedo);
        }

        for (String ch : saveChars)
            scene.getAccelerators().put(
                    new KeyCharacterCombination(ch, KeyCombination.SHORTCUT_DOWN),
                    saveBtn::fire);

    }

    private void launchGame(Board board) {
        if (this.currentDiff == null) {
            System.err.println("Warning: currentDiff is null when calling launchGame(board). Defaulting to MEDIUM.");
            this.currentDiff = Difficulty.MEDIUM;
        }
        int limit = currentDiff.timeLimitSec;
        launchGame(board, limit == 0 ? 0 : limit);
    }

    private List<Path> discoverLevels() {
        List<Path> levelFiles = new ArrayList<>();
        String mapsResourcePath = "maps";

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = getClass().getClassLoader();
            }

            URL mapsUrl = null;
            if (classLoader != null) {
                mapsUrl = classLoader.getResource(mapsResourcePath);
            }

            if (mapsUrl == null) {
                mapsUrl = getClass().getResource("/" + mapsResourcePath);
            }

            if (mapsUrl == null) {
                System.err.println("FATAL ERROR: Resource directory '/" + mapsResourcePath + "' not found in classpath!");
                System.err.println("Please ensure 'src/main/resources/" + mapsResourcePath + "' exists and is included in the build.");
                Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "Cannot find levels directory (resources/" + mapsResourcePath + ").\nPlease check application resources.").showAndWait();
                });
                return Collections.emptyList();
            }

            URI uri = mapsUrl.toURI();
            Path myPath;
            FileSystem fileSystem = null;

            try {
                if (uri.getScheme().equals("jar")) {
                    try {
                        myPath = Paths.get(uri);
                    } catch (java.nio.file.FileSystemNotFoundException e) {
                        fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                        myPath = fileSystem.getPath(mapsResourcePath);
                    }
                } else {
                    myPath = Paths.get(uri);
                }

                if (!Files.exists(myPath) || !Files.isDirectory(myPath)) {
                    System.err.println("ERROR: Path found but is not a directory or doesn't exist: " + myPath);
                    return Collections.emptyList();
                }

                levelFiles = walkAndSortLevels(myPath);

            } finally {

            }

        } catch (IOException | URISyntaxException e) {
            System.err.println("Error discovering levels: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error during level discovery: " + e.getMessage());
            e.printStackTrace();
        }
        return levelFiles;
    }

    private List<Path> walkAndSortLevels(Path directoryPath) throws IOException {
        Pattern sortPattern = Pattern.compile("level (\\d+)\\.json", Pattern.CASE_INSENSITIVE);
        try (var stream = Files.walk(directoryPath, 1)) {
            return stream
                    .filter(path -> !Files.isDirectory(path))
                    .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                    .sorted(Comparator.comparingInt(path -> {
                        Matcher m = sortPattern.matcher(path.getFileName().toString());
                        if (m.matches()) {
                            try {
                                return Integer.parseInt(m.group(1));
                            } catch (NumberFormatException nfe) {
                                return Integer.MAX_VALUE;
                            }
                        }
                        return Integer.MAX_VALUE;
                    }))
                    .collect(Collectors.toList());
        }
    }

    private void buildLevelSelectionPane() {
        this.levelTilePane = new TilePane();
        this.levelTilePane.setPadding(new Insets(15));
        this.levelTilePane.setHgap(15);
        this.levelTilePane.setVgap(15);
        this.levelTilePane.setPrefColumns(3);

        this.levelSelectionScrollPane = new ScrollPane(this.levelTilePane);
        this.levelSelectionScrollPane.setFitToWidth(true);
        this.levelSelectionScrollPane.getStyleClass().add("edge-to-edge");

        Label title = new Label("Choose Level");
        title.getStyleClass().add("title-label");
        VBox.setMargin(title, new Insets(10, 0, 20, 0));

        Button backBtn = new Button("Back to Main Menu");
        backBtn.getStyleClass().add("secondary-menu-button");
        backBtn.setOnAction(e -> showMainMenu());
        VBox.setMargin(backBtn, new Insets(20, 0, 10, 0));

        VBox rootLayout = new VBox(title, this.levelSelectionScrollPane, backBtn);
        rootLayout.setAlignment(Pos.TOP_CENTER);
        rootLayout.setPadding(new Insets(10));
        rootLayout.getStyleClass().add("vbox-panel");

        this.levelChoiceRootPane = rootLayout;
    }

    private void populateLevelTiles() {
        if (levelTilePane == null) return;
        levelTilePane.getChildren().clear();

        List<Path> levelFiles = discoverLevels();
        int levelCounterForFallback = 1;

        for (Path levelPath : levelFiles) {
            try {
                String levelJson = Files.readString(levelPath);
                LevelData levelData = BoardSerializer.fromJson(levelJson);
                Board board = levelData.board();
                Difficulty loadedDifficulty = levelData.difficulty();

                // --- Creating a preview ---
                BoardView previewBoardView = new BoardView(board, null);
                previewBoardView.setMouseTransparent(true);
                double previewSize = 160;
                double originalWidth = board.getCols() * BoardView.CELL;
                double originalHeight = board.getRows() * BoardView.CELL;
                StackPane previewContainer = new StackPane();
                previewContainer.setPrefSize(previewSize, previewSize);
                previewContainer.setMaxSize(previewSize, previewSize);
                previewContainer.setMinSize(previewSize, previewSize);
                previewContainer.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px;");
                Group boardGroup = new Group(previewBoardView);
                double scaleX = previewSize / originalWidth;
                double scaleY = previewSize / originalHeight;
                double scale = Math.min(scaleX, scaleY);
                boardGroup.setScaleX(scale);
                boardGroup.setScaleY(scale);
                previewContainer.getChildren().add(boardGroup);
                StackPane.setAlignment(boardGroup, Pos.CENTER);


                // --- Level name ---
                String fileName = levelPath.getFileName().toString();
                String displayName;
                Pattern displayPattern = Pattern.compile("(Level \\d+)\\.json", Pattern.CASE_INSENSITIVE);
                Matcher matcher = displayPattern.matcher(fileName);
                if (matcher.matches()) {
                    displayName = matcher.group(1);
                } else {
                    if (fileName.toLowerCase().endsWith(".json")) {
                        displayName = fileName.substring(0, fileName.lastIndexOf('.'));
                    } else {
                        displayName = fileName;
                    }
                }
                Label nameLabel = new Label(displayName);
                nameLabel.setStyle("-fx-font-size: 14px; -fx-padding: 5px 0 0 0;");


                // --- Tile level ---
                VBox levelTile = new VBox(5, previewContainer, nameLabel);
                levelTile.setAlignment(Pos.CENTER);
                levelTile.setPadding(new Insets(10));
                levelTile.getStyleClass().add("level-tile");

                // Click handler
                levelTile.setOnMouseClicked(event -> {
                    this.currentDiff = loadedDifficulty;
                    launchGame(board);
                });

                levelTilePane.getChildren().add(levelTile);

            } catch (IOException e) {
                System.err.println("Error loading level for preview: " + levelPath + " - " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Unexpected error processing level " + levelPath + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private void showLevelSelectionPane() {
        if (this.levelChoiceRootPane == null) {
            buildLevelSelectionPane();
        }

        if (this.levelTilePane == null) {
            System.err.println("Error: levelTilePane is null in showLevelSelectionPane. Rebuilding.");
            buildLevelSelectionPane();
        }

        populateLevelTiles();

        scene.setRoot(this.levelChoiceRootPane);
    }

    private void updateTimeLabel(Label lbl,int val,int limit){
        lbl.setText(String.format("%02d:%02d", val/60, val%60));
        if (limit>0 && val<=10) lbl.setStyle("-fx-text-fill:red;");
        else                    lbl.setStyle("");
    }

    private void showToast(String msg){
        Label lbl = new Label(msg);
        lbl.setStyle("""
        -fx-background-color: rgba(50,50,50,0.9);
        -fx-text-fill: white;
        -fx-padding: 6 12 6 12;
        -fx-background-radius: 6;
        -fx-font-size: 13px;
    """);

        Popup toast = new Popup();
        toast.getContent().add(lbl);
        toast.setAutoFix(true);
        toast.setAutoHide(true);

        toast.show(primaryStage);
        toast.setX(primaryStage.getX()
                + (primaryStage.getWidth()-lbl.getWidth())/2);
        toast.setY(primaryStage.getY()
                + primaryStage.getHeight() - 80);

        FadeTransition ft = new FadeTransition(Duration.seconds(1.5), lbl);
        ft.setFromValue(1.0);  ft.setToValue(0.0);
        ft.setOnFinished(e -> toast.hide());
        ft.play();
    }

    private void showMainMenu() {
        if (clock != null) clock.stop();
        if (hintWin != null) hintWin.hide();
        GameHistory.getInstance().close();
        scene.setRoot(mainMenuPane);
    }

    private void showDifficultyMenu() { scene.setRoot(difficultyMenuPane); }

    private void buildSettingsPane() {
        Label titleLabel = new Label("Settings");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label skinLabel = new Label("Tile Skin:");
        ComboBox<String> skinComboBox = new ComboBox<>();
        skinComboBox.getItems().addAll(GameSettings.getAvailableSkins());
        skinComboBox.setValue(GameSettings.getCurrentSkinPath());

        skinComboBox.setOnAction(e -> {
            String selectedSkin = skinComboBox.getValue();
            GameSettings.setCurrentSkinPath(selectedSkin);
            BoardView.updateSkin(selectedSkin);

            if (gameBoardView != null && scene.getRoot() == gamePane) {
                gameBoardView.refresh();
            }
            if (hintWin != null && hintWin.isShowing()) {
                if (hintWin.getScene().getRoot() instanceof BoardView) {
                    ((BoardView) hintWin.getScene().getRoot()).refresh();
                }
            }

            showToast("Skin changed to: " + selectedSkin);
        });

        Label themeLabel = new Label("Theme:");
        RadioButton lightThemeRadio = new RadioButton("Light");
        lightThemeRadio.setUserData(GameSettings.Theme.LIGHT);
        RadioButton darkThemeRadio = new RadioButton("Dark");
        darkThemeRadio.setUserData(GameSettings.Theme.DARK);

        ToggleGroup themeToggleGroup = new ToggleGroup();
        lightThemeRadio.setToggleGroup(themeToggleGroup);
        darkThemeRadio.setToggleGroup(themeToggleGroup);

        if (GameSettings.getCurrentTheme() == GameSettings.Theme.DARK) {
            darkThemeRadio.setSelected(true);
        } else {
            lightThemeRadio.setSelected(true);
        }

        themeToggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle != null) {
                GameSettings.Theme selectedTheme = (GameSettings.Theme) newToggle.getUserData();
                GameSettings.setCurrentTheme(selectedTheme);
                applyCurrentTheme();
                showToast("Theme changed to: " + selectedTheme.name().toLowerCase());
            }
        });

        HBox themeBox = new HBox(10, lightThemeRadio, darkThemeRadio);
        themeBox.setAlignment(Pos.CENTER);

        Button backBtn = new Button("Back to Main Menu");
        backBtn.setOnAction(e -> showMainMenu());

        VBox layout = new VBox(15, titleLabel, skinLabel, skinComboBox, themeLabel, themeBox, backBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        settingsPane = layout;
    }

    private void applyCurrentTheme() {
        if (scene == null) return;
        scene.getStylesheets().clear();
        if (GameSettings.getCurrentTheme() == GameSettings.Theme.DARK) {
            scene.getStylesheets().add(getClass().getResource(darkThemePath).toExternalForm());
        } else {
            scene.getStylesheets().add(getClass().getResource(lightThemePath).toExternalForm());
        }
    }

    private void showSettingsPane() {
        scene.setRoot(settingsPane);
    }

    private void showEndScreen(boolean victory, int elapsed) {
        if (clock != null) clock.stop();
        if (hintWin != null) hintWin.hide();

        Label title = new Label(victory ? "You Win!" : "Game Over");
        title.setStyle("-fx-font-size: 36px;");

        Label time = new Label("Time: " +
                String.format("%02d:%02d", elapsed/60, elapsed%60));

        Button stats = new Button("Stats");
        stats.setOnAction(e -> new StatsWindow(currentBoard).show());

        Button toMenu = new Button("Back to Menu");
        toMenu.setOnAction(e -> showMainMenu());

        VBox pane = new VBox(20, title, time, stats, toMenu);
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(40));
        scene.setRoot(pane);
    }

    /* ------------------------------------------------------------------------- */
    public static void main(String[] args) { launch(args); }
}

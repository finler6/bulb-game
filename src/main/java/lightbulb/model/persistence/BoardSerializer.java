package lightbulb.model.persistence;

import com.google.gson.*;

import lightbulb.model.*;

/**
 * Třída RotateCommand reprezentuje konkrétní příkaz pro akci otočení
 * herního prvku v buňce. Umožňuje vykonání a zrušení této akce,
 * což je využíváno pro funkcionalitu undo/redo a logování.
 * Implementuje rozhraní Command.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public final class BoardSerializer {

    /* ---------- serialization ---------- */
    public static String toJson(Board b, Difficulty difficulty) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "init");
        root.addProperty("rows", b.getRows());
        root.addProperty("cols", b.getCols());

        if (difficulty != null) {
            root.addProperty("difficulty", difficulty.name());
        } else {
            root.addProperty("difficulty", Difficulty.MEDIUM.name());
            System.err.println("Warning: toJson called without difficulty, defaulting to MEDIUM.");
        }

        JsonArray rows = new JsonArray();
        for (int r = 0; r < b.getRows(); r++) {
            JsonArray row = new JsonArray();
            for (int c = 0; c < b.getCols(); c++) {
                Cell cell = b.getCell(r, c);
                JsonObject jc = new JsonObject();

                Element el = cell.getElement();
                if (el == null) {
                    jc.addProperty("el", "EMPTY");
                } else if (el instanceof Wire w) {
                    jc.addProperty("el", "Wire_" + w.getWireType());
                } else {
                    jc.addProperty("el", el.getClass().getSimpleName());
                }
                jc.addProperty("rot", cell.getRotation());
                row.add(jc);
            }
            rows.add(row);
        }
        root.add("cells", rows);
        return root.toString();
    }

    /* ---------- deserialization (on playback) ---------- */
    public static LevelData fromJson(String json) {
        JsonObject obj  = JsonParser.parseString(json).getAsJsonObject();
        int rows = obj.get("rows").getAsInt();
        int cols = obj.get("cols").getAsInt();

        Board board = new Board(rows, cols);
        JsonArray cells = obj.getAsJsonArray("cells");

        for (int r = 0; r < rows; r++) {
            JsonArray row = cells.get(r).getAsJsonArray();
            for (int c = 0; c < cols; c++) {
                JsonObject jc = row.get(c).getAsJsonObject();
                String token = jc.get("el").getAsString();
                int    rot   = jc.get("rot").getAsInt();
                board.placeElement(r, c, token);
                board.getCell(r, c).setRotation(rot);
            }
        }

        Difficulty difficulty = Difficulty.MEDIUM;
        if (obj.has("difficulty")) {
            String diffName = obj.get("difficulty").getAsString();
            try {
                difficulty = Difficulty.valueOf(diffName.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Unknown difficulty '" + diffName + "' in JSON. Using MEDIUM.");
                difficulty = Difficulty.MEDIUM;
            }
        } else {
            System.err.println("Warning: Difficulty field not found in JSON. Using MEDIUM.");
            difficulty = Difficulty.MEDIUM;
        }

        return new LevelData(board, difficulty);
    }

    private BoardSerializer() {}
}

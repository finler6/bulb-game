package lightbulb.view;

import java.util.prefs.Preferences;

/**
 * Třída GameSettings spravuje a perzistentně ukládá uživatelská nastavení hry,
 * jako jsou vybraný skin pro herní prvky a téma vzhledu aplikace (světlé/tmavé).
 * Pro ukládání využívá Java Preferences API.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public class GameSettings {
    // --- Skin Settings ---
    private static final String SKIN_PREF_KEY = "gameSkin";
    private static final String DEFAULT_SKIN = "standart";
    private static String currentSkinPath = loadSkinPreference();

    // --- Theme Settings ---
    public enum Theme { LIGHT, DARK }
    private static final String THEME_PREF_KEY = "gameTheme";
    private static final Theme DEFAULT_THEME = Theme.LIGHT;
    private static Theme currentTheme = loadThemePreference();

    public static String[] getAvailableSkins() {
        return new String[]{"standart", "oil"};
    }

    /**
     * Returns the folder name of the currently selected skin.
     * @return skin folder name
     */
    public static String getCurrentSkinPath() {
        if (currentSkinPath == null || currentSkinPath.isEmpty()) {
            return DEFAULT_SKIN;
        }
        return currentSkinPath;
    }

    /**
     * Returns the currently selected theme.
     * @return Theme (LIGHT or DARK)
     */
    public static Theme getCurrentTheme() {
        return currentTheme;
    }


    /**
     * Sets the new skin and saves the selection.
     * @param skinPath is the folder name of the new skin
     */
    public static void setCurrentSkinPath(String skinPath) {
        if (skinPath != null && !skinPath.isEmpty() && isValidSkin(skinPath)) {
            GameSettings.currentSkinPath = skinPath;
            saveSkinPreference(skinPath);
        } else {
            GameSettings.currentSkinPath = DEFAULT_SKIN;
            saveSkinPreference(DEFAULT_SKIN);
            System.err.println("Attempted to set invalid skin: " + skinPath + ". Reverted to default.");
        }
    }

    /**
     * Sets a new theme and saves the selection.
     * @param theme new theme
     */
    public static void setCurrentTheme(Theme theme) {
        if (theme != null) {
            GameSettings.currentTheme = theme;
            saveThemePreference(theme);
        }
    }

    /**
     * Checks if such a skin exists (based on the list for now).
     * @param skinPath skin folder name
     * @return true if the skin is valid
     */
    public static boolean isValidSkin(String skinPath) {
        for (String availableSkin : getAvailableSkins()) {
            if (availableSkin.equals(skinPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads a saved theme from Preferences.
     * @return Theme or default value
     */
    private static Theme loadThemePreference() {
        Preferences prefs = Preferences.userNodeForPackage(GameSettings.class);
        String themeName = prefs.get(THEME_PREF_KEY, DEFAULT_THEME.name());
        try {
            return Theme.valueOf(themeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DEFAULT_THEME;
        }
    }

    /**
     * Saves the current theme in Preferences.
     * @param theme theme to save
     */
    private static void saveThemePreference(Theme theme) {
        Preferences prefs = Preferences.userNodeForPackage(GameSettings.class);
        prefs.put(THEME_PREF_KEY, theme.name());
    }

    /**
     * Loads the saved skin name from Preferences.
     * @return skin name or default value
     */
    private static String loadSkinPreference() {
        Preferences prefs = Preferences.userNodeForPackage(GameSettings.class);
        return prefs.get(SKIN_PREF_KEY, DEFAULT_SKIN);
    }

    /**
     * Saves the name of the current skin in Preferences.
     * @param skinPath name of the skin to save
     */
    private static void saveSkinPreference(String skinPath) {
        Preferences prefs = Preferences.userNodeForPackage(GameSettings.class);
        prefs.put(SKIN_PREF_KEY, skinPath);
    }
}
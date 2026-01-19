package silentconvent;

import java.io.*;
import javax.swing.*;

/**
 * SaveManager handles all game saving and loading functionality.
 * It creates SaveData snapshots of the current game state and restores them
 * later.
 * 
 * Key features:
 * - Save games to disk with automatic file naming
 * - Load saved games and restore state
 * - List all available saves
 * - Detect "broken memory" (narrative story mechanic)
 * 
 * Save files are stored in the "saves/" folder with .sav extension.
 */
public class SaveManager {

    /**
     * Flag: is the memory "broken"? (narrative mechanic - player forgets the story)
     */
    private static boolean broken = false;

    /** Directory where save files are stored */
    private static final String SAVE_DIR = "saves";

    /**
     * Save the current game snapshot using the requested filename.
     * If a file with that name already exists, creates a numbered suffix (name(n))
     * to avoid overwriting.
     * Returns the final saved filename (without extension) on success or null on
     * failure.
     * 
     * @param filename The name for the save file (without extension)
     * @return The actual filename used (with numbering if needed), or null if save
     *         failed
     */
    public static String saveGame(String filename) {
        try {
            // Create saves folder if it doesn't exist
            File dir = new File(SAVE_DIR);
            if (!dir.exists())
                dir.mkdirs();

            // Create a snapshot of the current game state
            SaveData sd = snapshotCurrent();
            if (sd == null)
                return null;

            // Sanitize filename (remove whitespace, check if empty)
            String base = filename.trim();
            if (base.isEmpty())
                return null;

            // Try to create the save file, numbering if file already exists
            // Example: "mysave.sav", "mysave(1).sav", "mysave(2).sav", etc.
            File out = new File(dir, base + ".sav");
            int attempt = 1;
            while (out.exists()) {
                String trial = String.format("%s(%d)", base, attempt);
                out = new File(dir, trial + ".sav");
                attempt++;
            }

            // Write the SaveData object to disk
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(out))) {
                oos.writeObject(sd);
            }
            System.out.println("Saved game to " + out.getAbsolutePath());
            String savedName = out.getName().replaceAll("\\.sav$", ""); // Remove .sav extension
            return savedName;
        } catch (Exception e) {
            System.out.println("Failed to save game: " + e.getMessage());
            return null;
        }
    }

    /**
     * Load a SaveData object from disk and restore the game state.
     * If memory is broken (narrative mechanic), loading is blocked and a glitch
     * effect plays.
     * 
     * @param filename The name of the save file (without .sav extension)
     * @return True if load succeeded, false if it failed
     */
    public static boolean loadGame(String filename) {
        try {
            // If memory is broken (story mechanic), prevent loading
            if (broken) {
                // Narrative reaction: loading is forbidden / corrupted
                // Show glitch effect and warning message
                GameFrame.triggerRememberGlitch();
                return false;
            }

            // Check if the save file exists
            File file = new File(SAVE_DIR, filename + ".sav");
            if (!file.exists()) {
                JOptionPane.showMessageDialog(null, "Save not found.", "Load", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Read the SaveData object from disk
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                SaveData sd = (SaveData) ois.readObject();
                // Restore the game state from the SaveData
                restoreFromSave(sd);
            }
            return true;
        } catch (Exception e) {
            System.out.println("Failed to load game: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get a list of all available save files.
     * Returns the filenames without the .sav extension.
     * 
     * @return Array of save filenames, or empty array if no saves exist
     */
    public static String[] listSaves() {
        File dir = new File(SAVE_DIR);
        if (!dir.exists())
            return new String[0];
        // List all .sav files in the saves directory
        String[] list = dir.list((d, name) -> name.endsWith(".sav"));
        if (list == null)
            return new String[0];
        // Remove .sav extension from filenames
        for (int i = 0; i < list.length; i++) {
            list[i] = list[i].replaceAll("\\.sav$", "");
        }
        return list;
    }

    /**
     * Create a snapshot of the current game state.
     * Captures: current scene, dialogue position, background, music track.
     * 
     * @return A SaveData object representing the current state, or null if no scene
     *         is loaded
     */
    private static SaveData snapshotCurrent() {
        try {
            Scene current = SceneManager.getCurrent();
            if (current == null)
                return null;

            // Create a SaveData object with current state
            SaveData sd = new SaveData();
            sd.sceneClass = current.getClass().getName(); // Store which scene we're in
            sd.index = current.index; // Store dialogue position
            sd.day = current.getDay(); // Store day number

            // Store background image path if one is loaded
            if (GameFrame.background.getIcon() instanceof ImageIcon) {
                ImageIcon ic = (ImageIcon) GameFrame.background.getIcon();
                sd.backgroundPath = ic.getDescription(); // The path is stored in the description
            }

            // Store music information (path, whether it loops, local multiplier)
            sd.musicPath = AudioPlayer.getCurrentTrackPath();
            sd.musicLoop = AudioPlayer.getCurrentLoop();
            sd.musicLocalMultiplier = AudioPlayer.getCurrentLocalMultiplier();

            return sd;
        } catch (Exception e) {
            System.out.println("Failed to create snapshot: " + e.getMessage());
            return null;
        }
    }

    /**
     * Restore the game state from a SaveData object.
     * Recreates the scene, restores dialogue position, background, and music.
     * 
     * @param sd The SaveData object containing the state to restore
     */
    private static void restoreFromSave(SaveData sd) {
        if (sd == null)
            return;
        try {
            // Recreate the scene using reflection (Class.forName)
            // This allows us to save and load any scene without knowing them in advance
            Class<?> cls = Class.forName(sd.sceneClass);
            Scene s = (Scene) cls.getDeclaredConstructor().newInstance();

            // Load the scene and restore the dialogue position
            SceneManager.load(s);
            s.restoreIndex(sd.index); // Jump to the saved position in the dialogue

            // Restore background image (if one was saved)
            if (sd.backgroundPath != null) {
                GameFrame.setBackgroundImage(sd.backgroundPath);
            }

            // Note: Music is commented out - could be re-enabled for full state restoration
            // if (sd.musicPath != null) {
            // AudioPlayer.play(sd.musicPath, sd.musicLoop, sd.musicLocalMultiplier);
            // } else {
            // AudioPlayer.stop();
            // }
        } catch (Exception e) {
            System.out.println("Failed to restore save: " + e.getMessage());
        }
    }

    /**
     * Break the memory (story mechanic).
     * When broken, players cannot load saved games - attempting to load triggers a
     * glitch effect.
     * This is part of the game's narrative about forgetting/memory loss.
     */
    public static void breakMemory() {
        broken = true;
        System.out.println("Memory has been broken (narrative).");
    }

    /**
     * Check if memory is currently broken.
     * 
     * @return True if memory is broken, false otherwise
     */
    public static boolean isBroken() {
        return broken;
    }
}

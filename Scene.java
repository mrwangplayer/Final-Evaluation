package silentconvent;

/**
 * Abstract base class for all game scenes in Silent Convent.
 * Each day and story segment is represented as a Scene.
 * Scenes manage the dialogue flow, character positioning, and progression
 * logic.
 * 
 * Subclasses include: DayOneScene, DayTwoScene, FinalScene, etc.
 */
public abstract class Scene {

    /**
     * Tracks the current position/step in the scene (which dialogue line is
     * showing)
     */
    protected int index = 0;

    /**
     * Called when this scene becomes active (when transitioning to it).
     * Resets the internal index to 0 and clears any characters from the previous
     * scene.
     * Then calls the scene-specific start() method to initialize the scene.
     * 
     * Usage: SceneManager.load() → scene.enter() → scene.start()
     */
    public void enter() {
        // Reset to the beginning of this scene
        index = 0;
        // Clear any residual character sprites from the previous scene so they don't
        // persist
        // This prevents characters from appearing in multiple scenes
        GameFrame.clearCharacters();
        // Let the specific scene implement its own startup logic
        start();
    }

    /**
     * Scene-specific initialization. Subclasses must override this.
     * This is where each scene sets up its dialogue, shows initial characters,
     * plays background music, etc.
     */
    public abstract void start();

    /**
     * Move to the next step in the scene. Called when player clicks "Next" button.
     * Subclasses must override this to implement the scene's progression logic.
     * Examples:
     * - Show next dialogue line
     * - Move to next day/scene
     * - Show character reactions
     * - Play sound effects
     */
    public abstract void next();

    /**
     * Restore the scene to a previously saved state and update the displayed text.
     * Used when loading a saved game.
     * 
     * @param idx The saved index position to restore to
     */
    public void restoreIndex(int idx) {
        // Set the scene back to the saved position
        this.index = idx;
        // Ask the subclass to update the displayed text for this index
        updateDisplay();
    }

    /**
     * Update the GameFrame's display to show content at the current index.
     * Subclasses should override this to update dialogue and characters based on
     * index.
     * Example: if index=3, show the 3rd dialogue line and corresponding character
     * poses.
     * 
     * This is used when loading a saved game to restore visual state.
     */
    public void updateDisplay() {
        // Default empty implementation - subclasses override if they need this
    }

    /**
     * Return which day this scene belongs to (used for tracking progress).
     * Example: DayOneScene returns 1, DayTwoScene returns 2, etc.
     * 
     * @return The day number (default is 0 for scenes not part of a specific day)
     */
    public int getDay() {
        return 0;
    }
}

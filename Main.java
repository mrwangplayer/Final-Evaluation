  package silentconvent;

/**
 * Main entry point for the Silent Convent game application.
 * This class starts the program and displays the main menu.
 */
public class Main {

    /**
     * The main method - called when the program runs.
     * It opens the MainMenu dialog which allows the player to:
     * - Play (start a new game)
     * - Load a saved game
     * - View the full story (unlocked after first playtrough)
     * 
     * @param args Command line arguments (not used in this application)
     */
    public static void main(String[] args) {
        // Show the main menu first; Play will start the SceneManager and the game loop
        MainMenu.open();
    }
}

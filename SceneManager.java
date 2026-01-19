package silentconvent;

import scenes.*;

/**
 * SceneManager controls the flow of the game - which scene is currently active,
 * transitions between scenes, and player interactions with buttons.
 * 
 * It acts as the "conductor" of the story, managing:
 * - Current active scene
 * - Button listeners (Next, Remember, Main Menu)
 * - Fade transitions between days
 * - Save/load game state
 * - Unlocking the full story
 */
public class SceneManager {

    /** The scene currently being displayed */
    private static Scene current;

    /** Flag: are we waiting for player to press Next to reveal the final scene? */
    private static boolean awaitingFinalAdvance = false;

    /** The final scene waiting to be revealed */
    private static Scene finalPending = null;

    /** Flag: has the game started (Play button pressed) */
    private static boolean started = false;

    /** Flag: has the player unlocked the full story view? */
    private static boolean storyUnlocked = false;

    /** Flag: have button listeners been initialized? */
    private static boolean buttonsInitialized = false;

    /**
     * Start the game - create the game frame and load Day One.
     * Called when player clicks "Play" in the main menu.
     * Only runs once (ignored if called multiple times).
     */
    public static void start() {
        if (started)
            return;
        started = true;
        // Create & show the GameFrame only when the game actually starts (Play pressed)
        GameFrame.ensureCreated();
        GameFrame.getInstance().setVisible(true);
        initializeButtonListeners();
        load(new DayOneScene());
    }

    /**
     * Ensure button listeners are set up.
     * Used when loading a saved game from the menu.
     */
    public static void ensureButtonsInitialized() {
        if (!buttonsInitialized) {
            started = true;
            initializeButtonListeners();
        }
    }

    /**
     * Initialize button click listeners for Next, Remember, and Main Menu buttons.
     * This sets up what happens when the player clicks these buttons.
     */
    private static void initializeButtonListeners() {
        if (buttonsInitialized)
            return;
        buttonsInitialized = true;

        // ===== NEXT BUTTON LISTENER =====
        // Called when player clicks "Next" to advance dialogue/story
        GameFrame.nextButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // Disable button for 300ms to prevent rapid spamming
                // Rapid clicks can confuse game state or trigger multiple actions
                GameFrame.nextButton.setEnabled(false);
                new javax.swing.Timer(300, new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent ev) {
                        ((javax.swing.Timer) ev.getSource()).stop();
                        GameFrame.nextButton.setEnabled(true);
                    }
                }).start();

                // If we're waiting for final advance, reveal the final scene
                // Otherwise, advance the current scene
                if (awaitingFinalAdvance) {
                    revealFinal();
                } else if (current != null) {
                    current.next();
                }
            }
        });

        // ===== REMEMBER BUTTON LISTENER =====
        // Called when player clicks "Remember" to open save/load menu
        // (Note: broken memory will still allow the menu but loading will be blocked)
        GameFrame.rememberButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                SaveMenu.open();
            }
        });
    }

    /**
     * Get the currently active scene.
     * 
     * @return The current Scene object
     */
    public static Scene getCurrent() {
        return current;
    }

    /**
     * Unlock the full story view in the main menu.
     * Called when the game reaches a certain point in the story.
     */
    public static void unlockStory() {
        storyUnlocked = true;
    }

    /**
     * Check if the full story has been unlocked.
     * 
     * @return True if story is unlocked, false otherwise
     */
    public static boolean isStoryUnlocked() {
        return storyUnlocked;
    }

    /**
     * Transition from current scene to next scene with a fade effect.
     * Shows "Day X" text in the middle of the screen during transition.
     * 
     * Transition behavior:
     * - Fade to black
     * - Display "Day X" message
     * - Load the new scene
     * - Fade back in
     * 
     * @param nextScene The scene to transition to
     * @param dayNumber The day number to display
     */
    public static void transitionTo(Scene nextScene, int dayNumber) {
        // Choose shorter fades if staying within the same day (for speed)
        // Longer fades between different days (for atmosphere)
        boolean sameDay = (current != null && nextScene != null && current.getDay() == nextScene.getDay());
        int fadeMs = sameDay ? 350 : 700; // milliseconds for fade animation

        // Optionally duck (lower) master volume a bit during short intra-day fades
        float originalVolume = AudioPlayer.getVolume();
        float ducked = sameDay ? Math.max(0f, originalVolume * 0.8f) : originalVolume;

        // Start fade to black
        GameFrame.fadeToBlack(fadeMs, new Runnable() {
            @Override
            public void run() {
                // Lower audio slightly for short transitions
                if (sameDay)
                    AudioPlayer.setVolume(ducked);
                // Stop any currently playing audio
                AudioPlayer.stop();
                // Show "Day X" text on black screen
                GameFrame.showCenteredText("Day " + dayNumber);

                // Wait 900ms while showing "Day X", then load scene and fade in
                new javax.swing.Timer(900, new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent ev) {
                        ((javax.swing.Timer) ev.getSource()).stop();
                        // Hide "Day X" and load the new scene
                        GameFrame.hideCenteredText();
                        load(nextScene);
                        // Fade back in from black
                        GameFrame.fadeFromBlack(fadeMs, new Runnable() {
                            @Override
                            public void run() {
                                // Restore master volume
                                if (sameDay)
                                    AudioPlayer.setVolume(originalVolume);
                                GameFrame.showDialogue("");
                            }
                        });
                    }
                }).start();
            }
        });
    }

    /**
     * Transition to the final scene with special "I don't want to remember this"
     * message.
     * This is a unique transition for the story's climax.
     * 
     * The final scene will only be revealed when the player clicks "Next" again
     * (awaitingFinalAdvance becomes true).
     * 
     * @param finalScene The final scene to eventually show
     */
    public static void transitionToFinal(Scene finalScene) {
        // Fade to black and display the special final message
        // Then wait for the player to press Next
        GameFrame.fadeToBlack(700, new Runnable() {
            @Override
            public void run() {
                // Stop any music
                AudioPlayer.stop();
                // Show the emotional message
                GameFrame.showCenteredText("I don't want to remember this");
                GameFrame.hideDialogue();
                GameFrame.rememberButton.setVisible(false);

                // Wait for Next to be pressed to reveal final scene
                awaitingFinalAdvance = true;
                finalPending = finalScene;
            }
        });
    }

    /**
     * Reveal the final scene after the special transition message.
     * Called from the Next button listener when awaitingFinalAdvance is true.
     */
    private static void revealFinal() {
        awaitingFinalAdvance = false;
        if (finalPending == null)
            return;

        // Load final scene while black, then fade in to reveal it
        load(finalPending);
        GameFrame.fadeFromBlack(700, new Runnable() {
            @Override
            public void run() {
                // Final scene decides to use centered text rendering; nothing more needed here.
            }
        });
    }

    /**
     * Load and activate a scene.
     * This sets the scene as current, calls enter() to initialize it,
     * and makes sure the Next button is enabled.
     * 
     * @param scene The Scene to load and display
     */
    public static void load(Scene scene) {
        current = scene;
        System.out.println("Loading scene: " + scene.getClass().getSimpleName());

        // Use enter() to reset the scene index and call its start() method
        // This initializes the scene (loads sprites, plays music, displays first line,
        // etc.)
        scene.enter();

        // Ensure the Next button is enabled when a new scene loads
        GameFrame.nextButton.setEnabled(true);
    }
}

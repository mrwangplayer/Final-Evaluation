package silentconvent;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * GameFrame is the main window/display for the Silent Convent game.
 * It handles all visual elements:
 * - Background images (monastery scenes)
 * - Character sprites (positioned left and right)
 * - Dialogue text display box
 * - Named speech box (showing speaker names)
 * - UI buttons (Next, Remember, Main Menu)
 * - Fade effects (black screen transitions)
 * 
 * This is a singleton - use getInstance() to get the single GameFrame instance.
 */
public class GameFrame extends JFrame {

    // ===== PUBLIC UI ELEMENTS (used by other classes) =====
    /**
    Text area for
    displaying dialogue
    and narrative text*/
    public static JTextArea dialogue;

    /** Button player clicks to advance to next dialogue/scene */
    public static JButton nextButton;

    /** Button to open save/load menu (Remember) */
    public static JButton rememberButton;

    /** Button to return to main menu during gameplay */
    public static JButton mainMenuButton;

    /** Label that displays the background image */
    public static JLabel background;

    /** Single instance of the GameFrame (singleton pattern) */
    private static GameFrame instance;

    // ===== PRIVATE UI ELEMENTS =====
    /** Label on the left side for showing character sprites */
    private JLabel leftSprite;

    /** Label on the right side for showing character sprites */
    private JLabel rightSprite;

    /** Debug overlay label showing what images are currently loaded */
    private JLabel debugLabel;

    /**
     * Constructor - sets up the game window and all UI elements.
     * Creates the frame but does NOT show it automatically
     * (MainMenu controls when the frame becomes visible).
     */
    public GameFrame() {
        // Set up the window itself
        setTitle("Silent Convent");
        setSize(1280, 740);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null); // Use null layout for absolute positioning of components

        // ===== BACKGROUND IMAGE =====
        background = new JLabel();
        background.setBounds(0, 0, 1280, 720);
        add(background);

        // ===== DIALOGUE BOX (bottom center) =====
        dialogue = new JTextArea();
        dialogue.setBounds(100, 500, 1080, 150); // Position and size
        dialogue.setEditable(false); // Players can't type in it
        dialogue.setLineWrap(true); // Wrap long lines
        dialogue.setWrapStyleWord(true); // Wrap at word boundaries
        dialogue.setFont(new Font("Serif", Font.PLAIN, 22));
        dialogue.setBackground(new Color(0, 0, 0, 180)); // Semi-transparent black
        dialogue.setForeground(Color.WHITE); // White text
        add(dialogue);

        // ===== NEXT BUTTON (bottom right) =====
        nextButton = new JButton("Next");
        nextButton.setBounds(1100, 660, 100, 30);
        add(nextButton);

        // ===== REMEMBER BUTTON (bottom left) =====
        rememberButton = new JButton("Remember");
        rememberButton.setBounds(50, 660, 150, 30);
        add(rememberButton);

        // ===== MAIN MENU BUTTON (bottom center) =====
        // Main Menu button available in gameplay to return to main menu
        mainMenuButton = new JButton("Main Menu");
        mainMenuButton.setBounds((1280 - 150) / 2, 660, 150, 30);
        add(mainMenuButton);
        mainMenuButton.addActionListener(e -> {
            // Show main menu dialog without stopping the game
            MainMenu.open();
        });

        // ===== NAMED SPEECH BOX (top right) =====
        // Shows who is speaking and what they say (character name + dialogue)
        namedPanel = new JPanel();
        namedPanel.setLayout(new BorderLayout());
        namedPanel.setBounds(860, 20, 380, 120);
        namedPanel.setBackground(new Color(0, 0, 0, 160));
        namedPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

        // Label showing the speaker's name
        nameLabel = new JLabel("", SwingConstants.LEFT);
        nameLabel.setFont(new Font("Serif", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);

        // Text area showing what the speaker is saying
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setOpaque(false); // Transparent background
        messageArea.setForeground(Color.WHITE);

        namedPanel.add(nameLabel, BorderLayout.NORTH);
        namedPanel.add(messageArea, BorderLayout.CENTER);
        namedPanel.setVisible(false); // Hidden until a character speaks
        add(namedPanel);

        // ===== DEBUG OVERLAY (top left) =====
        // Shows which images are currently loaded (for development/debugging)
        JLabel debugLabel = new JLabel();
        debugLabel.setBounds(10, 10, 420, 80);
        debugLabel.setOpaque(true);
        debugLabel.setBackground(new Color(0, 0, 0, 120));
        debugLabel.setForeground(Color.WHITE);
        debugLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        debugLabel.setVerticalAlignment(SwingConstants.TOP);
        debugLabel.setText("Debug: ");
        debugLabel.setVisible(false); // Hide by default in release builds
        add(debugLabel);
        this.debugLabel = debugLabel;

        // ===== CHARACTER SPRITES =====
        // Character sprites positioned left and right (narrower and shorter to fit
        // frame)
        leftSprite = new JLabel();
        leftSprite.setBounds(60, 200, 220, 380);
        leftSprite.setHorizontalAlignment(SwingConstants.CENTER);
        add(leftSprite);

        rightSprite = new JLabel();
        rightSprite.setBounds(1000, 200, 220, 380);
        rightSprite.setHorizontalAlignment(SwingConstants.CENTER);
        add(rightSprite);

        // ===== FADE PANEL =====
        // Semi-transparent black overlay used for transitions and centered text
        fadePanel = new FadePanel();
        fadePanel.setBounds(0, 0, 1280, 720);
        fadePanel.setVisible(false);
        // Add fade panel to the layered pane to ensure it stays above background and
        // sprites
        getLayeredPane().add(fadePanel, JLayeredPane.POPUP_LAYER);

        // ===== FINALIZE SETUP =====
        instance = this;
        // Ensure background sits at the bottom layer so UI components remain visible
        ensureBackgroundAtBottom();
        // Keep SceneManager.start() out of the constructor so main menu can control
        // startup
        // SceneManager.start();
        // Center window on screen
        setLocationRelativeTo(null);
        // Do not auto-show the frame here so the Main Menu can control when it appears.
    }

    /**
     * Get the singleton GameFrame instance.
     * 
     * @return The GameFrame instance (or null if not yet created)
     */
    public static GameFrame getInstance() {
        return instance;
    }

    /**
     * Ensure an instance exists. Call this before trying to access the frame from
     * startup code.
     * If no instance exists yet, this creates one.
     */
    public static void ensureCreated() {
        if (instance == null)
            new GameFrame();
    }

    /**
     * Ensure the background label is placed at the back of the content pane.
     * This prevents the background from covering other UI elements like buttons and
     * dialogue.
     */
    private void ensureBackgroundAtBottom() {
        try {
            java.awt.Container cp = getContentPane();
            int bottomIndex = Math.max(0, cp.getComponentCount() - 1);
            cp.setComponentZOrder(background, bottomIndex);
            background.repaint();
        } catch (Exception e) {
            System.out.println("Failed to adjust background z-order: " + e.getMessage());
        }
    }

    // ===== DIALOGUE DISPLAY METHODS =====

    /**
     * Display text in the main dialogue box (clears and sets text neatly).
     * 
     * @param text The text to display
     */
    public static void showDialogue(String text) {
        instance.dialogue.setVisible(true);
        instance.dialogue.setText("");
        instance.dialogue.setText(text);
        instance.dialogue.setCaretPosition(0); // Scroll to top
        instance.dialogue.repaint();
    }

    /**
     * Update the debug overlay to show what images are currently loaded.
     * Shows background, left sprite, and right sprite filenames.
     */
    private void updateDebugOverlay() {
        try {
            String bg = "(none)";
            if (background.getIcon() instanceof ImageIcon)
                bg = ((ImageIcon) background.getIcon()).getDescription();
            String left = "(none)";
            if (leftSprite.getIcon() instanceof ImageIcon)
                left = ((ImageIcon) leftSprite.getIcon()).getDescription();
            String right = "(none)";
            if (rightSprite.getIcon() instanceof ImageIcon)
                right = ((ImageIcon) rightSprite.getIcon()).getDescription();
            String s = "BG: " + bg + "<br>Left: " + left + "<br>Right: " + right;
            if (debugLabel != null)
                debugLabel.setText("<html>" + s + "</html>");
        } catch (Exception e) {
            System.out.println("Failed to update debug overlay: " + e.getMessage());
        }
    }

    /**
     * Show a line of dialogue with automatic format detection.
     * Handles multiple dialogue formats:
     * - "[Name] message" → Shows in named speech box with character sprite
     * - "Name: message" → Shows in named speech box (if Name is a single word)
     * - "quoted text" → Shows as Agnes speaking (default)
     * - Plain text → Shows in main dialogue box
     * 
     * @param s The text line to display
     */
    public static void showLine(String s) {
        if (s == null)
            return;
        s = s.trim();

        // ===== FORMAT 1: [Name] message =====
        // Explicit bracket format for specific character dialogue
        if (s.startsWith("[") && s.contains("]")) {
            int end = s.indexOf(']');
            String name = s.substring(1, end).trim();
            String msg = s.substring(end + 1).trim();
            showNamedSpeech(name, msg);
            // Hide main dialogue while named speech shows
            instance.dialogue.setText("");
            instance.dialogue.setVisible(false);
            return;
        }

        // ===== FORMAT 2: Name: message =====
        // Detect a single word name before ':' (e.g., "Agnes: Hello")
        int colon = s.indexOf(':');
        if (colon > 0) {
            String maybeName = s.substring(0, colon).trim();
            if (maybeName.matches("^[A-Za-zÀ-ÖØ-öø-ÿ]+$")) { // Single word, letters only
                String msg = s.substring(colon + 1).trim();
                showNamedSpeech(maybeName, msg);
                instance.dialogue.setText("");
                instance.dialogue.setVisible(false);
                return;
            }
        }

        // ===== FORMAT 3: "quoted speech" =====
        // Assume Agnes as default speaker when lines contain quoted text
        if (s.contains("\"") || (s.startsWith("\"") && s.length() > 1)) {
            String msg = s.replace("\"", "").trim();
            showNamedSpeech("Agnes", msg);
            instance.dialogue.setText("");
            instance.dialogue.setVisible(false);
            return;
        }

        // ===== FORMAT 4: Plain narrative text =====
        // Fallback: plain dialogue line with no speaker
        clearNamedSpeech();
        // When a plain narrative line is shown, clear visible speaker sprites
        // so they don't linger from the previous dialogue
        clearCharacters();
        showDialogue(s);
    }

    /**
     * Show a character speaking with their name in the named speech box.
     * Automatically displays the character sprite and dims the non-speaking
     * character if applicable.
     * 
     * @param name The character's name (can include | or , for two speakers)
     * @param msg  The message the character is saying
     */
    public static void showNamedSpeech(String name, String msg) {
        instance.nameLabel.setText(name);
        instance.messageArea.setText(msg);
        instance.namedPanel.setVisible(true);
        // Attempt to show speaker sprite(s)
        showSpeaker(name);
        // Ensure UI remains above the background and fade panel is still on top
        instance.ensureUIZOrder();
    }

    /**
     * Clear the named speech box (hide the character name and message).
     */
    public static void clearNamedSpeech() {
        instance.nameLabel.setText("");
        instance.messageArea.setText("");
        instance.namedPanel.setVisible(false);
    }

    /**
     * Show a character sprite based on the name string.
     * Supports multiple formats for showing one or two characters:
     * - Single name → Shows on right, dims left
     * - "Name1, Name2" → Shows both, Name1 on left (dimmed), Name2 on right
     * - "Name1 | Name2" → Shows both, Name1 on left (speaking), Name2 on right
     * (dimmed)
     * 
     * @param name The character name(s) to show
     */
    private static void showSpeaker(String name) {
        // If the name string contains a comma, show both speakers
        if (name.contains(",")) {
            String[] parts = name.split(",");
            showLeftCharacter(parts[0].trim());
            showRightCharacter(parts[1].trim());
            dimLeft(false);
            dimRight(true);
        } else if (name.contains("|")) {
            // Pipe separator: show both, first is speaker
            String[] parts = name.split("\\|");
            showLeftCharacter(parts[0].trim());
            showRightCharacter(parts[1].trim());
            // First is speaker (bright), second is dimmed listener
            dimLeft(false);
            dimRight(true);
        } else {
            // Single character: show on right, dim left
            showRightCharacter(name.trim());
            dimLeft(true);
            dimRight(false);
        }
    }

    /**
     * Load and display a character sprite on the left side.
     * 
     * @param charName The character name (e.g., "Agnes")
     */
    public static void showLeftCharacter(String charName) {
        setSpriteForLabel(instance.leftSprite, charName + "_Left.png");
    }

    /**
     * Load and display a character sprite on the right side.
     * 
     * @param charName The character name (e.g., "Agnes")
     */
    public static void showRightCharacter(String charName) {
        setSpriteForLabel(instance.rightSprite, charName + ".png");
    }

    /**
     * Clear all character sprites from both left and right sides.
     * Used when transitioning to purely narrative text.
     */
    public static void clearCharacters() {
        instance.leftSprite.setIcon(null);
        instance.rightSprite.setIcon(null);
        System.out.println("Cleared characters");
        if (instance != null)
            instance.updateDebugOverlay();
    }

    /**
     * Load an image file and set it as the sprite for a label.
     * Searches for the file in multiple locations and handles scaling/centering.
     * 
     * Tries candidates:
     * - Exact filename
     * - assets/images/filename
     * - assets/images/Nun portraits/filename
     * - Lowercase variants
     * 
     * @param lbl      The JLabel to set the sprite on
     * @param filename The image filename to load
     */
    private static void setSpriteForLabel(JLabel lbl, String filename) {
        try {
            // Build list of candidate paths to try
            String[] candidates = new String[] {
                    filename,
                    "assets/images/" + filename,
                    filename.toLowerCase(),
                    "assets/images/" + filename.toLowerCase()
            };

            java.io.File chosen = null;
            // Also try common subfolders (e.g., Nun portraits)
            java.util.List<String> extras = new java.util.ArrayList<>();
            extras.addAll(java.util.Arrays.asList(candidates));
            for (String c : new String[] { "assets/images/Nun portraits/" + filename,
                    "assets/images/Nun portraits/" + filename.toLowerCase() })
                extras.add(c);

            // Try each candidate until one exists
            for (String c : extras) {
                java.io.File f = new java.io.File(c);
                if (f.exists()) {
                    chosen = f;
                    break;
                }
            }

            if (chosen == null) {
                System.out.println("Sprite not found: " + filename);
                lbl.setIcon(null);
                if (instance != null)
                    instance.updateDebugOverlay();
                return;
            }

            // Load the image
            BufferedImage img = ImageIO.read(chosen);

            // Calculate scaled size to fit label while preserving aspect ratio
            int targetW = lbl.getWidth() > 0 ? lbl.getWidth() : img.getWidth();
            int targetH = lbl.getHeight() > 0 ? lbl.getHeight() : img.getHeight();
            // Preserve aspect ratio while fitting into targetW x targetH
            double iw = img.getWidth();
            double ih = img.getHeight();
            double ratio = iw / ih;
            int scaledW = targetW;
            int scaledH = (int) Math.round(targetW / ratio);
            if (scaledH > targetH) {
                scaledH = targetH;
                scaledW = (int) Math.round(targetH * ratio);
            }

            // Scale the image
            Image scaled = img.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
            // Draw centered onto a transparent canvas of size targetW x targetH
            // so the sprite doesn't stretch or shift
            BufferedImage canvas = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = canvas.createGraphics();
            g2.setComposite(AlphaComposite.SrcOver);
            int x = (targetW - scaledW) / 2;
            int y = (targetH - scaledH) / 2;
            g2.drawImage(scaled, x, y, scaledW, scaledH, null);
            g2.dispose();

            // Set the image on the label
            ImageIcon ic = new ImageIcon(canvas);
            ic.setDescription(chosen.getAbsolutePath());
            lbl.setIcon(ic);
            lbl.repaint();
            System.out.println("Loaded sprite for " + filename + ": " + chosen.getAbsolutePath());
            if (instance != null)
                instance.updateDebugOverlay();
        } catch (Exception e) {
            System.out.println("Failed to load sprite " + filename + ": " + e.getMessage());
            lbl.setIcon(null);
            if (instance != null)
                instance.updateDebugOverlay();
        }
    }

    /**
     * Apply a dark tint (dimming effect) to the left character sprite.
     * Used to show which character is currently speaking.
     */
    private static void dimLeft(boolean dim) {
        applyDimToLabel(instance.leftSprite, dim);
    }

    /**
     * Apply a dark tint (dimming effect) to the right character sprite.
     * Used to show which character is currently speaking.
     */
    private static void dimRight(boolean dim) {
        applyDimToLabel(instance.rightSprite, dim);
    }

    /**
     * Apply a translucent black overlay to a JLabel's sprite to produce a dimmed
     * look.
     * This darkens a character sprite to show they're not the one speaking.
     * 
     * @param lbl The label with the sprite to dim
     * @param dim True to dim, false to restore brightness
     */
    private static void applyDimToLabel(JLabel lbl, boolean dim) {
        try {
            if (lbl.getIcon() == null)
                return;
            ImageIcon icon = (ImageIcon) lbl.getIcon();
            String desc = icon.getDescription();
            if (desc == null)
                return;
            java.io.File f = new java.io.File(desc);
            if (!f.exists())
                return;
            BufferedImage img = ImageIO.read(f);
            int targetW = lbl.getWidth() > 0 ? lbl.getWidth() : img.getWidth();
            int targetH = lbl.getHeight() > 0 ? lbl.getHeight() : img.getHeight();
            double iw = img.getWidth();
            double ih = img.getHeight();
            double ratio = iw / ih;
            int scaledW = targetW;
            int scaledH = (int) Math.round(targetW / ratio);
            if (scaledH > targetH) {
                scaledH = targetH;
                scaledW = (int) Math.round(targetH * ratio);
            }
            Image scaled = img.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
            BufferedImage canvas = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = canvas.createGraphics();
            g2.setComposite(AlphaComposite.SrcOver);
            int x = (targetW - scaledW) / 2;
            int y = (targetH - scaledH) / 2;
            g2.drawImage(scaled, x, y, scaledW, scaledH, null);
            if (dim) {
                // Apply 50% black overlay for dimming effect
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, targetW, targetH);
            }
            g2.dispose();
            ImageIcon ic = new ImageIcon(canvas);
            ic.setDescription(desc);
            lbl.setIcon(ic);
            lbl.repaint();
        } catch (Exception e) {
            System.out.println("Failed to apply dim: " + e.getMessage());
        }
    }

    /**
     * Hide the main dialogue box.
     * Used during transitions or when only character sprite dialogue should show.
     */
    public static void hideDialogue() {
        instance.dialogue.setVisible(false);
    }

    /**
     * Show large centered text on the screen (used for "Day X" transitions).
     * 
     * @param text The text to display centered
     */
    public static void showCenteredText(String text) {
        instance.fadePanel.setCenteredText(text);
        instance.fadePanel.setShowCenteredText(true);
        // Ensure fade overlay is visible and topmost so centered text is not occluded
        // by sprites/background
        instance.fadePanel.setVisible(true);
        instance.fadePanel.repaint();
        instance.ensureFadePanelOnTop();
    }

    /**
     * Hide the centered text display.
     */
    public static void hideCenteredText() {
        instance.fadePanel.setShowCenteredText(false);
        instance.fadePanel.repaint();
    }

    /**
     * Set the background image for the current scene.
     * Searches for the image in multiple locations and automatically scales it.
     * 
     * Searches in these locations:
     * - Provided path directly
     * - assets/images/
     * - assets/images/ with bg_ prefix
     * - assets/images/ with _day variant
     * 
     * @param path The image filename or path
     */
    public static void setBackgroundImage(String path) {
        if (path == null)
            return;
        try {
            // Build candidate paths to try
            String base = path;
            if (base.startsWith("assets/"))
                base = base.substring("assets/".length());
            if (base.startsWith("images/"))
                base = base.substring("images/".length());
            base = base.replaceAll("^/", "");
            String nameNoExt = base;
            if (nameNoExt.contains("."))
                nameNoExt = nameNoExt.substring(0, nameNoExt.lastIndexOf('.'));

            // Build list of candidate paths
            java.util.List<String> candList = new java.util.ArrayList<>();
            candList.add(path);
            candList.add("assets/images/" + path);
            candList.add("assets/images/" + nameNoExt + ".png");
            candList.add("assets/images/" + nameNoExt + ".PNG");
            candList.add("assets/images/" + nameNoExt + ".jpg");
            candList.add("assets/images/" + nameNoExt + ".jpeg");
            candList.add("assets/images/" + nameNoExt);
            candList.add("assets/images/bg_" + nameNoExt + ".PNG");
            candList.add("assets/images/bg_" + nameNoExt + ".png");
            candList.add("assets/images/bg_" + nameNoExt + "_day.PNG");
            candList.add("assets/images/bg_" + nameNoExt + "_day.png");
            candList.add("assets/images/bg_" + nameNoExt + "_day_calm.PNG");
            candList.add("assets/images/bg_" + nameNoExt + "_calm.PNG");

            // If the base had two parts like place_mood try to expand to day variants
            if (nameNoExt.contains("_")) {
                String[] parts = nameNoExt.split("_");
                if (parts.length >= 2) {
                    String place = parts[0];
                    String mood = parts[1];
                    candList.add("assets/images/bg_" + place + "_day_" + mood + ".PNG");
                    candList.add("assets/images/bg_" + place + "_day_" + mood + ".png");
                    candList.add("assets/images/bg_" + place + "_" + mood + ".PNG");
                    candList.add("assets/images/bg_" + place + "_" + mood + ".png");
                }
            }

            String[] candidates = candList.toArray(new String[0]);
            java.io.File f = null;
            // Try each candidate until one exists
            for (String c : candidates) {
                java.io.File tf = new java.io.File(c);
                if (tf.exists()) {
                    f = tf;
                    break;
                }
            }

            if (f == null) {
                System.out.println("Background image not found (tried candidates). Requested: " + path);
                for (String c : candidates)
                    System.out.println(" - " + c);
                return;
            }

            // Load and scale to label size
            BufferedImage img = ImageIO.read(f);
            int w = instance.background.getWidth();
            int h = instance.background.getHeight();
            if (w <= 0 || h <= 0) {
                // Fallback to image original size if label size not available yet
                ImageIcon ic = new ImageIcon(img);
                ic.setDescription(f.getAbsolutePath());
                instance.background.setIcon(ic);
                return;
            }

            // Scale and set the background
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            ImageIcon ic = new ImageIcon(scaled);
            ic.setDescription(f.getAbsolutePath());
            instance.background.setIcon(ic);
            instance.background.repaint();
            // Ensure the background remains at the back after changing the icon
            instance.ensureBackgroundAtBottom();
            // Re-assert UI z-order so overlays remain visible
            instance.ensureUIZOrder();
            instance.updateDebugOverlay();
        } catch (Exception e) {
            System.out.println("Failed to set background: " + e.getMessage());
        }
    }

    /**
     * Fade the screen to black over the specified duration.
     * When fade is complete, runs the onFullBlack callback.
     * 
     * @param durationMs  How long the fade should take (in milliseconds)
     * @param onFullBlack Callback to run when screen is fully black
     */
    public static void fadeToBlack(int durationMs, Runnable onFullBlack) {
        instance.fadePanel.setVisible(true);
        instance.fadePanel.fadeIn(durationMs, onFullBlack);
    }

    /**
     * Convenience method: fade to black, change background, then fade back in.
     * Creates a smooth transition between scenes.
     * 
     * @param path       The new background image path
     * @param durationMs Total duration of the crossfade effect
     */
    public static void crossfadeBackground(String path, int durationMs) {
        fadeToBlack(durationMs / 2, () -> {
            setBackgroundImage(path);
            fadeFromBlack(durationMs / 2, null);
        });
    }

    /**
     * Fade the screen from black to transparent.
     * 
     * @param durationMs How long the fade should take (in milliseconds)
     * @param onComplete Callback to run when fade is complete
     */
    public static void fadeFromBlack(int durationMs, Runnable onComplete) {
        instance.fadePanel.fadeOut(durationMs, () -> {
            instance.fadePanel.setVisible(false);
            if (onComplete != null)
                onComplete.run();
        });
    }

    /**
     * Trigger a visual & audio 'glitch' effect when Remember button is pressed
     * after the memory becomes broken (story mechanic).
     * Shows a visual flash, plays glitch sound, and displays a warning message.
     */
    public static void triggerRememberGlitch() {
        // Short flash + play glitch sound
        instance.fadePanel.flashOnce(120);
        AudioPlayer.play("assets/audio/glitch_short.wav", false);
        // Show message after slight delay
        new Timer(250, e -> {
            ((Timer) e.getSource()).stop();
            JOptionPane.showMessageDialog(null, "I don't want to remember this.", "Memory",
                    JOptionPane.WARNING_MESSAGE);
        }).start();
    }

    /**
     * Simple wrapper to flash the screen white once for a brief period.
     * 
     * @param ms Duration of the flash in milliseconds
     */
    public static void flashOnce(int ms) {
        instance.fadePanel.flashOnce(ms);
    }

    /**
     * Set the alpha (transparency) of the fade overlay for dimming effects.
     * 0.0 = fully transparent (invisible)
     * 1.0 = fully opaque (solid black)
     * 
     * @param alpha Transparency value (0.0 - 1.0)
     */
    public static void setFadeAlpha(float alpha) {
        if (alpha < 0f)
            alpha = 0f;
        if (alpha > 1f)
            alpha = 1f;
        instance.fadePanel.setAlpha(alpha);
        instance.fadePanel.setVisible(alpha > 0f);
        instance.ensureFadePanelOnTop();
    }

    /**
     * Show a centered button on top of the fade panel after a delay.
     * Clicking the button runs the callback function.
     * Used for interactive story beats.
     * 
     * @param text    The button label
     * @param delayMs How long to wait before showing the button
     * @param onClick Callback when button is clicked
     */
    public static void showCenteredButtonAfterDelay(String text, int delayMs, Runnable onClick) {
        new Timer(delayMs, e -> {
            ((Timer) e.getSource()).stop();
            JButton btn = new JButton(text);
            btn.setFocusable(false);
            btn.setBounds((instance.getWidth() - 200) / 2, (instance.getHeight() - 40) / 2, 200, 40);
            instance.getLayeredPane().add(btn, JLayeredPane.POPUP_LAYER);
            btn.addActionListener(ae -> {
                instance.getLayeredPane().remove(btn);
                instance.repaint();
                if (onClick != null)
                    onClick.run();
            });
            instance.ensureFadePanelOnTop();
        }).start();
    }

    // ===== INNER FADE PANEL CLASS =====
    /**
     * Inner class that handles fade effects and centered text rendering.
     * Draws a semi-transparent black overlay and optional centered text on top.
     */
    private class FadePanel extends JPanel {
        /** Current transparency of the black overlay (0.0 - 1.0) */
        private float alpha = 0f;

        /** Text to display centered on screen */
        private String centeredText = null;

        /** Whether to show the centered text */
        private boolean showCentered = false;

        public FadePanel() {
            setOpaque(false); // Allow background to show through when transparent
        }

        @Override
        public void setVisible(boolean v) {
            super.setVisible(v);
            // Keep fade panel on top whenever visible
            if (v)
                ensureFadePanelOnTop();
        }

        /**
         * Set the text to display centered on the screen.
         * 
         * @param t The text to show
         */
        public void setCenteredText(String t) {
            centeredText = t;
        }

        /**
         * Control whether to show the centered text.
         * 
         * @param v True to show, false to hide
         */
        public void setShowCenteredText(boolean v) {
            showCentered = v;
        }

        /**
         * Manually set the fade overlay's alpha (transparency).
         * 
         * @param a Alpha value (0.0 = transparent, 1.0 = opaque)
         */
        public void setAlpha(float a) {
            alpha = Math.max(0f, Math.min(1f, a));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            // Draw black overlay with current alpha (transparency)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Draw centered text on top (always fully opaque)
            if (showCentered && centeredText != null) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Serif", Font.BOLD, 36));
                FontMetrics fm = g2.getFontMetrics();
                int w = fm.stringWidth(centeredText);
                int x = (getWidth() - w) / 2;
                int y = (getHeight()) / 2;
                g2.drawString(centeredText, x, y);
            }
            g2.dispose();
        }

        /**
         * Fade in to full black over the specified duration.
         * Calls onFullBlack when fully black.
         * 
         * @param durationMs  How long the fade takes
         * @param onFullBlack Callback when fully black
         */
        public void fadeIn(int durationMs, Runnable onFullBlack) {
            int interval = 40; // Update every 40ms (smooth animation)
            int steps = Math.max(1, durationMs / interval);
            alpha = 0f;
            Timer t = new Timer(interval, null);
            final int[] count = { 0 };
            t.addActionListener(e -> {
                count[0]++;
                alpha = Math.min(1f, (float) count[0] / steps);
                repaint();
                if (alpha >= 1f) {
                    t.stop();
                    if (onFullBlack != null)
                        onFullBlack.run();
                }
            });
            t.start();
        }

        /**
         * Fade out from black to fully transparent.
         * 
         * @param durationMs How long the fade takes
         * @param onComplete Callback when fully transparent
         */
        public void fadeOut(int durationMs, Runnable onComplete) {
            int interval = 40; // Update every 40ms
            int steps = Math.max(1, durationMs / interval);
            alpha = 1f;
            Timer t = new Timer(interval, null);
            final int[] count = { 0 };
            t.addActionListener(e -> {
                count[0]++;
                alpha = Math.max(0f, 1f - (float) count[0] / steps);
                repaint();
                if (alpha <= 0f) {
                    t.stop();
                    if (onComplete != null)
                        onComplete.run();
                }
            });
            t.start();
        }

        /**
         * One quick flash effect (brief white flash, not fade).
         * 
         * @param ms Duration of the flash
         */
        public void flashOnce(int ms) {
            setVisible(true);
            setCenteredText(null);
            alpha = 0.7f; // 70% opaque = visible flash
            repaint();
            new Timer(ms, e -> {
                alpha = 0f;
                repaint();
                ((Timer) e.getSource()).stop();
            }).start();
        }
    }

    // ===== FIELDS FOR NAMED SPEECH AND FADE =====
    /** Panel showing character name and speech (top right) */
    private JPanel namedPanel;

    /** Label for the speaker's name */
    private JLabel nameLabel;

    /** Text area for the speaker's message */
    private JTextArea messageArea;

    /** The fade panel for transitions */
    private FadePanel fadePanel;

    /**
     * Helper to keep z-order (layering) consistent.
     * Ensures fade panel stays on top of all other elements.
     */
    private void ensureFadePanelOnTop() {
        try {
            java.awt.Container cp = getContentPane();
            // If fade panel is on the layered pane, move it to front there
            if (fadePanel.getParent() == getLayeredPane()) {
                getLayeredPane().moveToFront(fadePanel);
            } else {
                cp.setComponentZOrder(fadePanel, 0); // 0 = top
            }
            // Some platforms need repaint to refresh stacking
            fadePanel.repaint();
            cp.repaint();
        } catch (Exception e) {
            System.out.println("Failed to bring fade panel to front: " + e.getMessage());
        }
    }

    /**
     * Ensure UI elements stay in the correct order (z-order).
     * Prevents sprites from covering buttons, dialogue from being hidden, etc.
     */
    private void ensureUIZOrder() {
        try {
            java.awt.Container cp = getContentPane();
            // 0 is top; we want fadePanel at 0, then important UI elements above background
            if (fadePanel != null)
                cp.setComponentZOrder(fadePanel, 0);
            // Place main controls above background (higher numbers = lower in stack)
            cp.setComponentZOrder(nextButton, 1);
            cp.setComponentZOrder(rememberButton, 2);
            cp.setComponentZOrder(dialogue, 3);
            cp.setComponentZOrder(namedPanel, 4);
            cp.setComponentZOrder(leftSprite, 5);
            cp.setComponentZOrder(rightSprite, 6);
            cp.setComponentZOrder(background, cp.getComponentCount() - 1); // Bottom
        } catch (Exception e) {
            System.out.println("Failed to enforce UI z-order: " + e.getMessage());
        } finally {
            // Always ensure fade panel is at the absolute front
            ensureFadePanelOnTop();
        }
    }

}

    public GameFrame() {
        setTitle("Silent Convent");
        setSize(1280, 740);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        background = new JLabel();
        background.setBounds(0, 0, 1280, 720);
        add(background);

        dialogue = new JTextArea();
        dialogue.setBounds(100, 500, 1080, 150);
        dialogue.setEditable(false);
        dialogue.setLineWrap(true);
        dialogue.setWrapStyleWord(true);
        dialogue.setFont(new Font("Serif", Font.PLAIN, 22));
        dialogue.setBackground(new Color(0, 0, 0, 180));
        dialogue.setForeground(Color.WHITE);
        add(dialogue);

        nextButton = new JButton("Next");
        nextButton.setBounds(1100, 660, 100, 30);
        add(nextButton);

        rememberButton = new JButton("Remember");
        rememberButton.setBounds(50, 660, 150, 30);
        add(rememberButton);

        // Main Menu button available in gameplay to return to main menu
        mainMenuButton = new JButton("Main Menu");
        mainMenuButton.setBounds((1280 - 150) / 2, 660, 150, 30);
        add(mainMenuButton);
        mainMenuButton.addActionListener(e -> {
            // Show main menu dialog without stopping the game
            MainMenu.open();
        });

        // Named speech box (top-right)
        namedPanel = new JPanel();
        namedPanel.setLayout(new BorderLayout());
        namedPanel.setBounds(860, 20, 380, 120);
        namedPanel.setBackground(new Color(0, 0, 0, 160));
        namedPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        nameLabel = new JLabel("", SwingConstants.LEFT);
        nameLabel.setFont(new Font("Serif", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setOpaque(false);
        messageArea.setForeground(Color.WHITE);
        namedPanel.add(nameLabel, BorderLayout.NORTH);
        namedPanel.add(messageArea, BorderLayout.CENTER);
        namedPanel.setVisible(false);
        add(namedPanel);

        // Debug overlay (top-left) to help verify loaded assets
        JLabel debugLabel = new JLabel();
        debugLabel.setBounds(10, 10, 420, 80);
        debugLabel.setOpaque(true);
        debugLabel.setBackground(new Color(0, 0, 0, 120));
        debugLabel.setForeground(Color.WHITE);
        debugLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        debugLabel.setVerticalAlignment(SwingConstants.TOP);
        debugLabel.setText("Debug: ");
        debugLabel.setVisible(false); // hide by default in release builds
        add(debugLabel);
        this.debugLabel = debugLabel;
        // Character sprites (left and right) narrower and shorter to fit within frame
        leftSprite = new JLabel();
        leftSprite.setBounds(60, 200, 220, 380);
        leftSprite.setHorizontalAlignment(SwingConstants.CENTER);
        add(leftSprite);

        rightSprite = new JLabel();
        rightSprite.setBounds(1000, 200, 220, 380);
        rightSprite.setHorizontalAlignment(SwingConstants.CENTER);
        add(rightSprite);

        // Fade overlay panel (draws black with variable alpha + centered text)
        fadePanel = new FadePanel();
        fadePanel.setBounds(0, 0, 1280, 720);
        fadePanel.setVisible(false);
        // Add fade panel to the layered pane to ensure it stays above background and
        // sprites
        getLayeredPane().add(fadePanel, JLayeredPane.POPUP_LAYER);

        instance = this;
        // Ensure background sits at the bottom layer so UI components remain visible
        ensureBackgroundAtBottom();
        // Keep SceneManager.start() out of the constructor so main menu can control
        // startup
        // SceneManager.start();
        // center window on screen
        setLocationRelativeTo(null);
        // Do not auto-show the frame here so the Main Menu can control when it appears.
        // allow other classes to get the active frame
    }

    public static GameFrame getInstance() {
        return instance;
    }

    // Ensure an instance exists. Call this before trying to access the frame from
    // startup code.
    public static void ensureCreated() {
        if (instance == null)
            new GameFrame();
    }

    /** Ensure the background label is placed at the back of the content pane. */
    private void ensureBackgroundAtBottom() {
        try {
            java.awt.Container cp = getContentPane();
            int bottomIndex = Math.max(0, cp.getComponentCount() - 1);
            cp.setComponentZOrder(background, bottomIndex);
            background.repaint();
        } catch (Exception e) {
            System.out.println("Failed to adjust background z-order: " + e.getMessage());
        }
    }

    // --- Dialogue helper: clears & sets text neatly ---
    public static void showDialogue(String text) {
        instance.dialogue.setVisible(true);
        instance.dialogue.setText("");
        instance.dialogue.setText(text);
        instance.dialogue.setCaretPosition(0);
        instance.dialogue.repaint();
    }

    private void updateDebugOverlay() {
        try {
            String bg = "(none)";
            if (background.getIcon() instanceof ImageIcon)
                bg = ((ImageIcon) background.getIcon()).getDescription();
            String left = "(none)";
            if (leftSprite.getIcon() instanceof ImageIcon)
                left = ((ImageIcon) leftSprite.getIcon()).getDescription();
            String right = "(none)";
            if (rightSprite.getIcon() instanceof ImageIcon)
                right = ((ImageIcon) rightSprite.getIcon()).getDescription();
            String s = "BG: " + bg + "<br>Left: " + left + "<br>Right: " + right;
            if (debugLabel != null)
                debugLabel.setText("<html>" + s + "</html>");
        } catch (Exception e) {
            System.out.println("Failed to update debug overlay: " + e.getMessage());
        }
    }

    // Show a line; if it is in the format "[Name] message" show it in the named
    // speech box.
    public static void showLine(String s) {
        if (s == null)
            return;
        s = s.trim();
        // [Name] message format (explicit)
        if (s.startsWith("[") && s.contains("]")) {
            int end = s.indexOf(']');
            String name = s.substring(1, end).trim();
            String msg = s.substring(end + 1).trim();
            showNamedSpeech(name, msg);
            // hide main dialogue while named speech shows
            instance.dialogue.setText("");
            instance.dialogue.setVisible(false);
            return;
        }

        // Name: message format (e.g., "Agnes: Hello") — detect a single word name
        // before ':'
        int colon = s.indexOf(':');
        if (colon > 0) {
            String maybeName = s.substring(0, colon).trim();
            if (maybeName.matches("^[A-Za-zÀ-ÖØ-öø-ÿ]+$")) {
                String msg = s.substring(colon + 1).trim();
                showNamedSpeech(maybeName, msg);
                instance.dialogue.setText("");
                instance.dialogue.setVisible(false);
                return;
            }
        }

        // Quoted speech - assume Agnes as default speaker when lines contain quoted
        // text
        if (s.contains("\"") || (s.startsWith("\"") && s.length() > 1)) {
            String msg = s.replace("\"", "").trim();
            showNamedSpeech("Agnes", msg);
            instance.dialogue.setText("");
            instance.dialogue.setVisible(false);
            return;
        }

        // Fallback: plain dialogue line
        clearNamedSpeech();
        // When a plain narrative line is shown, clear visible speaker sprites so they
        // don't linger
        clearCharacters();
        showDialogue(s);
    }

    public static void showNamedSpeech(String name, String msg) {
        instance.nameLabel.setText(name);
        instance.messageArea.setText(msg);
        instance.namedPanel.setVisible(true);
        // attempt to show speaker sprite(s)
        showSpeaker(name);
        // ensure UI remains above the background and fade panel is still on top
        instance.ensureUIZOrder();
    }

    public static void clearNamedSpeech() {
        instance.nameLabel.setText("");
        instance.messageArea.setText("");
        instance.namedPanel.setVisible(false);
    }

    private static void showSpeaker(String name) {
        // If the name string contains a comma, show both speakers
        if (name.contains(",")) {
            String[] parts = name.split(",");
            showLeftCharacter(parts[0].trim());
            showRightCharacter(parts[1].trim());
            dimLeft(false);
            dimRight(true);
        } else if (name.contains("|")) {
            String[] parts = name.split("\\|");
            showLeftCharacter(parts[0].trim());
            showRightCharacter(parts[1].trim());
            // first is speaker
            dimLeft(false);
            dimRight(true);
        } else {
            showRightCharacter(name.trim());
            dimLeft(true);
            dimRight(false);
        }
    }

    public static void showLeftCharacter(String charName) {
        setSpriteForLabel(instance.leftSprite, charName + "_Left.png");
    }

    public static void showRightCharacter(String charName) {
        setSpriteForLabel(instance.rightSprite, charName + ".png");
    }

    public static void clearCharacters() {
        instance.leftSprite.setIcon(null);
        instance.rightSprite.setIcon(null);
        System.out.println("Cleared characters");
        if (instance != null)
            instance.updateDebugOverlay();
    }

    private static void setSpriteForLabel(JLabel lbl, String filename) {
        try {
            // Try a list of candidates: raw filename, assets/images/filename, lowercase
            // variants
            String[] candidates = new String[] {
                    filename,
                    "assets/images/" + filename,
                    filename.toLowerCase(),
                    "assets/images/" + filename.toLowerCase()
            };

            java.io.File chosen = null;
            // Also try common subfolders (e.g., Nun portraits)
            java.util.List<String> extras = new java.util.ArrayList<>();
            extras.addAll(java.util.Arrays.asList(candidates));
            for (String c : new String[] { "assets/images/Nun portraits/" + filename,
                    "assets/images/Nun portraits/" + filename.toLowerCase() })
                extras.add(c);

            for (String c : extras) {
                java.io.File f = new java.io.File(c);
                if (f.exists()) {
                    chosen = f;
                    break;
                }
            }

            if (chosen == null) {
                System.out.println("Sprite not found: " + filename);
                lbl.setIcon(null);
                if (instance != null)
                    instance.updateDebugOverlay();
                return;
            }

            BufferedImage img = ImageIO.read(chosen);

            int targetW = lbl.getWidth() > 0 ? lbl.getWidth() : img.getWidth();
            int targetH = lbl.getHeight() > 0 ? lbl.getHeight() : img.getHeight();
            // Preserve aspect ratio while fitting into targetW x targetH
            double iw = img.getWidth();
            double ih = img.getHeight();
            double ratio = iw / ih;
            int scaledW = targetW;
            int scaledH = (int) Math.round(targetW / ratio);
            if (scaledH > targetH) {
                scaledH = targetH;
                scaledW = (int) Math.round(targetH * ratio);
            }

            Image scaled = img.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
            // Draw centered onto a transparent canvas of size targetW x targetH so the
            // sprite doesn't stretch or shift
            BufferedImage canvas = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = canvas.createGraphics();
            g2.setComposite(AlphaComposite.SrcOver);
            int x = (targetW - scaledW) / 2;
            int y = (targetH - scaledH) / 2;
            g2.drawImage(scaled, x, y, scaledW, scaledH, null);
            g2.dispose();

            ImageIcon ic = new ImageIcon(canvas);
            ic.setDescription(chosen.getAbsolutePath());
            lbl.setIcon(ic);
            lbl.repaint();
            System.out.println("Loaded sprite for " + filename + ": " + chosen.getAbsolutePath());
            if (instance != null)
                instance.updateDebugOverlay();
        } catch (Exception e) {
            System.out.println("Failed to load sprite " + filename + ": " + e.getMessage());
            lbl.setIcon(null);
            if (instance != null)
                instance.updateDebugOverlay();
        }
    }

    private static void dimLeft(boolean dim) {
        applyDimToLabel(instance.leftSprite, dim);
    }

    private static void dimRight(boolean dim) {
        applyDimToLabel(instance.rightSprite, dim);
    }

    // Apply a translucent black overlay to the given JLabel's icon to produce a
    // dimmed look
    private static void applyDimToLabel(JLabel lbl, boolean dim) {
        try {
            if (lbl.getIcon() == null)
                return;
            ImageIcon icon = (ImageIcon) lbl.getIcon();
            String desc = icon.getDescription();
            if (desc == null)
                return;
            java.io.File f = new java.io.File(desc);
            if (!f.exists())
                return;
            BufferedImage img = ImageIO.read(f);
            int targetW = lbl.getWidth() > 0 ? lbl.getWidth() : img.getWidth();
            int targetH = lbl.getHeight() > 0 ? lbl.getHeight() : img.getHeight();
            double iw = img.getWidth();
            double ih = img.getHeight();
            double ratio = iw / ih;
            int scaledW = targetW;
            int scaledH = (int) Math.round(targetW / ratio);
            if (scaledH > targetH) {
                scaledH = targetH;
                scaledW = (int) Math.round(targetH * ratio);
            }
            Image scaled = img.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
            BufferedImage canvas = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = canvas.createGraphics();
            g2.setComposite(AlphaComposite.SrcOver);
            int x = (targetW - scaledW) / 2;
            int y = (targetH - scaledH) / 2;
            g2.drawImage(scaled, x, y, scaledW, scaledH, null);
            if (dim) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, targetW, targetH);
            }
            g2.dispose();
            ImageIcon ic = new ImageIcon(canvas);
            ic.setDescription(desc);
            lbl.setIcon(ic);
            lbl.repaint();
        } catch (Exception e) {
            System.out.println("Failed to apply dim: " + e.getMessage());
        }
    }

    public static void hideDialogue() {
        instance.dialogue.setVisible(false);
    }

    public static void showCenteredText(String text) {
        instance.fadePanel.setCenteredText(text);
        instance.fadePanel.setShowCenteredText(true);
        // ensure fade overlay is visible and topmost so centered text is not occluded
        // by sprites/background
        instance.fadePanel.setVisible(true);
        instance.fadePanel.repaint();
        instance.ensureFadePanelOnTop();
    }

    public static void hideCenteredText() {
        instance.fadePanel.setShowCenteredText(false);
        instance.fadePanel.repaint();
    }

    public static void setBackgroundImage(String path) {
        if (path == null)
            return;
        try {
            // Build candidate paths to try (handle both full and partial paths, case
            // variants, and missing extension)
            String base = path;
            if (base.startsWith("assets/"))
                base = base.substring("assets/".length());
            if (base.startsWith("images/"))
                base = base.substring("images/".length());
            base = base.replaceAll("^/", "");
            String nameNoExt = base;
            if (nameNoExt.contains("."))
                nameNoExt = nameNoExt.substring(0, nameNoExt.lastIndexOf('.'));

            java.util.List<String> candList = new java.util.ArrayList<>();
            candList.add(path);
            candList.add("assets/images/" + path);
            candList.add("assets/images/" + nameNoExt + ".png");
            candList.add("assets/images/" + nameNoExt + ".PNG");
            candList.add("assets/images/" + nameNoExt + ".jpg");
            candList.add("assets/images/" + nameNoExt + ".jpeg");
            candList.add("assets/images/" + nameNoExt);
            candList.add("assets/images/bg_" + nameNoExt + ".PNG");
            candList.add("assets/images/bg_" + nameNoExt + ".png");
            candList.add("assets/images/bg_" + nameNoExt + "_day.PNG");
            candList.add("assets/images/bg_" + nameNoExt + "_day.png");
            candList.add("assets/images/bg_" + nameNoExt + "_day_calm.PNG");
            candList.add("assets/images/bg_" + nameNoExt + "_calm.PNG");

            // If the base had two parts like place_mood try to expand to day variants
            if (nameNoExt.contains("_")) {
                String[] parts = nameNoExt.split("_");
                if (parts.length >= 2) {
                    String place = parts[0];
                    String mood = parts[1];
                    candList.add("assets/images/bg_" + place + "_day_" + mood + ".PNG");
                    candList.add("assets/images/bg_" + place + "_day_" + mood + ".png");
                    candList.add("assets/images/bg_" + place + "_" + mood + ".PNG");
                    candList.add("assets/images/bg_" + place + "_" + mood + ".png");
                }
            }

            String[] candidates = candList.toArray(new String[0]);
            java.io.File f = null;
            for (String c : candidates) {
                java.io.File tf = new java.io.File(c);
                if (tf.exists()) {
                    f = tf;
                    break;
                }
            }

            if (f == null) {
                System.out.println("Background image not found (tried candidates). Requested: " + path);
                for (String c : candidates)
                    System.out.println(" - " + c);
                return;
            }

            // Load and scale to label size
            BufferedImage img = ImageIO.read(f);
            int w = instance.background.getWidth();
            int h = instance.background.getHeight();
            if (w <= 0 || h <= 0) {
                // fallback to image original size
                ImageIcon ic = new ImageIcon(img);
                ic.setDescription(f.getAbsolutePath());
                instance.background.setIcon(ic);
                return;
            }

            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            ImageIcon ic = new ImageIcon(scaled);
            ic.setDescription(f.getAbsolutePath());
            instance.background.setIcon(ic);
            instance.background.repaint();
            // Ensure the background remains at the back after changing the icon
            instance.ensureBackgroundAtBottom();
            // Re-assert UI z-order so overlays remain visible
            instance.ensureUIZOrder();
            instance.updateDebugOverlay();
        } catch (Exception e) {
            System.out.println("Failed to set background: " + e.getMessage());
        }
    }

    public static void fadeToBlack(int durationMs, Runnable onFullBlack) {
        instance.fadePanel.setVisible(true);
        instance.fadePanel.fadeIn(durationMs, onFullBlack);
    }

    /**
     * Convenience: crossfade background quickly (fade out, set background, fade in)
     */
    public static void crossfadeBackground(String path, int durationMs) {
        fadeToBlack(durationMs / 2, () -> {
            setBackgroundImage(path);
            fadeFromBlack(durationMs / 2, null);
        });
    }

    public static void fadeFromBlack(int durationMs, Runnable onComplete) {
        instance.fadePanel.fadeOut(durationMs, () -> {
            instance.fadePanel.setVisible(false);
            if (onComplete != null)
                onComplete.run();
        });
    }

    // Trigger a visual & audio 'glitch' when Remember is pressed after memory is
    // broken
    public static void triggerRememberGlitch() {
        // short flash + play glitch sound
        instance.fadePanel.flashOnce(120);
        AudioPlayer.play("assets/audio/glitch_short.wav", false);
        // show message after slight delay
        new Timer(250, e -> {
            ((Timer) e.getSource()).stop();
            JOptionPane.showMessageDialog(null, "I don't want to remember this.", "Memory",
                    JOptionPane.WARNING_MESSAGE);
        }).start();
    }

    // Simple wrapper to flash without showing the warning message
    public static void flashOnce(int ms) {
        instance.fadePanel.flashOnce(ms);
    }

    // Set fade overlay alpha (0.0 - 1.0) for dimming effects
    public static void setFadeAlpha(float alpha) {
        if (alpha < 0f)
            alpha = 0f;
        if (alpha > 1f)
            alpha = 1f;
        instance.fadePanel.setAlpha(alpha);
        instance.fadePanel.setVisible(alpha > 0f);
        instance.ensureFadePanelOnTop();
    }

    // Show a centered button on top of the fade panel after a delay; clicking it
    // runs the callback
    public static void showCenteredButtonAfterDelay(String text, int delayMs, Runnable onClick) {
        new Timer(delayMs, e -> {
            ((Timer) e.getSource()).stop();
            JButton btn = new JButton(text);
            btn.setFocusable(false);
            btn.setBounds((instance.getWidth() - 200) / 2, (instance.getHeight() - 40) / 2, 200, 40);
            instance.getLayeredPane().add(btn, JLayeredPane.POPUP_LAYER);
            btn.addActionListener(ae -> {
                instance.getLayeredPane().remove(btn);
                instance.repaint();
                if (onClick != null)
                    onClick.run();
            });
            instance.ensureFadePanelOnTop();
        }).start();
    }

    // --- Inner fade panel class ---
    private class FadePanel extends JPanel {
        private float alpha = 0f;
        private String centeredText = null;
        private boolean showCentered = false;

        public FadePanel() {
            setOpaque(false);
        }

        @Override
        public void setVisible(boolean v) {
            super.setVisible(v);
            // keep fade panel on top whenever visible
            if (v)
                ensureFadePanelOnTop();
        }

        public void setCenteredText(String t) {
            centeredText = t;
        }

        public void setShowCenteredText(boolean v) {
            showCentered = v;
        }

        // Allow external control of the dim alpha
        public void setAlpha(float a) {
            alpha = Math.max(0f, Math.min(1f, a));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            // draw black with alpha
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
            // draw centered text (always full opacity)
            if (showCentered && centeredText != null) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Serif", Font.BOLD, 36));
                FontMetrics fm = g2.getFontMetrics();
                int w = fm.stringWidth(centeredText);
                int x = (getWidth() - w) / 2;
                int y = (getHeight()) / 2;
                g2.drawString(centeredText, x, y);
            }
            g2.dispose();
        }

        // fade in to full black over durationMs then call onFullBlack
        public void fadeIn(int durationMs, Runnable onFullBlack) {
            int interval = 40;
            int steps = Math.max(1, durationMs / interval);
            alpha = 0f;
            Timer t = new Timer(interval, null);
            final int[] count = { 0 };
            t.addActionListener(e -> {
                count[0]++;
                alpha = Math.min(1f, (float) count[0] / steps);
                repaint();
                if (alpha >= 1f) {
                    t.stop();
                    if (onFullBlack != null)
                        onFullBlack.run();
                }
            });
            t.start();
        }

        // fade out from black to transparent
        public void fadeOut(int durationMs, Runnable onComplete) {
            int interval = 40;
            int steps = Math.max(1, durationMs / interval);
            alpha = 1f;
            Timer t = new Timer(interval, null);
            final int[] count = { 0 };
            t.addActionListener(e -> {
                count[0]++;
                alpha = Math.max(0f, 1f - (float) count[0] / steps);
                repaint();
                if (alpha <= 0f) {
                    t.stop();
                    if (onComplete != null)
                        onComplete.run();
                }
            });
            t.start();
        }

        // one quick flash effect
        public void flashOnce(int ms) {
            setVisible(true);
            setCenteredText(null);
            alpha = 0.7f;
            repaint();
            new Timer(ms, e -> {
                alpha = 0f;
                repaint();
                ((Timer) e.getSource()).stop();
            }).start();
        }
    }

    // fields for named speech and fade
    private JPanel namedPanel;
    private JLabel nameLabel;
    private JTextArea messageArea;
    private FadePanel fadePanel;

    // helper to keep z-order consistent
    private void ensureFadePanelOnTop() {
        try {
            java.awt.Container cp = getContentPane();
            // If fade panel is on the layered pane, move it front there
            if (fadePanel.getParent() == getLayeredPane()) {
                getLayeredPane().moveToFront(fadePanel);
            } else {
                cp.setComponentZOrder(fadePanel, 0); // top
            }
            // some platforms need repaint to refresh stacking
            fadePanel.repaint();
            cp.repaint();
        } catch (Exception e) {
            System.out.println("Failed to bring fade panel to front: " + e.getMessage());
        }
    }

    private void ensureUIZOrder() {
        try {
            java.awt.Container cp = getContentPane();
            // 0 is top; we want fadePanel at 0, then important UI elements above background
            if (fadePanel != null)
                cp.setComponentZOrder(fadePanel, 0);
            // place main controls above background
            cp.setComponentZOrder(nextButton, 1);
            cp.setComponentZOrder(rememberButton, 2);
            cp.setComponentZOrder(dialogue, 3);
            cp.setComponentZOrder(namedPanel, 4);
            cp.setComponentZOrder(leftSprite, 5);
            cp.setComponentZOrder(rightSprite, 6);
            cp.setComponentZOrder(background, cp.getComponentCount() - 1); // bottom
        } catch (Exception e) {
            System.out.println("Failed to enforce UI z-order: " + e.getMessage());
        } finally {
            // Always ensure fade panel is at the absolute front
            ensureFadePanelOnTop();
        }
    }
}

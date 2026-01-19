package silentconvent;

import java.awt.*;
import javax.swing.*;

public class MainMenu {
    public static void open() {
        JDialog d = new JDialog((Frame) null, "Main Menu", true);
        d.setSize(480, 380);
        d.setLayout(new BorderLayout());
        d.setLocationRelativeTo(null);

        if (GameFrame.getInstance() != null) {
            GameFrame.getInstance().setVisible(false);
        }

        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Silent Convent", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 28));
        top.add(title, BorderLayout.CENTER);
        d.add(top, BorderLayout.NORTH);

        // try to show menu background if available
        JLabel img = new JLabel();
        img.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            java.io.File f = new java.io.File("assets/images/bc_mainMenu.png");
            if (!f.exists())
                f = new java.io.File("assets/images/bg_library.PNG");
            if (f.exists()) {
                ImageIcon ic = new ImageIcon(f.getAbsolutePath());
                img.setIcon(ic);
            }
        } catch (Exception e) {
        }
        d.add(img, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton play = new JButton("Play");
        JButton saves = new JButton("Saves");
        JButton story = new JButton("Story");
        story.setEnabled(SceneManager.isStoryUnlocked());
        buttons.add(play);
        buttons.add(saves);
        buttons.add(story);
        d.add(buttons, BorderLayout.SOUTH);

        play.addActionListener(e -> {
            d.dispose();
            // If the SceneManager hasn't been started, start it; otherwise restart from day
            // one
            if (!isSceneManagerStarted()) {
                SceneManager.start();
            } else {
                // Ensure game frame exists and is visible, then restart from Day One
                GameFrame.ensureCreated();
                GameFrame.getInstance().setVisible(true);
                SceneManager.load(new scenes.DayOneScene());
            }
        });

        saves.addActionListener(e -> {
            SaveMenu.open(d);
        });

        story.addActionListener(e -> {
            // For now show a simple summary dialog with the entire story if unlocked
            if (SceneManager.isStoryUnlocked()) {
                JOptionPane.showMessageDialog(d, "The Monastery Without Doors\n\n" +
                        "The monastery stood where the road ended, surrounded by fields that bent gently in the wind. Its walls were pale stone, warmed by sunlight even in the early morning, and the bells rang softly, never urgently, as if time itself moved more slowly there.\n\n"
                        +
                        "Six young nuns lived within its walls.\n\n" +
                        "They were all the same age — nineteen, perhaps twenty — and they moved together with an ease that came from familiarity rather than discipline. Mornings began in the library, where dust drifted through tall windows and pages whispered when turned. Afternoons belonged to the garden, where flowers grew without symmetry and laughter carried easily across the grass. Evenings ended with shared meals and quiet conversation, followed by lying in a circle beneath the sky, watching clouds dissolve into stars.\n\n"
                        +
                        "They called one another sisters, and they meant it.\n\n" +
                        "Elara was the quiet one. She listened more than she spoke, her gaze often lingering somewhere just beyond the others, as if watching something only she could see. Miriam teased her gently for it. Clara reminded her to eat. Lucia pulled her into laughter when silence became too heavy. Ruth prayed for her without ever saying why. Agnes watched over all of them, calm and unyielding.\n\n"
                        +
                        "It was a good life. A safe one.\n\n" +
                        "At first, nothing felt wrong.\n\n" +
                        "Then small things began to repeat.\n\n" +
                        "A phrase spoken twice in the same tone. A laugh that lingered too long. A question asked again when it had already been answered. No one acknowledged it. They smiled, adjusted, moved on.\n\n"
                        +
                        "Irritation crept in quietly.\n\n" +
                        "A voice too loud in the library. Footsteps that echoed longer than they should have. The clatter of cutlery at dinner sounding sharper each night. Miriam once asked Clara to be quieter, her smile strained. Another time, someone said, very calmly, that the sound of breathing was unbearable.\n\n"
                        +
                        "The words hung in the air long after they were spoken.\n\n" +
                        "The first disappearance happened without announcement.\n\n" +
                        "One morning, a place at the table was empty. A chair pushed in as if it had never been used. The others did not comment. They passed bread. They spoke of the weather. The day continued.\n\n"
                        +
                        "That night, the silence at dinner felt thick. The food tasted unfamiliar — not unpleasant, but wrong, as if its texture did not belong in the mouth. A heavy smell lingered, something warm and dense that no one named.\n\n"
                        +
                        "Another day passed.\n\n" +
                        "Then another sister was gone.\n\n" +
                        "The routines held. The garden still bloomed. The bells still rang. But the air pressed closer, and emotions surfaced without warning. Kindness became fragile. Affection sharpened into frustration. Silence stretched until it felt intentional.\n\n"
                        +
                        "Elara noticed the gaps.\n\n" +
                        "She counted them when she thought no one was watching.\n\n" +
                        "Each disappearance made the monastery smaller. The halls narrower. The rooms quieter. The laughter thinner.\n\n"
                        +
                        "Memory became unreliable. Events slipped out of order. Days folded into one another. Sometimes Elara was certain something had happened. Other times, she was sure it had not.\n\n"
                        +
                        "By the final days, only she remained.\n\n" +
                        "There was no dramatic moment. No realization spoken aloud.\n\n" +
                        "Just quiet.\n\n" +
                        "The monastery did not collapse. It simply faded — like a thought abandoned halfway through.\n\n"
                        +
                        "What remained was a girl sitting alone, no longer pretending she was surrounded by others.\n\n"
                        +
                        "Elara had once lived in a house filled with voices. Then those voices were gone. Her parents left behind a silence too large for one person to carry. In the years that followed, she learned how to be observed, how to speak carefully, how to stay inside the lines of her own mind.\n\n"
                        +
                        "The monastery was something she built when the world became unlivable.\n\n" +
                        "Six sisters were easier than one grieving child.\n\n" +
                        "But even imagined walls cannot hold forever.\n\n" +
                        "When the illusion finally dissolved, there was no terror — only exhaustion.\n\n" +
                        "Elara remained.\n\n" +
                        "Not healed. Not whole.\n\n" +
                        "But alive.\n\n" +
                        "And somewhere in the quiet that followed, there was space for something real to begin.",
                        "Story", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        d.setVisible(true);
    }

    private static boolean isSceneManagerStarted() {
        try {
            java.lang.reflect.Field f = SceneManager.class.getDeclaredField("started");
            f.setAccessible(true);
            return f.getBoolean(null);
        } catch (Exception e) {
            return true; // safe default
        }
    }
}

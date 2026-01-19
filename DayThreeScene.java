package scenes;

import java.awt.Font;
import silentconvent.*;

public class DayThreeScene extends Scene {

    protected String[] text = {
            "Four sisters sit at breakfast.",
            "Someone keeps repeating the same prayer.",
            "No one asks where the others are.",
            "A chair scrapes loudly.",
            "Agnes: \"Please stop.\"",
            "Agnes: \"Please stop talking.\""
    };

    @Override
    public void start() {
        GameFrame.showDialogue("");
        GameFrame.setBackgroundImage("assets/images/bg_dining_tense.PNG");
        // Play unease at 50% local volume (preserves master volume)
        AudioPlayer.play("assets/audio/bgm_day_unease.wav", true, 0.5f);
        GameFrame.dialogue.setFont(new Font("SansSerif", Font.PLAIN, 23));
        index = 0;
        updateDisplay();
    }

    private boolean tensionChoiceMade = false;

    public void next() {
        index++;
        // Present tension choice at index 2
        if (index == 2 && !tensionChoiceMade) {
            // show another nun to indicate who may speak
            GameFrame.showLeftCharacter("Lucille");
            // dim background while choice is visible
            GameFrame.setFadeAlpha(0.35f);

            String prompt = "She is already on edge. What should she do?";
            String[] opts = { "She should speak.", "She should stay silent." };
            int sel = silentconvent.ChoiceDialog.showChoices(null, prompt, opts);
            tensionChoiceMade = true;
            // remove dim
            GameFrame.setFadeAlpha(0f);

            if (sel == 0) {
                // Speaking reveals disturbing information — leave nun visible
                String[] newText = new String[text.length + 1];
                System.arraycopy(text, 0, newText, 0, index + 1);
                newText[index + 1] = "She whispers about a girl who always counted to six.";
                if (text.length > index + 1)
                    System.arraycopy(text, index + 1, newText, index + 2, text.length - (index + 1));
                text = newText;
            } else {
                // Silence: remove the nun and create a tense line
                GameFrame.clearCharacters();
                String[] newText = new String[text.length + 1];
                System.arraycopy(text, 0, newText, 0, index + 1);
                newText[index + 1] = "Silence stretches. Their breath fills the space.";
                if (text.length > index + 1)
                    System.arraycopy(text, index + 1, newText, index + 2, text.length - (index + 1));
                text = newText;
            }
            updateDisplay();
            return;
        }

        if (index < text.length) {
            updateDisplay();
        } else {
            SceneManager.transitionTo(new DayFourScene(), 4);
        }
    }

    @Override
    public void updateDisplay() {
        GameFrame.showLine(text[index]);
    }

    @Override
    public int getDay() {
        return 3;
    }
}

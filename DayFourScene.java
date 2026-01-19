package scenes;

import java.awt.Font;
import silentconvent.*;

public class DayFourScene extends Scene {

    protected String[] text = {
            "Three plates are set at dinner.",
            "The food smells wrong.",
            "No one touches it.",
            "Breathing feels loud.",
            "Lucille: \"Your chewing is unbearable.\"",
            "Silence answers back."
    };

    @Override
    public void start() {
        GameFrame.showDialogue("");
        GameFrame.setBackgroundImage("assets/images/bg_dining_tense.PNG");
        // Slightly reduce music during tense dinner
        AudioPlayer.play("assets/audio/bgm_day_unease.wav", true, 0.6f);
        GameFrame.dialogue.setFont(new Font("SansSerif", Font.BOLD, 24));
        index = 0;
        updateDisplay();
    }

    @Override
    public void next() {
        index++;
        if (index < text.length) {
            updateDisplay();
        } else {
            SceneManager.transitionTo(new DayFiveScene(), 5);
        }
    }

    @Override
    public void updateDisplay() {
        GameFrame.showLine(text[index]);
    }

    @Override
    public int getDay() {
        return 4;
    }
}

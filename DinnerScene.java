package scenes;

import java.awt.Font;
import silentconvent.*;

public class DinnerScene extends Scene {

    private String[] text = {
            "Dinner is served.",
            "The food smells unfamiliar.",
            "No one speaks.",
            "A fork scrapes against a plate.",
            "Beatrice: \"Please be quiet.\"",
            "Helena: \"Your voice is driving me insane.\""
    };

    @Override
    public void start() {
        GameFrame.showDialogue("");
        // Dinner scene background + calmer ambience
        GameFrame.setBackgroundImage("assets/images/bg_dining_calm.PNG");
        AudioPlayer.play("assets/audio/ambience_dinner.wav", true, 0.5f);
        GameFrame.dialogue.setFont(new Font("SansSerif", Font.BOLD, 24));
        index = 0;
        updateDisplay();
    }

    @Override
    public void next() {
        index++;
        if (index < text.length) {
            GameFrame.showLine(text[index]);
        } else {
            // Do not break memory here; memory will be broken later in the narrative (Day
            // 5)
            SceneManager.transitionTo(new DayTwoScene(), 2);
        }
    }
}

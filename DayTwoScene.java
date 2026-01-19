package scenes;

import java.awt.Font;
import silentconvent.*;

public class DayTwoScene extends Scene {

    protected String[] text = {
            "Only five sisters gather in the garden.",
            "The space where one should be is ignored.",
            "Someone mentions the weather.",
            "Someone laughs too loudly.",
            "Everything is normal."
    };

    @Override
    public void start() {
        GameFrame.showDialogue("");
        // Garden calm background for day two
        GameFrame.setBackgroundImage("assets/images/bg_garden_day_calm.PNG");
        AudioPlayer.play("assets/audio/bgm_day_calm.wav", true, 1.0f);
        GameFrame.dialogue.setFont(new Font("SansSerif", Font.PLAIN, 22));
        index = 0;
        updateDisplay();
    }

    @Override
    public void next() {
        index++;
        if (index < text.length) {
            updateDisplay();
        } else {
            SceneManager.transitionTo(new DayThreeScene(), 3);
        }
    }

    @Override
    public void updateDisplay() {
        GameFrame.showLine(text[index]);
    }

    @Override
    public int getDay() {
        return 2;
    }
}

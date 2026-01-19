package scenes;

import java.awt.Font;
import silentconvent.*;

public class DayFiveScene extends Scene {

    protected String[] text = {
            "Two sisters walk the hallway.",
            "One speaks. The other nods.",
            "Footsteps echo where none should be.",
            "Someone laughs.",
            "Miriam: \"We've always been this way.\""
    };

    @Override
    public void start() {
        GameFrame.showDialogue("");
        GameFrame.setBackgroundImage("assets/images/bg_bedroom_calm.PNG");
        AudioPlayer.play("assets/audio/ambience_monastery.wav", true, 1.0f);
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
            // Narrative memory lock happens here (climax moment)
            AudioPlayer.play("assets/audio/glitch_short.wav", false, 1.0f);
            SaveManager.breakMemory();
            SceneManager.transitionTo(new DaySixScene(), 6);
        }
    }

    @Override
    public void updateDisplay() {
        GameFrame.showLine(text[index]);
    }

    @Override
    public int getDay() {
        return 5;
    }
}

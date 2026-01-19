package scenes;

import java.awt.Font;
import silentconvent.*;

public class DaySixScene extends Scene {

    protected String[] text = {
            "One plate.",
            "One chair.",
            "The room feels too large.",
            "The walls breathe.",
            "My heartbeat rises.",
            "Too high.",
            "Too loud."
    };

    @Override
    public void start() {
        GameFrame.showDialogue("");
        // Tense bedroom for day six
        GameFrame.setBackgroundImage("assets/images/bg_bedroom_tense.PNG");
        // Quick flash at scene start for emphasis
        GameFrame.flashOnce(1500);
        AudioPlayer.play("assets/audio/highpitch.wav", true, 1.0f);
        GameFrame.dialogue.setFont(new Font("SansSerif", Font.BOLD, 26));
        index = 0;
        updateDisplay();
    }

    @Override
    public void next() {
        index++;
        if (index < text.length) {

            updateDisplay();
        } else {
            // Transition to a final-room image briefly, then reveal FinalScene
            GameFrame.fadeToBlack(500, () -> {
                GameFrame.setBackgroundImage("assets/images/bg_empty_final.PNG");
                // keep heartbeat playing, then transition to final
                GameFrame.fadeFromBlack(500, () -> {
                    SceneManager.transitionToFinal(new FinalScene());
                });
            });
        }
    }

    @Override
    public void updateDisplay() {
        GameFrame.showLine(text[index]);
    }

    @Override
    public int getDay() {
        return 6;
    }
}

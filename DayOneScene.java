package scenes;

import java.awt.Font;
import silentconvent.*;

public class DayOneScene extends Scene {

    protected String[] text = {
            "Morning light spills across the monastery garden.",
            "Six young sisters sit together among the flowers.",
            "They laugh quietly. The sound feels warm.",
            "They lie on the grass in a circle, watching clouds drift.",
            "No one wants to move.",
            "Thérèse: \"I remember my mother's lullaby.\""
    };

    @Override
    public void start() {
        // Clear any previous text and set calm music for the day scenes
        GameFrame.dialogue.setText("");
        AudioPlayer.play("assets/audio/bgm_day_calm.wav", true, 1.0f);
        GameFrame.setBackgroundImage("assets/images/bg_garden_day_calm.PNG");
        GameFrame.dialogue.setFont(new Font("Serif", Font.PLAIN, 22));
        index = 0;
        updateDisplay();
    }

    private String choiceDetail = null;

    @Override
    public void next() {
        index++;
        // Present a choice on the early calm moment
        if (index == 1 && choiceDetail == null) {
            String prompt = "What should she ask?";
            String[] opts = { "Ask about literature", "Ask about science", "Ask about faith" };
            int sel = silentconvent.ChoiceDialog.showChoices(null, prompt, opts);
            if (sel >= 0) {
                if (sel == 0)
                    choiceDetail = "She asks about books; the sister smiles about a favorite poet.";
                if (sel == 1)
                    choiceDetail = "She asks about stars; the sister speaks of experiments she once read about.";
                if (sel == 2)
                    choiceDetail = "She asks about faith; the sister hums a hymn and looks at the sky.";
                // inject the choice detail into the text stream after the current line
                String[] newText = new String[text.length + 1];
                System.arraycopy(text, 0, newText, 0, index + 1);
                newText[index + 1] = choiceDetail;
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
            SceneManager.load(new DinnerScene());
        }
    }

    @Override
    public void updateDisplay() {
        GameFrame.dialogue.setText(text[index]);
    }

    @Override
    public int getDay() {
        return 1;
    }
}

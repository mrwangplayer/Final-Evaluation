package scenes;

import silentconvent.*;

public class FinalScene extends Scene {

    protected String[] text = {
            "There was never a convent.",
            "There was never six.",
            "Only one girl.",
            "Nineteen years old.",
            "Her parents never returned home.",
            "The institution was quiet.",
            "Too quiet.",
            "So she made others.",
            "So she wouldn’t be alone.",
            "She called them sisters.",
            "She called it faith.",
            "She called it home."
    };

    @Override
    public void start() {
        // Final scene uses centered text over the background (not the dialogue box)
        GameFrame.hideDialogue();
        AudioPlayer.stop();
        // set final empty room behind the centered text
        GameFrame.setBackgroundImage("assets/images/bg_empty_final.PNG");
        GameFrame.hideDialogue();
        index = 0;
        updateDisplay();
    }

    @Override
    public void next() {
        index++;
        if (index < text.length) {
            updateDisplay();
        } else {
            GameFrame.showCenteredText("The game ends in a long breath.");
            GameFrame.nextButton.setEnabled(false);

            // Fade to black, then after 5 seconds show a Main Menu button so the player can
            // return
            GameFrame.fadeToBlack(700, () -> {
                // Hide the text after fade is complete

                GameFrame.hideCenteredText();
                // After black is full, show a Main Menu button after a short pause
                GameFrame.showCenteredButtonAfterDelay("Main Menu", 3000, () -> {
                    // Hide the centered text when the button appears
                    // Open the main menu and unlock story view
                    SceneManager.unlockStory();
                    silentconvent.MainMenu.open();
                    updateDisplay();
                });
            });
        }
    }

    @Override
    public void updateDisplay() {
        GameFrame.showCenteredText(text[index]);
    }
}

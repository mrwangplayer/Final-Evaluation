package silentconvent;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * AudioPlayer handles all sound and music playback in the game.
 * It manages:
 * - Playing background music (looped)
 * - Playing sound effects (One timed)
 * - Master volume control
 * - Per-track volume multipliers (for relative loudness)
 * - Preventing audio interruptions (reuses clips if same track is playing)
 * 
 * Volume is linear (0.0 - 1.0),and convertedtoDecibels
 */
public class AudioPlayer {

    /** The currently playing audio clip */
    private static Clip clip;

    /** Path of the currently playing audio file */
    private static String currentPath = null;

    /** Whether the current clip should loop continuously */
    private static boolean currentLoop = false;

    /** Local multiplier applied to current track (0.0 - 1.0) */
    private static float currentLocalMultiplier = 1.0f;

    /**
     * Master volume for all audio (linear 0.0 - 1.0).
     * Set to 100% by default so local multipliers control relative levels.
     * Example: master = 0.8 (80%), local = 0.5 (50%) → effective = 40%
     */
    private static float masterVolume = 1.0f;

    /**
     * Set the master volume for all audio.
     * This affects all currently playing and future audio.
     * 
     * @param linear Volume level (0.0 = silent, 1.0 = full volume)
     */
    public static void setVolume(float linear) {
        // Clamp the value to valid range
        if (linear < 0f)
            linear = 0f;
        if (linear > 1f)
            linear = 1f;
        masterVolume = linear;
        System.out.println("AudioPlayer: volume set to " + (int) (linear * 100) + "%");

        // Apply the new volume to the currently playing clip (if one exists)
        applyVolumeToClip(clip);
    }

    /**
     * Get the current master volume.
     * 
     * @return Volume level (0.0 - 1.0)
     */
    public static float getVolume() {
        return masterVolume;
    }

    /**
     * Apply the master volume to the given audio clip.
     * Converts linear volume (0.0-1.0) to decibels for Java's audio system.
     * 
     * @param c The audio clip to adjust (if null, does nothing)
     */
    private static void applyVolumeToClip(Clip c) {
        if (c == null)
            return;
        try {
            // Get the master gain (volume) control from the audio clip
            FloatControl gainControl = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);

            // Convert linear 0.0-1.0 to decibels using formula: dB = 20 * log10(volume)
            // Use a minimum value of 0.0001 to avoid log(0) which would be -infinity
            float dB = (float) (20.0 * Math.log10(masterVolume <= 0.0001 ? 0.0001 : masterVolume));

            // Clamp to the control's min/max range
            if (dB < gainControl.getMinimum())
                dB = gainControl.getMinimum();
            if (dB > gainControl.getMaximum())
                dB = gainControl.getMaximum();

            // Apply the calculated volume
            gainControl.setValue(dB);
        } catch (IllegalArgumentException iae) {
            // Some audio systems don't support volume control - ignore silently
            System.out.println("AudioPlayer: volume control not supported for this clip.");
        } catch (Exception e) {
            System.out.println("AudioPlayer: failed to apply volume: " + e.getMessage());
        }
    }

    /**
     * Apply a specific volume value to a clip (independent of master volume).
     * Used for local multiplier calculations.
     * 
     * @param c      The audio clip to adjust
     * @param linear Volume level (0.0 - 1.0)
     */
    private static void applyVolumeToClipWithValue(Clip c, float linear) {
        if (c == null)
            return;
        try {
            FloatControl gainControl = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
            // Clamp to valid range
            float v = Math.max(0.0001f, Math.min(1f, linear));
            // Convert to decibels
            float dB = (float) (20.0 * Math.log10(v));
            // Clamp to control's range
            if (dB < gainControl.getMinimum())
                dB = gainControl.getMinimum();
            if (dB > gainControl.getMaximum())
                dB = gainControl.getMaximum();
            gainControl.setValue(dB);
        } catch (IllegalArgumentException iae) {
            System.out.println("AudioPlayer: volume control not supported for this clip (value set)");
        } catch (Exception e) {
            System.out.println("AudioPlayer: failed to apply explicit volume: " + e.getMessage());
        }
    }

    /**
     * Play an audio file with optional looping.
     * Uses the master volume for playback.
     * 
     * If the same audio is already playing, reuses the clip to avoid
     * interruptions/gaps.
     * 
     * @param path Path to the audio file
     * @param loop True to loop continuously, false for one-shot
     */
    public static void play(String path, boolean loop) {
        // If we're already playing the same path, avoid stopping and reopening the clip
        // which can cause audible gaps or stuttering
        if (path != null && path.equals(currentPath) && clip != null && clip.isRunning()) {
            currentLoop = loop;
            if (loop)
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            applyVolumeToClip(clip);
            System.out.println("AudioPlayer: already playing " + path + ", reusing clip.");
            return;
        }

        // Save track info (used for save/load)
        currentPath = path;
        currentLoop = loop;
        currentLocalMultiplier = 1.0f; // Default multiplier

        // Stop any previous clip
        stop();

        try {
            File audioFile = new File(path);
            System.out.println("Attempting to play audio: " + audioFile.getAbsolutePath());

            // Check if file exists
            if (!audioFile.exists()) {
                System.out.println("Audio file NOT FOUND: " + audioFile.getAbsolutePath());
                return;
            }

            // Load the audio file
            AudioInputStream audio = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(audio);

            // Apply master volume before starting playback
            applyVolumeToClip(clip);

            // Start playback (looped or once)
            if (loop)
                clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop forever
            clip.start();
            System.out.println(
                    "Playing audio: " + audioFile.getName() + " at " + (int) (masterVolume * 100) + "% volume");
        } catch (Exception e) {
            System.out.println("Audio error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Play an audio file with a local volume multiplier.
     * 
     * Local multiplier works with master volume:
     * Example: master = 0.4 (40%), localMultiplier = 0.5 (50%) → effective = 20%
     * 
     * Used for:
     * - Ambient sounds (lower multiplier)
     * - Music fades (adjust multiplier while playing same track)
     * - Sound effect relative loudness
     * 
     * @param path            Path to the audio file
     * @param loop            True to loop continuously, false for one-shot
     * @param localMultiplier Local multiplier (0.0 - 1.0) applied on top of master
     *                        volume
     */
    public static void play(String path, boolean loop, float localMultiplier) {
        // If we're already playing the same path, reuse the clip to avoid interruptions
        if (path != null && path.equals(currentPath) && clip != null && clip.isRunning()) {
            currentLoop = loop;
            currentLocalMultiplier = Math.max(0f, Math.min(1f, localMultiplier));
            // Calculate effective volume: master * local
            float effective = Math.max(0f, Math.min(1f, masterVolume * currentLocalMultiplier));
            applyVolumeToClipWithValue(clip, effective);
            if (loop)
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            System.out.println("AudioPlayer: already playing " + path + ", reusing clip.");
            return;
        }

        // Record current track info (used for save/load)
        currentPath = path;
        currentLoop = loop;
        currentLocalMultiplier = Math.max(0f, Math.min(1f, localMultiplier));

        // Stop any previous clip
        stop();

        try {
            File audioFile = new File(path);
            System.out.println("Attempting to play audio (with local multiplier): " + audioFile.getAbsolutePath()
                    + " multiplier=" + localMultiplier);

            // Check if file exists
            if (!audioFile.exists()) {
                System.out.println("Audio file NOT FOUND: " + audioFile.getAbsolutePath());
                return;
            }

            // Load the audio file
            AudioInputStream audio = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(audio);

            // Apply effective volume (master * localMultiplier)
            float effective = Math.max(0f, Math.min(1f, masterVolume * currentLocalMultiplier));
            applyVolumeToClipWithValue(clip, effective);

            // Start playback (looped or once)
            if (loop)
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            System.out.println("Playing audio: " + audioFile.getName() + " at " + (int) (effective * 100)
                    + "% effective volume (master " + (int) (masterVolume * 100) + "%)");
        } catch (Exception e) {
            System.out.println("Audio error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Stop the currently playing audio.
     * Closes the audio clip and resets track information.
     */
    public static void stop() {
        try {
            if (clip != null && clip.isOpen()) {
                clip.stop();
                clip.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Reset track info
            currentPath = null;
            currentLoop = false;
            currentLocalMultiplier = 1.0f;
        }
    }

    /**
     * Get the path of the currently playing audio track.
     * Used for save/load functionality.
     * 
     * @return The path of the playing track, or null if nothing is playing
     */
    public static String getCurrentTrackPath() {
        return currentPath;
    }

    /**
     * Check if the current track is looping.
     * Used for save/load functionality.
     * 
     * @return True if current track loops, false otherwise
     */
    public static boolean getCurrentLoop() {
        return currentLoop;
    }

    /**
     * Get the local multiplier of the currently playing track.
     * Used for save/load functionality.
     * 
     * @return The local multiplier (0.0 - 1.0)
     */
    public static float getCurrentLocalMultiplier() {
        return currentLocalMultiplier;
    }
}

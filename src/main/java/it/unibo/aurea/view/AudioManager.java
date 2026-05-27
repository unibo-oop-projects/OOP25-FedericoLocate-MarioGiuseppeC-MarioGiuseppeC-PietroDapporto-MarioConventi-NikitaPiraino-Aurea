package it.unibo.aurea.view;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Utility class managing all game audio.
 * Handles background music loop and one-shot sound effects.
 * Background music is paused during special sounds and resumes automatically.
 */
@SuppressWarnings({"PMD.NonThreadSafeSingleton", "PMD.SingularField"})
public final class AudioManager {

    private static final Logger LOGGER = Logger.getLogger(AudioManager.class.getName());
    private static final double BACKGROUND_VOLUME = 0.18;
    private static final double SWIPE_VOLUME = 1.0;
    private static final double POPUP_VOLUME = 1.0;
    private static final double ENDGAME_VOLUME = 0.85;
    private static final String SOUND_NOT_FOUND = "Sound not found: {0}";

    @SuppressFBWarnings(
        value = "LI_LAZY_INIT_STATIC",
        justification = "AudioManager is only accessed from the JavaFX thread."
    )
    private static MediaPlayer backgroundPlayer;
    private static MediaPlayer effectPlayer;

    private AudioManager() { }

    /**
     * Initializes and starts the background music loop.
     * Safe to call multiple times: stops any existing music first.
     */
    public static void startBackground() {
        stopBackground();
        final URL resource = AudioManager.class.getResource("/sounds/background.wav");
        if (resource == null) {
            LOGGER.warning("background.wav not found");
            return;
        }
        backgroundPlayer = new MediaPlayer(new Media(resource.toExternalForm()));
        backgroundPlayer.setVolume(BACKGROUND_VOLUME);
        backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        backgroundPlayer.play();
    }

    /**
     * Stops the background music permanently (used at game end).
     */
    public static void stopBackground() {
        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
            backgroundPlayer = null;
        }
    }

    /**
     * Plays the card swipe sound over the background music.
     */
    public static void playSwipe() {
        playClip("/sounds/swipe.wav", SWIPE_VOLUME);
    }

    /**
     * Plays the counsellor popup sound, pausing background music.
     * Background resumes automatically when the sound ends.
     */
    public static void playPopUp() {
        playWithPause("/sounds/popUp.wav", POPUP_VOLUME);
    }

    /**
     * Plays the victory sound, stopping background music permanently.
     */
    public static void playVictory() {
        playEndGame("/sounds/win.mp3");
    }

    /**
     * Plays the defeat sound, stopping background music permanently.
     */
    public static void playDefeat() {
        playEndGame("/sounds/lose.mp3");
    }

    private static void playClip(final String resourcePath, final double volume) {
        final URL resource = AudioManager.class.getResource(resourcePath);
        if (resource == null) {
            LOGGER.log(Level.WARNING, SOUND_NOT_FOUND, resourcePath);
            return;
        }
        final AudioClip clip = new AudioClip(resource.toExternalForm());
        clip.setVolume(volume);
        clip.play();
    }

    private static void playWithPause(final String resourcePath, final double volume) {
        final URL resource = AudioManager.class.getResource(resourcePath);
        if (resource == null) {
            LOGGER.log(Level.WARNING, SOUND_NOT_FOUND, resourcePath);
            return;
        }
        if (backgroundPlayer != null) {
            backgroundPlayer.pause();
        }
        effectPlayer = new MediaPlayer(new Media(resource.toExternalForm()));
        effectPlayer.setVolume(volume);
        effectPlayer.setOnEndOfMedia(() -> {
            if (backgroundPlayer != null) {
                backgroundPlayer.play();
            }
        });
        effectPlayer.play();
    }

    private static void playEndGame(final String resourcePath) {
        stopBackground();
        final URL resource = AudioManager.class.getResource(resourcePath);
        if (resource == null) {
            LOGGER.log(Level.WARNING, SOUND_NOT_FOUND, resourcePath);
            return;
        }
        effectPlayer = new MediaPlayer(new Media(resource.toExternalForm()));
        effectPlayer.setVolume(ENDGAME_VOLUME);
        effectPlayer.play();
    }
}

// ============================================================
//  AlertSound.java  –  Plays an Alert Sound on Motion Detection
//
//  Two methods provided:
//  1. beep()        – System beep (works everywhere, no extra files)
//  2. playTone()    – Generates a "beep" programmatically using
//                     Java Sound API (javax.sound.sampled)
//                     No external audio file needed!
// ============================================================

import javax.sound.sampled.*;
import java.awt.*;

public class AlertSound {

    // -------------------------------------------------------
    // Controls whether alerts are enabled or muted.
    // Can be toggled from the UI (future enhancement).
    // -------------------------------------------------------
    private boolean soundEnabled = true;

    public AlertSound() { }

    // -------------------------------------------------------
    // Plays an alert.  Uses generated tone as primary,
    // falls back to system beep if sound card is unavailable.
    // -------------------------------------------------------
    public void playAlert() {
        if (!soundEnabled) return; // silent mode

        // Run sound in a separate thread so it doesn't freeze the UI
        new Thread(() -> {
            try {
                playTone(880, 300); // 880 Hz for 300 milliseconds
                Thread.sleep(100);  // tiny gap
                playTone(1100, 200); // higher pitch second beep
            } catch (Exception e) {
                // Fallback: use the system beep (simple and reliable)
                System.out.println("[AlertSound] Falling back to system beep.");
                Toolkit.getDefaultToolkit().beep();
            }
        }).start();
    }

    // -------------------------------------------------------
    // Generates and plays a pure sine-wave tone.
    //
    // HOW IT WORKS:
    // Sound = air pressure waves. We simulate this with a sine wave.
    // We calculate the byte values of a sine wave at a given
    // frequency and hand them to Java's audio output line.
    //
    // @param frequencyHz  Pitch of the tone (e.g., 440 = A4 note)
    // @param durationMs   How long to play in milliseconds
    // -------------------------------------------------------
    public static void playTone(int frequencyHz, int durationMs) {
        try {
            // Define the audio format:
            // 44100 Hz sample rate, 8-bit samples, mono, signed, big-endian
            float sampleRate = 44100f;
            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);

            // DataLine.Info describes what kind of audio line we want
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            // Check if the system supports this audio format
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("[AlertSound] Audio line not supported.");
                Toolkit.getDefaultToolkit().beep(); // fallback
                return;
            }

            // Open the audio output line
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start(); // start audio playback

            // Calculate total number of audio samples needed
            int numSamples = (int) (sampleRate * durationMs / 1000);
            byte[] audioData = new byte[numSamples];

            // Fill the byte array with sine wave values
            // Math.sin gives a value from -1 to +1; we scale to 0–127 range (8-bit)
            for (int i = 0; i < numSamples; i++) {
                double angle = 2.0 * Math.PI * i * frequencyHz / sampleRate;
                audioData[i] = (byte) (Math.sin(angle) * 80); // 80 = volume (max 127)
            }

            // Write the audio data to the speaker
            line.write(audioData, 0, audioData.length);
            line.drain(); // wait until all audio is played
            line.close(); // close the line to free the audio device

        } catch (LineUnavailableException e) {
            System.err.println("[AlertSound] Audio device unavailable: " + e.getMessage());
            Toolkit.getDefaultToolkit().beep(); // system fallback
        }
    }

    // -------------------------------------------------------
    // Simple wrapper for the system beep (backup method)
    // -------------------------------------------------------
    public void beep() {
        if (soundEnabled) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    // Toggle sound on/off
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}

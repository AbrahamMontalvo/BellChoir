/**
 * Filename: Tone.java
 * Author: Nate Williams
 * Editor: Abraham Montalvo
 * Task: CS-410 Lab 2: Bell Choir
 * Due Date: April 4, 2025
 * 
 * We use this Tone class to create instances of notes to play when listed in our text files.
 */

package src;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

public class Tone {

    // Part of code provided, necessary for playing music
    private final AudioFormat af;

    // Constructor for Tone
    Tone(AudioFormat af) {
        this.af = af;
    }

    /**
     * Used initially for testing, but plays a single BellNote when given a line to play
     * @param line
     * @param bn
     */
    private void playNote(SourceDataLine line, BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
    }
}

// BellNote class that takes a Note and NoteLength
class BellNote {
    final Note note;
    final NoteLength length;

    BellNote(Note note, NoteLength length) {
        this.note = note;
        this.length = length;
    }
}

// Enum for number of beats for a BellNote
enum NoteLength {
    WHOLE(1.0f),
    HALF(0.5f),
    QUARTER(0.25f),
    EIGHTH(0.125f);

    private final int timeMs;

    private NoteLength(float length) {
        timeMs = (int)(length * Note.MEASURE_LENGTH_SEC * 1000);
    }

    public int timeMs() {
        return timeMs;
    }
}

// Enum for sound and note frequency played by a Note
enum Note {
    // REST Must be the first 'Note'
    REST,
    A3,
    A3S,
    B3,
    C3,
    C3S,
    D3,
    D3S,
    E3,
    F3,
    F3S,
    G3,
    G3S,
    A4,
    A4S,
    B4,
    C4,
    C4S,
    D4,
    D4S,
    E4,
    F4,
    F4S,
    G4,
    G4S,
    A5;

    public static final int SAMPLE_RATE = 48 * 1024; // ~48KHz
    public static final int MEASURE_LENGTH_SEC = 1;

    // Circumference of a circle divided by # of samples
    private static final double step_alpha = (2.0d * Math.PI) / SAMPLE_RATE;

    private final double FREQUENCY_A_HZ = 220.0d;
    private final double MAX_VOLUME = 127.0d;

    private final byte[] sinSample = new byte[MEASURE_LENGTH_SEC * SAMPLE_RATE];

    // Constructor for Note, setting Note instances at proper frequency for play
    private Note() {
        int n = this.ordinal();
        if (n > 0) {
            // Calculate the frequency!
            final double halfStepUpFromA = n - 1;
            final double exp = halfStepUpFromA / 12.0d;
            final double freq = FREQUENCY_A_HZ * Math.pow(2.0d, exp);

            // Create sinusoidal data sample for the desired frequency
            final double sinStep = freq * step_alpha;
            for (int i = 0; i < sinSample.length; i++) {
                sinSample[i] = (byte)(Math.sin(i * sinStep) * MAX_VOLUME);
            }
        }
    }

    /**
     * Used for playing notes
     * @return sinSample
     */
    public byte[] sample() {
        return sinSample;
    }
}

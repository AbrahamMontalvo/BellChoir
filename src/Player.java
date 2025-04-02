/**
 * Filename: Player.java
 * Author: Nate Williams
 * Editor: Abraham Montalvo
 * Task: CS-410 Lab 2: Bell Choir
 * Due Date: April 4, 2025
 * 
 * We use this Player class through which the Tone instances are played when we load in a song.
 */

package src;
import javax.sound.sampled.SourceDataLine;

public class Player implements Runnable {
    private static final int NUM_TURNS = 5;

    // private final State myJob;
    private final Thread f;

    // boolean for running player
    private volatile boolean running;

    // Boolean indicating turn status
    private boolean myTurn;

    // Counter variable for turns taken
    private int turnCount;

    // Note that player is assigned
    private Note assignment;

    // Constructor for Player
    Player(int i, Note assign) {
        turnCount = 1;
        f = new Thread(this, "Player" + (i+1));
        f.start();
        assignment = assign;
    }

    /**
     * Stops player thread
     * @throws InterruptedException
     */
    public void stopPlayer() throws InterruptedException {
        running = false;
        f.interrupt();
    }

    /**
     * Acquire turn, song line, and play notes
     */
    public void giveTurn(SourceDataLine line, BellNote bn) {
        synchronized (this) {
            if (myTurn) {
                throw new IllegalStateException("Attempt to give a turn to a player who's hasn't completed the current turn");
            }
            myTurn = true;
            notify();
            playNote(line, bn);
            while (myTurn) {
                try {
                    wait();
                } catch (InterruptedException ignored) {}
            }
        }
    }

    /**
     * Runner for thread
     */
    public void run() {
        running = true;
        synchronized (this) {
            do {
                // Wait for my turn
                while (!myTurn) {
                    try {
                        wait();
                    } catch (InterruptedException ignored) {}
                }

                // My turn!
                doTurn();
                turnCount++;

                // Done, complete turn and wakeup the waiting process
                myTurn = false;
                notify();
            } while (running);
        }
    }

    /**
     * Plays note
     * @param line
     * @param bn
     */
    public void playNote(SourceDataLine line, BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
        System.out.println(f.getName() + " played " + bn.note);
    }

    /**
     * Prints output that signifies that the Player has taken their turn
     */
    private void doTurn() {
        System.out.println("Player[" + f.getName() + "] taking turn " + turnCount);
    }

}

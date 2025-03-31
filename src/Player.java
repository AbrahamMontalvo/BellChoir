package src;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Player implements Runnable {
    private static final int NUM_TURNS = 5;

    // private final State myJob;
    private final Thread t;
    private volatile boolean running;
    private boolean myTurn;
    private int turnCount;
    private Note assignment;

    Player(int i, Note assign) {
        turnCount = 1;
        t = new Thread(this, "Player" + (i+1));
        t.start();
        assignment = assign;
    }

    public void stopPlayer() throws InterruptedException {
        t.join();
    }

    public void giveTurn() {
        synchronized (this) {
            if (myTurn) {
                throw new IllegalStateException("Attempt to give a turn to a player who's hasn't completed the current turn");
            }
            myTurn = true;
            notify();
            while (myTurn) {
                try {
                    wait();
                } catch (InterruptedException ignored) {}
            }
        }
    }

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

    void playSong(List<BellNote> song) throws LineUnavailableException {
        final AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();

            for (BellNote bn: song) {
                if(bn.note == assignment){
                    playNote(line, bn);
                }
            }
            line.drain();
        }
    }

    public void playNote(SourceDataLine line, BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
        System.out.println(t.getName() + " played " + bn.note);
    }

    private void doTurn() {
        System.out.println("Player[" + t.getName() + "] taking turn " + turnCount);
    }

}

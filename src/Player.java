package src;
import javax.sound.sampled.SourceDataLine;

public class Player implements Runnable {
    private static final int NUM_TURNS = 5;

    // private final State myJob;
    private final Thread f;
    private volatile boolean running;
    private boolean myTurn;
    private int turnCount;
    private Note assignment;

    Player(int i, Note assign) {
        turnCount = 1;
        f = new Thread(this, "Player" + (i+1));
        f.start();
        assignment = assign;
    }

    public void stopPlayer() throws InterruptedException {
        running = false;
        f.interrupt();
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

    // void playSong(List<BellNote> song) throws LineUnavailableException {
    //     final AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
    //     try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
    //         line.open();
    //         line.start();
    //         line.drain();
    //     }
    // }

    public void playNote(SourceDataLine line, BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
        System.out.println(f.getName() + " played " + bn.note);
    }

    private void doTurn() {
        System.out.println("Player[" + f.getName() + "] taking turn " + turnCount);
    }

}

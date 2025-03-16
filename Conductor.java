import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

public class Conductor implements Runnable{

    private final Thread t;
    private List<Player> choir;
    private static List<BellNote> noteArray;
    private List<Note> assignments;
        
        // Mary had a little lamb
        private static final List<BellNote> song = new ArrayList<BellNote>() {{
            add(new BellNote(Note.A5, NoteLength.QUARTER));
            add(new BellNote(Note.G4, NoteLength.QUARTER));
            add(new BellNote(Note.F4, NoteLength.QUARTER));
            add(new BellNote(Note.G4, NoteLength.QUARTER));
    
            add(new BellNote(Note.A5, NoteLength.QUARTER));
            add(new BellNote(Note.A5, NoteLength.QUARTER));
            add(new BellNote(Note.A5, NoteLength.HALF));
    
            add(new BellNote(Note.G4, NoteLength.QUARTER));
            add(new BellNote(Note.G4, NoteLength.QUARTER));
            add(new BellNote(Note.G4, NoteLength.HALF));
    
            add(new BellNote(Note.A5, NoteLength.QUARTER));
            add(new BellNote(Note.A5, NoteLength.QUARTER));
            add(new BellNote(Note.A5, NoteLength.HALF));
    
            add(new BellNote(Note.A5, NoteLength.QUARTER));
            add(new BellNote(Note.G4, NoteLength.QUARTER));
            add(new BellNote(Note.F4, NoteLength.QUARTER));
            add(new BellNote(Note.G4, NoteLength.QUARTER));
    
            add(new BellNote(Note.A5, NoteLength.QUARTER));
            add(new BellNote(Note.A5, NoteLength.QUARTER));
            add(new BellNote(Note.A5, NoteLength.QUARTER));
            add(new BellNote(Note.A5, NoteLength.QUARTER));
    
            add(new BellNote(Note.G4, NoteLength.QUARTER));
            add(new BellNote(Note.G4, NoteLength.QUARTER));
            add(new BellNote(Note.A5, NoteLength.QUARTER));
            add(new BellNote(Note.G4, NoteLength.QUARTER));
    
            add(new BellNote(Note.F4, NoteLength.WHOLE));
        }};
        
        private static List<BellNote> loadNotes(String filename) {
            List<BellNote> notes = new ArrayList<>();
            try(final Scanner noteReader = new Scanner(new File(filename))){
                String[] noteString;
                while(noteReader.hasNext()){
                    noteString = noteReader.nextLine().split(" ");
                    NoteLength[] reader = new NoteLength[] {null, NoteLength.WHOLE, NoteLength.HALF, null, NoteLength.QUARTER, null, null, null, NoteLength.EIGHTH};
                    notes.add(new BellNote(Note.valueOf(noteString[0]), reader[Integer.parseInt(noteString[1])]));
                }
            }
            catch (IOException e){
                System.out.println("File not found!"); 
            }
            return notes;
        }
    
        /**
         * Read in song
         * Create choir
         * Assign notes to Player instances
         * Play song
         */
        @Override
        public void run() {
            final AudioFormat af =
                new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
            try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
                line.open();
                line.start();
                int index = 0;
                for (BellNote bn: song) {
                    
                }
                line.drain();
            }
            catch (LineUnavailableException e){
                try {
                    t.join();
                    for (Player p : choir){
                        p.stopPlayer();
                    }
                }
                catch(InterruptedException d){}
            }
        }
    
        public static void main(String[] args) throws Exception {
        if(args.length == 0){
            Conductor c = new Conductor("BellChoir\\MaryHadALittleLamb.txt");
        }
        else{
            Conductor c = new Conductor(args[0]);
        }
    }

    public void getUniqueNotes(List<BellNote> e){
        assignments = new LinkedList<>();
        for (BellNote bn: e){
            if(!assignments.contains(bn.note)){
                assignments.add(bn.note);
            }
        }
    }

    Conductor(String filename) {
        List<BellNote> notes = loadNotes(filename);
        this.getUniqueNotes(notes);
        int i = 1;
        while(assignments.size() < 0){
            choir.add(new Player(i, assignments.removeFirst()));
            i += 1;
        }
        int turnCount = 1;
        t = new Thread(this, "Conductor");
        t.start();
    }
}

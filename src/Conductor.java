package src;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Conductor implements Runnable{

    private final Thread t;
    private static List<BellNote> noteArray;
    private List<Note> assignments;
    private Map<Note, Player> playMap;
    private static Map<Integer, NoteLength> reader;

    private static List<BellNote> loadNotes(String filename) throws IllegalArgumentException, IOException, NullPointerException, EOFException{
        List<BellNote> notes = new ArrayList<>();
        reader = new HashMap<>();
        reader.put(1, NoteLength.WHOLE);
        reader.put(2, NoteLength.HALF);
        reader.put(4, NoteLength.QUARTER);
        reader.put(8, NoteLength.EIGHTH);
        try(final Scanner noteReader = new Scanner(new File(filename))) {
            String[] noteString;
            while(noteReader.hasNext()){
                noteString = noteReader.nextLine().split(" ");
                if(noteString.length != 2){
                    throw new EOFException();
                }
                else{
                    Integer beats = Integer.valueOf(noteString[1]);
                    if(reader.keySet().contains(beats)){
                        notes.add(new BellNote(Note.valueOf(noteString[0]), reader.get(beats)));
                    }
                    else{
                        throw new NullPointerException();
                    } 
                }
            }
            noteReader.close();
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
        final AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();
            for (BellNote bn: noteArray) {
                playMap.get(bn.note).playNote(line, bn);
            }
            line.drain();
        }
        catch (LineUnavailableException e){
            try {
                for (Note p : playMap.keySet()){
                    playMap.get(p).stopPlayer();
                }
                t.join();
            }
            catch(InterruptedException d){}
        }
    }
    
    public static void main(String[] args) throws Exception {
        try {
            Conductor c = new Conductor(args[0]);
        } catch (Exception e) {
            
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
        t = new Thread(this, "Conductor");
        try{
            noteArray = loadNotes(filename);
            this.getUniqueNotes(noteArray);
            int i = 1;
            playMap = new HashMap<>();
            while(assignments.size() > 0){
                Note tempNote = assignments.removeFirst();
                Player newPlayer = new Player(i, tempNote);
                playMap.put(tempNote, newPlayer);
                i += 1;
            }
            t.start();
        }
        catch (EOFException h) {
            System.out.println("SONG NOT PLAYED: Lines of file submissions should contain exactly 2 entries, where the first entry is the note and the second is the length of said note.");
        }
        catch (IOException e){
            System.out.println("SONG NOT PLAYED: File not found!");
        }
        catch (IllegalArgumentException n) {
            System.out.println("SONG NOT PLAYED: Illegal note entry.");
        }
        catch (NullPointerException g) {
            System.out.println("SONG NOT PLAYED: Illegal note length entry.");
        }
    }
}

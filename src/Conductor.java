/**
 * Filename: Conductor.java
 * Author: Nate Williams
 * Editor: Abraham Montalvo
 * Task: CS-410 Lab 2: Bell Choir
 * Due Date: April 4, 2025
 * 
 * We use this Conductor class to create and signal Player instances to play the notes listed in our song files.
 */

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

    // Thread for Conductor class
    private final Thread t;

    // List for BellNotes of the song playing
    private static List<BellNote> noteArray;

    // List of unique Notes to assign to players
    private List<Note> assignments;

    // Map of Note assignments to their respective players
    private Map<Note, Player> playMap;

    // Used in loadNotes() method for verifying and reading NoteLengths
    private static Map<Integer, NoteLength> reader;

    // Boolean for while loop
    private volatile boolean playing;

    /**
     * Used to load BellNotes into the global List variable for playing
     * 
     * @param filename
     * @return notes (list of BellNotes, the song)
     * @throws IllegalArgumentException (when illegal note is entered)
     * @throws IOException (when file cannot be found)
     * @throws NullPointerException (when illegal note length is entered)
     * @throws EOFException (when file does not contain two entries per line)
     */
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
                playMap.get(bn.note).giveTurn(line, bn);
            }
            line.drain();
            for (Note p : playMap.keySet()){
                playMap.get(p).stopPlayer();
            }
            System.exit(0);
        }
        catch (LineUnavailableException e){}
        catch(InterruptedException d){}
    }
    
    public static void main(String[] args) throws Exception {
        Conductor c = new Conductor("songs\\GoodKingWenceslas.txt");
    }

    /**
     * Fills List variable with all unique BellNotes in song, used for giving assignments to Player instances
     * 
     * @param e
     */
    public void getUniqueNotes(List<BellNote> e){
        assignments = new LinkedList<>();
        for (BellNote bn: e){
            if(!assignments.contains(bn.note)){
                assignments.add(bn.note);
            }
        }
    }

    /*
     * Constructor used to verify and load songs
     */
    Conductor(String filename) {
        t = new Thread(this, "Conductor");
        playing = true;
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
        // Catch for improper formatting
        catch (EOFException h) {
            System.out.println("SONG NOT PLAYED: Lines of file submissions should contain exactly 2 entries, where the first entry is the note and the second is the length of said note.");
        }

        // Catch for nonexistent file
        catch (IOException e){
            System.out.println("SONG NOT PLAYED: File not found!");
        }

        // Catch for bad note
        catch (IllegalArgumentException n) {
            System.out.println("SONG NOT PLAYED: Illegal note entry.");
        }

        // Catch for bad note length
        catch (NullPointerException g) {
            System.out.println("SONG NOT PLAYED: Illegal note length entry.");
        }
    }
}

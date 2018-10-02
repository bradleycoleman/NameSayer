package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a name in the database.
 * Holds references to all original recordings and user attempts for this name,
 * as well as ratings for each original recording.
 */
public class Name implements Comparable<Object> {
    private String _name;
    private HashMap<File, Integer> _filesToRatings = new HashMap<>();
    private List<File> _files;
    private File _attempt;
    private int _rating;

    public Name(String name, List<File> database, File goodFiles, File badFiles) {
        _name = name;
        _files = database;
        _rating = 0;
        // All database entries are given a default rating of 0/2, to be changed by the text files if they exist
        for (File file : database) {
            _filesToRatings.put(file,_rating);
        }
        if (goodFiles.exists()) {
            setRatingGoodBad(goodFiles,true);
        }
        if (badFiles.exists()) {
            setRatingGoodBad(badFiles, false);
        }
    }

    private void setRatingGoodBad(File textFile, Boolean isGood){
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader(textFile));
            String line;
            // reding the goodnames to find those from this name
            while((line = reader.readLine())!= null){
                if (_files.contains(new File(line))) {
                    if (isGood) {
                        updateRatingOfFile(new File(line), 2);
                    } else {
                        updateRatingOfFile(new File(line), 1);
                    }
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Starts a recording process in bash which will save a audio file whose name is Name + time
     * @throws InterruptedException FileCommands.record throws this, being a processbuilder
     */
    public void addAttempt() throws InterruptedException {
        DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH:mm:ss");
        String attemptName = (_name + "_" + dateFormat.format(new Date()));
        FileCommands.record(attemptName);
        // After the recording is completed, the file is added to this Name's attempts
        _attempt = new File("userdata/attempts/" + attemptName + ".wav");
    }

    public void deleteAttempt(File attempt) {
        _attempt = null;
        FileCommands.deleteFile(attempt);
    }

    /**
     * Names are sorted alphabetically regardless of case
     */
    @Override
    public int compareTo(Object n) {
        return (toString().toLowerCase().compareTo(n.toString().toLowerCase()));
    }

    public int getRating(File file) {
        return _filesToRatings.get(file);
    }
    public String toString() {
        return _name;
    }
    public File getAttempt() {return _attempt;}
    public List<File> getFiles() { return _files; }

    /**
     * This updates the filesToRatings hashmap.
     */
    public void updateRatingOfFile(File file, int rating){
        _filesToRatings.replace(file, rating);
        _rating = rating;
    }

    public void setRating(int rating){
        _rating = rating;
    }
}

package data;

import java.io.File;
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
    private List<File> _attempts;
    private int _rating;

    public Name(String name, List<File> database, List<File> attempts) {
        _rating = 5;
        _attempts = attempts;
        _files = database;
        // All database entries are given a default rating of 5/5, to be changed by user if they consider the
        // recording to be of low quality
        for (File file : database) {
            _filesToRatings.put(file,_rating);
        }
        _name = name;
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
        _attempts.add(new File("userdata/attempts/" + attemptName + ".wav"));
    }

    public void deleteAttempt(File attempt) {
        _attempts.remove(attempt);
        FileCommands.deleteFile(attempt);
    }

    /**
     * Names are sorted alphabetically regardless of case
     */
    @Override
    public int compareTo(Object n) {
        return (toString().toLowerCase().compareTo(n.toString().toLowerCase()));
    }

    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("Attempts: " + _attempts.size() + "\nDatabase Versions: " + _files.size());
        if (_files.size() == 1) {
            sb.append("\nRating: " + _rating + "/5");
        } else {
            sb.append("\nRatings for each version:");
            for (File file : _files) {
                sb.append("\n" + _filesToRatings.get(file) + "/5: " + file.getName());
            }
        }
        return (sb.toString());
    }

    public String toString() {
        return _name;
    }
    public List<File> getAttempts() {return _attempts;}
    public List<File> getFiles() { return _files; }
    public int getRating(File recording) {
        return _filesToRatings.get(recording);
    }

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

package data;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a name in the database.
 * Holds references to all original recordings and user attempts for this name,
 * as well as ratings for each original recording.
 */
public class Name implements Comparable<Object> {
    private String _name;
    private HashMap<File, Integer> _filesToRatings = new HashMap<>();
    private List<File> _files = new ArrayList<>();
    private List<File> _attempts = new ArrayList<>();
    private int _rating;

    public Name(String name, List<File> database, List<File> attempts) {
        _rating = 5;
        _attempts = attempts;
        _files = database;
        for (File file : database) {
            _filesToRatings.put(file,_rating);
        }
        _name = name;
    }

    public void addAttempt() throws InterruptedException {
        FileCommands.record(_name + "_" + _attempts.size());
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
            for (int i = 0; i < _files.size(); i++) {
                sb.append("\n" + _filesToRatings.get(_files.get(i)) + "/5: " + _files.get(i));
            }
        }
        return (sb.toString());
    }

    public String toString() {
        return _name;
    }

    public List<File> getAttempts() {return _attempts;}
    public List<File> getFiles() { return _files; }
}

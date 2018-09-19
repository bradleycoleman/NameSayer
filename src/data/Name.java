package data;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a name in the database.
 * Holds information about the past attempts and rating.
 */
public class Name implements Comparable<Object> {
    private String _name;
    private File _file;
    private int _rating;

    public Name(File file) {
        _file = file;
        Pattern p = Pattern.compile("_([a-zA-Z]*)\\.wav");
        Matcher m = p.matcher(file.getName());
        if (m.find()) {
            _name = m.group(1);
        } else {
            _name = file.getName();
        }

    }

    /**
     * Names are sorted alphabetically regardless of case
     */
    @Override
    public int compareTo(Object n) {
        return (toString().toLowerCase().compareTo(n.toString().toLowerCase()));
    }

    public String getDetails() {
        return "Attempts: \nRating:";
    }

    public String toString() {
        return _name;
    }

    public File getFile() { return _file; }
}

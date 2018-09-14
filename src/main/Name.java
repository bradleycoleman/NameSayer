package main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a name in the database.
 * Holds information about the past attempts and rating.
 */
public class Name {
    private String _name;
    private int _rating;

    public Name(String fileName) {
        Pattern p = Pattern.compile("_([a-zA-Z]*)\\.wav");
        Matcher m = p.matcher(fileName);
        if (m.find()) {
            _name = m.group(1);
        } else {
            _name = fileName;
        }

    }

    public String getDetails() {
        return "Attempts: \nRating:";
    }

    public String toString() {
        return _name;
    }
}

package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a name in the database.
 * Holds references to all original recordings for this name, as well as ratings for each original recording.
 */
public class Name implements Comparable<Object> {
    private String _name;
    private HashMap<File, Integer> _filesToRatings = new HashMap<>();
    private List<File> _files;
    private int _rating;

    public Name(String name, List<File> database, File goodFiles, File badFiles) {
        _name = name;
        _files = new ArrayList<>();
        for (File original: database) {
            _files.add(new File("userdata/fixed/fix" + original.getName()));
        }
        _rating = 0;
        // All database entries are given a default rating of 0/2, to be changed by the text files if they exist
        for (File file : _files) {
            _filesToRatings.put(file,_rating);
        }
        if (goodFiles.exists()) {
            setRatingGoodBad(goodFiles,true);
        }
        if (badFiles.exists()) {
            setRatingGoodBad(badFiles, false);
        }
    }

    /**
     * This method will read the provided text file for occurances of the files in this name. If it reads a line that
     * matches the path of a file in _files, it will update the rating of that file in the hashmap depending on the
     * boolen parameter
     * @param textFile The text file to read from
     * @param isGood Whether the text file indicates good or bad names
     */
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
    public List<File> getFiles() { return _files; }

    /**
     * Finds the first good file it can. If no good files then the first unrated file.
     * If all files are badly rated returns the first file.
     * @return an audio file.
     */
    public File getDefault() {
        for (File file: _files) {
            if (_filesToRatings.get(file) == 2) {
                return file;
            }
        }
        for (File file : _files) {
            if (_filesToRatings.get(file) == 0) {
                return file;
            }
        }
        return _files.get(0);
    }

    /**
     * This updates the filesToRatings hashmap.
     */
    public void updateRatingOfFile(File file, int rating){
        _filesToRatings.replace(file, rating);
        _rating = rating;
    }
}

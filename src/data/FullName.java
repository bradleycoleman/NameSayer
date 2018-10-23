package data;

import javax.swing.text.html.ListView;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Object representing a string of Names. The audio files used for each part of the name are set to a default value
 * to begin with, but can be changed if the user wishes for a different file to be used. This allows for two different
 * pronunciations of the same Name in a single fullName.
 */
public class FullName implements Comparable<Object>{
    private List<Name> _subNames;
    private List<File> _attempts;
    private List<File> _audioFiles;
    private String _name;

    /**
     * Constructor to be used if the audio files for each name are provided
     * @param name The String representation of this full name
     * @param subNames The Name objects that comprise this name, the order of the list is the order they will be said
     * @param audioFiles The audio files for the full name. The index of each of these matches the index of their
     *                   respective names in the subNames List
     */
    public FullName(String name, List<Name> subNames, List<File> audioFiles) {
        _subNames = subNames;
        _name = name;
        _audioFiles = audioFiles;
        _attempts = new ArrayList<>();
        findAttempts();
    }

    /**
     * Constructor to be used if the default audio files for each name are to be used
     */
    public FullName(String name, List<Name> subNames) {
        _name = name;
        _subNames = subNames;
        _audioFiles = new ArrayList<>();
        _attempts = new ArrayList<>();
        for (Name subName: _subNames) {
            _audioFiles.add(subName.getDefault());
        }
        findAttempts();
    }

    /**
     * Gets all the attempts of this fullname from the attempts folder
     */
    private void findAttempts() {
        File attemptsLoc = new File("userdata/attempts");
        if (!attemptsLoc.exists()) {
            attemptsLoc.mkdirs();
        }
        List<File> attempts = Arrays.asList(attemptsLoc.listFiles());
        for (File attempt: attempts) {
            // tries to find files that match the patter of the name seperated by underscores, ending in an underscore
            // and the int start of the date (number)
            Pattern p = Pattern.compile(_name.replaceAll("[ -]", "_") + "_\\d");
            if (p.matcher(attempt.getName()).find()) {
                _attempts.add(attempt);
            }
        }
    }
    /**
     * Starts a recording process in bash which will save a audio file whose name is Name + time
     * @throws InterruptedException FileCommands.record throws this, being a processbuilder
     */
    public void addAttempt() {
        DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH:mm:ss");
        String attemptName = (_name.replaceAll(" ","_") + "_" + dateFormat.format(new Date()));
        FileCommands.record(attemptName);
        // After the recording is completed, the file is added to this Name's attempts

        _attempts.add(new File("userdata/attempts/" + attemptName + ".wav"));
    }

    public void deleteAttempt(File attempt) {
        _attempts.remove(attempt);
        FileCommands.deleteFile(attempt);
    }

    /**
     * Changes the preferred file at the specified index of the Name. If the file is not one from the Name at that index
     * then the method does nothing.
     */
    public void setFileAtIndex(File audioFile, int i) {
        if (_subNames.get(i).getFiles().contains(audioFile)) {
            _audioFiles.set(i,audioFile);
        }
    }

    public List<File> getAudioFiles() {
        return _audioFiles;
    }
    public List<File> getAttempts() { return _attempts; }
    public List<Name> getSubNames() {
        return _subNames;
    }
    public String toString() { return _name; }


    public int compareTo(Object n) {
        return (toString().toLowerCase().compareTo(n.toString().toLowerCase()));
    }
}

package data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Object representing a string of Names. The audio files used for each part of the name are set to a default value
 * to begin with, but can be changed if the user wishes for a different file to be used. This allows for two different
 * pronunciations of the same Name in a single fullName.
 */
public class FullName {
    private List<Name> _subNames;
    private List<File> _attempts;
    private List<File> _audioFiles;
    private String _name;

    public FullName(String name, List<Name> subNames, List<File> audioFiles) {
        _subNames = subNames;
        _name = name;
        _audioFiles = audioFiles;
    }

    public FullName(String name, List<Name> subNames) {
        _name = name;
        _subNames = subNames;
        _audioFiles = new ArrayList<>();
        for (Name subName: _subNames) {
            _audioFiles.add(subName.getDefault());
        }
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
    public List<Name> getSubNames() {
        return _subNames;
    }
    public String toString() {
        return _name;
    }
}

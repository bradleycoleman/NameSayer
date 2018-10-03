package data;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Playlist {
    private List<List<Name>> _fullNames;
    private int _completion;
    private String _name;
    private File _file;
    private HashMap<List<Name>,List<File>> _preferredFiles;

    /**
     * Constructor to be used at startup by NameSayerModel
     * @param playlist
     * @param model
     */
    public Playlist(File playlist, NameSayerModel model) {
        _file = playlist;
        BufferedReader reader;
        _preferredFiles = new HashMap<>();
        try{
            reader = new BufferedReader(new FileReader(playlist));
            // first line is name of playlist
            _name = reader.readLine();
            // second is completion
            _completion = Integer.parseInt(reader.readLine());
            String line;
            // The list of names in the full name
            List<Name> names = new ArrayList<>();
            // The list of preffered files for each name in the full name
            List<File> files = new ArrayList<>();
            while((line = reader.readLine())!= null){
                if (line.equals("~")) {
                    // ~ signifies the end of a full name, so the lists are added then reset
                    _fullNames.add(names);
                    _preferredFiles.put(names,files);
                    names = new ArrayList<>();
                    files = new ArrayList<>();
                } else {
                    // if the line is not null and not a ~, then it is a part of a full name
                    // the line will be of the format "[name] [preffered file filepath]"
                    String[] splitLine = line.split(" ");
                    for (Name name: model.getDatabase()) {
                        if (name.toString().equals(splitLine[0])) {
                            // Adding this name to the latest full name
                            names.add(name);
                            // Allocating the preffered file.
                            files.add(new File(splitLine[1]));
                        }
                    }
                }
            }
        } catch(IOException e){
            e.printStackTrace();
            System.out.println("Incorrect format for playlist");
        }
    }

    /**
     * Constructor to be used from create playlist screen. Names are added one by one later
     * @param name
     */
    public Playlist(String name) {
        _name = name;
        _completion = 0;
        _preferredFiles = new HashMap<>();
        // The file is named as the playlist name without spaces
        _file = new File("userdata/playlists/" + _name.replaceAll(" ","_") + ".txt");
    }

    /**
     * method to add a new full name to the playlist. each name has its own list of preffered files to allow for different
     * pronunciations of the same name in the same playlist (e.g. if there are two recordings of the same name available,
     * with two different pronunciations, and the user wants to practice both in the same playlist).
     * @param fullName a list of names that make up a full name
     * @param prefFiles a list of files that correspond to each name
     */
    public void addName(List<Name> fullName, List<File> prefFiles) {
        _preferredFiles.put(fullName,prefFiles);
        _fullNames.add(fullName);
    }

    @Override
    public String toString() {
        return _name;
    }

    public void rename(String newName) {
        _name = newName;
    }

    public List<File> getPrefs(List<Name> fullName) {
        return _preferredFiles.get(fullName);
    }


    /**
     * Saves the .txt file with all the information about this playlist to be used later.
     */
    public void updateFile() {
        BufferedWriter writer;
        try{
            FileCommands.deleteFile(_file);
            writer = new BufferedWriter(new FileWriter(_file, true));
            writer.write(_name + "\n" + _completion + "\n");
            // a full name is a list of Name
            for(List<Name> fullName: _fullNames){
                // write each word of the full name
                for(int i = 0; i < fullName.size(); i++) {
                    writer.write(fullName.get(i) + " " + _preferredFiles.get(fullName).get(i) + "\n");
                }
                // end of this full name with the '~' character
                writer.write("~\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

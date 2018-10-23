package data;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Playlist {
    private List<FullName> _fullNames;
    private int _completion;
    private String _name;
    private NameSayerModel _nameSayerModel;
    private File _file;

    /**
     * Constructor to be used at startup by NameSayerModel
     * @param playlist
     * @param model
     */
    public Playlist(File playlist, NameSayerModel model) {
        _file = playlist;
        _nameSayerModel = model;
        _fullNames = new ArrayList<>();
        BufferedReader reader;
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
            // How the user wants the name to be seen
            String name = reader.readLine();
            while((line = reader.readLine())!= null){
                if (line.equals("~")) {
                    // ~ signifies the end of a full name, so the details are added then reset
                    _fullNames.add(new FullName(name, names, files));
                    // the first line after the ~ is how the user wants the name to read.
                    if ((line = reader.readLine())!= null) {
                        name = line;
                        names = new ArrayList<>();
                        files = new ArrayList<>();
                    }
                } else {
                    // if the line is not null and not a ~, then it is a part of a full name
                    // the line will be of the format "[subname] [preffered file filepath]"
                    String[] splitLine = line.split(" ");
                    for (Name subname: model.getDatabase()) {
                        if (subname.toString().equals(splitLine[0])) {
                            // Adding this name to the latest full name
                            names.add(subname);
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
        _fullNames = new ArrayList<>();
        // The file is named as the playlist name without spaces
        _file = new File("userdata/playlists/" + _name.replaceAll(" ","_") + ".txt");
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
            for(FullName fullName: _fullNames){
                // The first line is the way the name is written (to allow for custom '-' and capitalization)
                writer.write(fullName.toString() + "\n");
                // write each line for the fullName: each is a Name, and the corresponded audio file
                for(int i = 0; i < fullName.getAudioFiles().size(); i++) {
                    writer.write(fullName.getSubNames().get(i) + " " + fullName.getAudioFiles().get(i) + "\n");
                }
                // end of this full name with the '~' character
                writer.write("~\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCompletion(int completion) {
        _completion = completion;
        updateFile();
    }

    public void setNames(List<FullName> names) {
        _fullNames = names;
    }

    public String toString() {
        return _name;
    }

    public int getCompletion() {
        return _completion;
    }

    public void rename(String newName) {
        _name = newName;
    }

    public File getFile() {
        return _file;
    }

    public List<FullName> getFullNames() {
        return _fullNames;
    }
}

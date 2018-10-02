package data;

import java.io.*;
import java.util.HashMap;
import java.util.List;

public class Playlist {
    private List<Name> _names;
    private int _completion;
    private String _name;
    private File _file;
    private HashMap<Name,File> _preferredNames;

    /**
     * constructor to be used at startup by NameSayerModel
     * @param playlist
     * @param model
     */
    public Playlist(File playlist, NameSayerModel model) {
        _file = playlist;
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader(playlist));
            // first line is name of playlist
            _name = reader.readLine();
            // second is completion
            _completion = Integer.parseInt(reader.readLine());
            String line;
            while((line = reader.readLine())!= null){
                String[] splitLine = line.split(" ");
                for (Name name: model.getDatabase()) {
                    if (name.toString().equals(splitLine[0])) {
                        _names.add(name);
                        _preferredNames.put(name,new File(splitLine[1]));
                    }
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Constructor to be used from creat playlist screen
     * @param name
     * @param names
     */
    public Playlist(String name, List<Name> names) {
        _names = names;
        _name = name;
        _completion = 0;
        _file = new File("userdata/playlists/" + _name.replaceAll(" ","_") + ".txt");
        updateFile();
    }

    @Override
    public String toString() {
        return _name;
    }

    public void updateFile() {
        BufferedWriter writer;
        try{
            FileCommands.deleteFile(_file);
            writer = new BufferedWriter(new FileWriter(_file, true));
            writer.write(_name + "\n" + _completion + "\n");
            for(Name n: _names){
                writer.write(n + " " +_preferredNames.get(n));
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

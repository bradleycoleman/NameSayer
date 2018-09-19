package data;

import javafx.collections.FXCollections;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This class will contain information about the original database, the current playlist and the names to be displayed.
 */
public class NameSayerModel {

    private List<Name> _database = new ArrayList<Name>();
    private List<Name> _nameslist = new ArrayList<Name>();
    private List<Name> _filteredNameslist = new ArrayList<Name>();
    private List<Name> _playlist = new ArrayList<Name>();


    public NameSayerModel(){
        // load the database from the folder
        File folder = new File("names");
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            _database.add(new Name(file));
            System.out.println();
        }
        _database.sort(new Comparator<Name>() {
            @Override
            public int compare(Name o1, Name o2) {
                return (o1.compareTo(o2));
            }
        });

        _nameslist = _database;
    }

    // Getters
    public List<Name> getDatabase(){
        return _database;
    }
    public List<Name> getNameslist(){
        return _nameslist;
    }
    public List<Name> getPlaylist(){
        return _playlist;
    }
    public List<Name> getFilteredNamesList(){
        return _filteredNameslist;
    }

    /**
     * This method will place all the names that contain the string filter in the _filteredNameslist;
     * @param filter
     */
    public void filterNamesList(String filter){
        _filteredNameslist = new ArrayList<Name>();

        // Add all names that contain the text from the search bar
        for(Name n: _nameslist){
            if(n.toString().toLowerCase().contains(filter.toLowerCase())){
                _filteredNameslist.add(n);
            }
        }

        // Sort with priority of the search bar text appearing earlier in the name.
        _filteredNameslist.sort(new Comparator<Name>() {
            @Override
            public int compare(Name o1, Name o2) {
                String i = o1.toString().toLowerCase().indexOf(filter.toLowerCase())+"";
                String j = o2.toString().toLowerCase().indexOf(filter.toLowerCase())+"";
                return (i.compareTo(j));
            }
        });
    }
}

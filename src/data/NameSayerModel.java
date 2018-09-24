package data;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        File namesData = new File("names");
        if(!namesData.exists()){
            namesData.mkdirs();
        }
        List<File> listOfNamesData = new ArrayList<>();
        listOfNamesData = Arrays.asList(namesData.listFiles());

        // load the attempts from the folder
        File attemptsData = new File("userdata/attempts");
        if(!attemptsData.exists()){
            attemptsData.mkdirs();
        }
        List<File> listOfAttemptsData = new ArrayList<>();
        listOfAttemptsData = Arrays.asList(attemptsData.listFiles());


        List<String> readNames = new ArrayList<>();

        // For every name this finds all names that match it to make a list of files for the Name constructor
        // get number of files, n. while i < n, read listofnamesdata(0)
        for (File file1 : listOfNamesData) {
            List<File> database = new ArrayList<>();
            List<File> attempts = new ArrayList<>();
            database.add(file1);
            String name;
            Pattern p = Pattern.compile("_([a-zA-Z]*)\\.wav");
            Matcher m1 = p.matcher(file1.getName());
            if (m1.find()) {
                name = m1.group(1);
                // checking if this name has already been done
                if (!readNames.contains(name)) {
                    readNames.add(name);
                    for (File file2 : listOfNamesData) {
                        Matcher m2 = p.matcher(file2.getName());
                        if (m2.find() && !file2.equals(file1)) {
                            if (m2.group(1).equals(name)) {
                                database.add(file2);
                            }
                        }
                    }
                    // Finding all the recorded attempts of this name
                    for (File attempt : listOfAttemptsData) {
                        Pattern attemptpat = Pattern.compile(name + "_");
                        Matcher attemptmat = attemptpat.matcher(attempt.getName());
                        if (attemptmat.find()) {
                            attempts.add(attempt);
                        }
                    }
                    _database.add(new Name(name, database, attempts));
                }
            } else {
                // This will not have any other files with same name, as the name is the file name
                name = file1.getName();
                _database.add(new Name(name, database, attempts));
            }

        }
        _database.sort(new Comparator<Name>() {
            @Override
            public int compare(Name o1, Name o2) {
                return (o1.compareTo(o2));
            }
        });

        // Create ratings.txt if it has hasn't already been created.
        try{
            // load all the ratings data
            File ratingsData = new File("userdata/ratings.txt");
            if(!ratingsData.exists()) {
                ratingsData.createNewFile();
                writeAllRatingsData();
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        // Add all the rating data to the database
        readAllRatingsData();

        _nameslist.addAll(_database);
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

    /**
     * This method will initialize all ratings data into ratings.txt.
     */
    public void writeAllRatingsData(){

        BufferedWriter writer = null;
        try{
            FileCommands.deleteAudio(new File("userdata/ratings.txt"));
            File ratingsFile = new File("userdata/ratings.txt");
            writer = new BufferedWriter(new FileWriter(ratingsFile, true));

            for(Name n: _database){
                for(File f: n.getFiles()){
                    writer.write(f.getPath()+" "+n.getRating(f)+"\n");
                }
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will update the ratings data of the database.
     */
    public void readAllRatingsData(){
        BufferedReader reader = null;
        try{
            File ratingsFile = new File("userdata/ratings.txt");
            reader = new BufferedReader(new FileReader(ratingsFile));

            String line;
            while((line = reader.readLine())!= null){
                String[] splitLine = line.split(" ");
                updateDatabaseRating(splitLine[0], Integer.parseInt(splitLine[1]));
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * This method will update the rating of a specific name in the database.
     * @param path
     * @param rating
     */
    public void updateDatabaseRating(String path, int rating){
        for(Name n:_database){
            for(File f: n.getFiles()){
                if(f.getPath().equals(path)){
                    n.updateRatingOfFile(f, rating);
                    n.setRating(rating);
                }
            }
        }
    }


}

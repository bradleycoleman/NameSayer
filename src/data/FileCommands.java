package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class includes various static methods relating to file manipulation. Many of the methods use bash processes.
 */
public class FileCommands {
    private static Process _process;

    /**
     * removes the silence in a name recording
     * @param audioFile this file will used to make a version without silence named [filename].wav
     */
    public static void removeSilence(File audioFile) {
        int targetVol = -24;
        int change = 0;
        new File("userdata/fixed").mkdirs();
        // this pattern will find the mean volume on the line that is outputted
        Pattern p = Pattern.compile("-([\\d]*)\\.");
        bashProcess("ffmpeg -i names/"+ audioFile.getName() + " -filter:a volumedetect -f null /dev/null 2>&1 | grep mean_volume",
                null);
        // getting the input stream of the process
        BufferedReader br = new BufferedReader(new InputStreamReader(_process.getInputStream()));
        try {
            Matcher m = p.matcher(br.readLine());
            // The change to be used on the file is the difference from the mean to the target volume
            if (m.find()) {
                change = targetVol + Integer.parseInt(m.group(1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        bashProcess("ffmpeg -y -i names/"+ audioFile.getName() + " -filter:a \"volume=" + change + "dB\" " +
                        "userdata/fixed/loud" + audioFile.getName(), null);
        System.out.println("Performing intial fix on " + audioFile.getName());
        bashProcess("ffmpeg -i userdata/fixed/loud" + audioFile.getName() + " -af silenceremove=1:0:-40dB userdata/fixed/fix" + audioFile.getName(),
                null);
    }


    /**
     * Using bash, this records audio until it is cancelled, saving the result to a file of the name given
     * @param name The name of the file
     */
    public static void record(String name) {
        // Making a directory for the attempts, will not make directory if it already exists.
        new File("userdata/attempts").mkdirs();
        // Recording until the process is cancelled
        bashProcess("ffmpeg -f alsa -i default $\""+name+"\".wav", new File("userdata/attempts"));
    }

    private static void bashProcess(String command, File directory) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            processBuilder.directory(directory);
            _process = processBuilder.start();
            _process.waitFor();
        }
        catch (IOException | InterruptedException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * This method ends the recording process in bash
     */
    public static void cancelRecording() {
        _process.destroy();
    }


    /**
     * This method will delete the file, if it exists
     * @param file the file to be deleted
     */
    public static void deleteFile(File file) {
        try {
            Files.deleteIfExists(Paths.get(file.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

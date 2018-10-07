package data;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class includes various static methods relating to file manipulation. Many of the methods use bash processes
 */
public class FileCommands {
    private static Process _process;

    /**
     * removes the silence in a name recording
     * @param audioFile this file will used to make a version without silence named [filename].wav
     */
    public static void removeSilence(File audioFile) {
        new File("userdata/fixed").mkdirs();
        bashProcess("ffmpeg -i names/" + audioFile.getName() +" -filter:a loudnorm userdata/fixed/loud" + audioFile.getName(),
                null);
        System.out.println("created" +audioFile.getName() + "loud");
        bashProcess("ffmpeg -i userdata/fixed/loud" + audioFile.getName() + " -af silenceremove=1:0:-60dB userdata/fixed/fix" + audioFile.getName(),
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

    /**
     * Using bash, this records five seconds of audio, saving the result to a file to userdata/test.wav
     */
    public static void recordTest() {
        // Making a directory for the test, will not make directory if it already exists.
        new File("userdata").mkdirs();
        // recording for 5s
        bashProcess("ffmpeg -f alsa -i default -t 5 test.wav", new File("userdata"));
    }

    private static void bashProcess(String command, File directory) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            processBuilder.directory(directory);
            _process = processBuilder.start();
            _process.waitFor();
            _process.destroy();
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
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n", Paths.get(file.getPath()));
        } catch (IOException x) {
            System.err.println(x);
        }
    }


}

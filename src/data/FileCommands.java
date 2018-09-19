package data;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileCommands {
    private static Process _recordingProcess;



    /**
     * Using bash, this records audio until it is cancelled, saving the result to a file of the name given
     * @param name The name of the file
     */
    public static void record(String name) throws InterruptedException {
        // Making a directory for the attempts, will not make directory if it already exists.
        new File("userdata/attempts").mkdirs();

        // Recording until the process is cancelled
        try {
            ProcessBuilder recordingProcessBuilder = new ProcessBuilder("bash", "-c", "ffmpeg -f alsa -i default $\""+name+"\".wav");
            recordingProcessBuilder.directory(new File("userdata/attempts"));
            _recordingProcess = recordingProcessBuilder.start();
            _recordingProcess.waitFor();
            _recordingProcess.destroy();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * This method ends the recording process in bash
     */
    public static void cancelRecording() {
        _recordingProcess.destroy();
    }

    /**
     * This method will delete the audio file related to the name entered, if it exists.
     * @param name [name].wav will be deleted from "creations"
     */
    public static void deleteAudio(String name) {
        try {
            Files.deleteIfExists(Paths.get("userdata/attempts/"+name+".wav"));
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n", Paths.get("userdata/attempts/"+name+".wav"));
        } catch (IOException x) {
            System.err.println(x);
        }
    }


}

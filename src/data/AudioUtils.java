package data;


import javafx.concurrent.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

public class AudioUtils {

    private Thread _soundThread;
    private AudioStream _clip;

    public AudioUtils() {
        // Don't need to initialize anything. (Yet).
    }

    /**
     * Plays an entire full name
     *
     * @param fullName
     */
    public void playFullName(FullName fullName) {
        List<File> files = fullName.getAudioFiles();
        for (File f : files) {
            playFile(f);
        }
    }

    /**
     * Plays a sound through a file
     *
     * @param file
     */
    public void playFile(File file) {
        try {
            InputStream inputStream = new FileInputStream(file);
            _clip = new AudioStream(inputStream);
            AudioPlayer.player.start(_clip);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Plays consecutive sounds through a file list
     *
     * @param files
     */
    public void playFiles(List<File> files) {
        Task audio = new Task() {
            @Override
            protected Object call() {
                for(File f: files){
                    playFile(f);
                    try {
                        Thread.sleep(_clip.getLength()/100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
        new Thread(audio).start();
    }

    public int getClipLength(File file) {
        try {
        	InputStream inputStream = new FileInputStream(file);
            _clip = new AudioStream(inputStream);
            return _clip.getLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public AudioStream getClip(){
        return _clip;
    }
}

package data;


import javafx.concurrent.Task;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
            _clip = new AudioStream(new FileInputStream(file));
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
                    System.out.println(_clip.getLength());
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
            Clip clip = AudioSystem.getClip();
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(file);
            clip.open(inputStream);
            return clip.getFrameLength();
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public AudioStream getClip(){
        return _clip;
    }
}
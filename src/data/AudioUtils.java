package data;


import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class AudioUtils {

    private Thread soundThread;

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
        soundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(file);
                    clip.open(inputStream);
                    clip.start();
                    while(clip.getMicrosecondLength() != clip.getMicrosecondPosition())
                    {
                        // Wait until the clip finishes to finish the thread.
                    }
                } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            soundThread.start();
            soundThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Plays consecutive sounds through a file list
     *
     * @param files
     */
    public void playFiles(List<File> files) {
        for(File f: files){
            playFile(f);
        }
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
}

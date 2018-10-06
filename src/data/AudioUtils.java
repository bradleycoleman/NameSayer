package data;


import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class AudioUtils {

    private Thread _soundThread;
    private Clip _clip;

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
        _soundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    _clip = AudioSystem.getClip();
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(file);
                    _clip.open(inputStream);
                    _clip.start();
                    while(_clip.getMicrosecondLength() != _clip.getMicrosecondPosition())
                    {
                        // Wait until the clip finishes to finish the thread.
                    }
                } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            _soundThread.start();
            _soundThread.join();
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

    public Clip getClip(){
        return _clip;
    }
}

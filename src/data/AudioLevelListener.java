package data;

import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import main.Main;

/**
 * This class will read the microphone level in real time.
 * References:
 * https://docs.oracle.com/javase/tutorial/sound/capturing.html
 * https://stackoverflow.com/questions/3899585/microphone-level-in-java
 *
 */
public class AudioLevelListener {
	private int level;
	private boolean stopCapture = false;
	private Main main;

	/**
	 * The constructor for AudioLevelListener. When constructed, AudioLevelListener will start listening.
	 * Most of this code is taken from the Oracle Capturing Audio tutorial.
	 * The tutorial can be found at:
	 * 		https://docs.oracle.com/javase/tutorial/sound/capturing.html
	 */
	public AudioLevelListener(Main main) {
		this.main = main;
		
		AudioFormat format = new AudioFormat(8000, 8, 1, true, true);
		
		TargetDataLine line = null;
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, 
		   format); // format is an AudioFormat object
		if (!AudioSystem.isLineSupported(info)) {
		    // Handle the error ...
		}
		// Obtain and open the line.
		try {
		    line = (TargetDataLine) AudioSystem.getLine(info);
		    line.open(format);
		} catch (LineUnavailableException e) {
		    e.printStackTrace();
		}
		
		// Assume that the TargetDataLine, line, has already
		// been obtained and opened.
		ByteArrayOutputStream out  = new ByteArrayOutputStream();
		int numBytesRead;
		byte[] data = new byte[line.getBufferSize() / 5];

		// Begin audio capture.
		line.start();

		// Here, stopped is a global boolean set by another thread.
		while (!stopCapture) {
		   // Read the next chunk of data from the TargetDataLine.
		   numBytesRead =  line.read(data, 0, data.length);
		   level = calculateRMSLevel(data);
		   main.getBrowseScreenController().getMicLevelBar().setProgress((double)level/10);
		}     
	}

	/**
	 * Will calculate the amplitude of a section of audio data using the Root Mean Squared method.
	 * This code is mainly taken from user Ascherer in a stack overflow post (linked below).
	 * 		https://stackoverflow.com/questions/3899585/microphone-level-in-java
	 * @param audioData
	 * @return
	 */
	public int calculateRMSLevel(byte[] audioData){ 
	    long lSum = 0;
	    for(int i=0; i < audioData.length; i++) {
	        lSum = lSum + audioData[i];
	    }

	    double dAvg = lSum / audioData.length;
	    double sumMeanSquare = 0d;

	    for(int j=0; j < audioData.length; j++) {
	    	sumMeanSquare += Math.pow(audioData[j] - dAvg, 2d);
	    }
	        
	    double averageMeanSquare = sumMeanSquare / audioData.length;

	    return (int)(Math.pow(averageMeanSquare,0.5d) + 0.5);
	}
	
	public int getLevel() {
		return level;
	}
	
	public void stopCapture() {
		stopCapture = true;
	}
}

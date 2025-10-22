package controller;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * This class manages audio playback, including background music and sound
 * effects.
 * 
 * @author Lorenzo Sabatino
 */
public class AudioManager {

	private static AudioManager instance;
	private Clip bgClip;
	private boolean isMuted = false;

	/**
	 * Returns the singleton instance of the AudioManager class.
	 * 
	 * @return The AudioManager instance.
	 */
	public static AudioManager getInstance() {
		if (instance == null)
			instance = new AudioManager();
		return instance;
	}

	private AudioManager() {

	}

	/**
	 * Plays an audio file.
	 * 
	 * @param filename The path of the audio file to be played.
	 */
	public void play(String filename) {

		try {
			InputStream in = new BufferedInputStream(new FileInputStream(filename));
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(in);
			Clip clip = AudioSystem.getClip();
			clip.open(audioIn);

			if (filename.endsWith("background.wav")) {
				bgClip = clip;
				float volume = isMuted ? 0.0f : 1.0f;
				setVolume(clip, volume);
				clip.addLineListener(new LineListener() {
					public void update(LineEvent event) {
						if (event.getType() == LineEvent.Type.STOP) {
							clip.setFramePosition(0);
							clip.start();
						}
					}
				});
			}

			clip.start();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Sets the volume of a Clip.
	 * 
	 * @param clip   The Clip to set the volume for.
	 * @param volume The volume level to set (0.0 to 1.0).
	 */
	public void setVolume(Clip clip, float volume) {
		if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
			gainControl.setValue(dB);
		}
	}

	/**
	 * Checks if the audio is currently muted.
	 * 
	 * @return True if audio is muted, false otherwise.
	 */
	public boolean isMuted() {
		return isMuted;
	}

	/**
	 * Sets the audio mute status.
	 * 
	 * @param isMuted True to mute audio, false to unmute.
	 */
	public void setMuted(boolean isMuted) {
		this.isMuted = isMuted;
	}

	/**
	 * Gets the background audio Clip.
	 * 
	 * @return The background audio Clip.
	 */
	public Clip getBgClip() {
		return bgClip;
	}
}
package toolbox;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import toolbox.utils.FileHandler;

public class Sound {

	private Clip clip;
	
	public Sound(String path) {
		clip = FileHandler.loadSound(path);
	}

	public void play() {
		try {
			new Thread() {
				public void run() {
			        try {
			        	if (!isPlaying()) {
			        		reset();
			        		clip.start();
			        	}
			        }
			        catch (Exception e) {
			            e.printStackTrace();
			        };
				}
			}.start();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		clip.stop();
	}
	
    // TODO: add pitch control
	public void reset() {
		clip.setFramePosition(0);
	}
	
	// public void setVolume(float volume) {
	//     if (volume < 0f || volume > 1f)
	//         throw new IllegalArgumentException("Volume not valid: " + volume);
	//     FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);        
	//     gainControl.setValue(20f * (float) Math.log10(volume));
	// }
	
	public boolean isPlaying() {
		return clip.isRunning();
	}
}
package toolbox.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class FileHandler {

    public static String loadText(String path) {
        try {
            Scanner scanner = new Scanner(new File(path));

            String content = "";
            while (scanner.hasNext()) {
                content += scanner.nextLine() + "\n";
            }

            scanner.close();

            return content;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] loadLines(String path) {
        try {
            Scanner scanner = new Scanner(new File(path));

            ArrayList<String> linesList = new ArrayList<String>();
            while (scanner.hasNext()) {
                linesList.add(scanner.nextLine());
            }

            scanner.close();

            String[] lines = new String[linesList.size()];
            for (int i = 0; i < lines.length; i++) {
                lines[i] = linesList.get(i);
            }

            return lines;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedImage loadImage(String path) {
        BufferedImage result = null;
        try {
            result = ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /** Only works with .wav files **/
    public static Clip loadSound(String path) {
        try {
			Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File(path)));
            
            return clip;
		} catch (Throwable e) {
			e.printStackTrace();
		}

        return null;
    }
}

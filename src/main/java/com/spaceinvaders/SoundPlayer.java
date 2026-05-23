package com.spaceinvaders;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

/**
 * Utility class for playing game sounds.
 */
public class SoundPlayer {
    private static Clip backgroundMusicClip = null;
    
    private SoundPlayer() {
    }

    public static void playShoot() {
        playTone(850, 50, 0.40);
    }

    public static void playHit() {
        playTone(220, 70, 0.60);
    }

    public static void playGameOver() {
        playTone(120, 320, 0.55);
    }
    
    public static void playBattle() {
        stopBackgroundMusic();
        playWavFileAsBackgroundMusic("resouce/戰鬥.wav");
    }
    
    public static void playBoss() {
        stopBackgroundMusic();
        playWavFileAsBackgroundMusic("resouce/boss.wav");
    }
    
    public static void playDefeat() {
        playWavFile("resouce/戰敗.wav");
    }
    
    public static void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
            backgroundMusicClip.close();
            backgroundMusicClip = null;
        }
    }
    
    private static void playWavFileAsBackgroundMusic(final String filePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File soundFile = new File(filePath);
                    if (!soundFile.exists()) {
                        System.err.println("Sound file not found: " + filePath);
                        return;
                    }
                    backgroundMusicClip = AudioSystem.getClip();
                    backgroundMusicClip.open(AudioSystem.getAudioInputStream(soundFile));
                    backgroundMusicClip.start();
                } catch (Exception e) {
                    System.err.println("Error playing sound: " + filePath);
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    private static void playWavFile(final String filePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File soundFile = new File(filePath);
                    if (!soundFile.exists()) {
                        System.err.println("Sound file not found: " + filePath);
                        return;
                    }
                    Clip clip = AudioSystem.getClip();
                    clip.open(AudioSystem.getAudioInputStream(soundFile));
                    clip.start();
                } catch (Exception e) {
                    System.err.println("Error playing sound: " + filePath);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void playTone(final int hz, final int ms, final double volume) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int sampleRate = 44100;
                    int numSamples = (int) ((ms / 1000.0) * sampleRate);
                    byte[] data = new byte[numSamples];

                    for (int i = 0; i < data.length; i++) {
                        double angle = i / (sampleRate / (double) hz) * 2.0 * Math.PI;
                        data[i] = (byte) (Math.sin(angle) * 127.0 * volume);
                    }

                    AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
                    Clip clip = AudioSystem.getClip();
                    clip.open(format, data, 0, data.length);
                    clip.start();
                } catch (Exception ignored) {
                    // Keep game playable even if sound is unavailable.
                }
            }
        }).start();
    }
}

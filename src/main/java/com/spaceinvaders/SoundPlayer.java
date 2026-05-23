package com.spaceinvaders;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

/**
 * Utility class for playing game sounds.
 */
public class SoundPlayer {
    private static Clip backgroundMusicClip = null;
    private static final List<Clip> activeClips = Collections.synchronizedList(new ArrayList<>());
    
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

    public static void playMenu() {
        stopBackgroundMusic();
        playWavFileAsBackgroundMusic("resouce/主畫面背景.wav");
    }
    
    public static void playDefeat() {
        playWavFile("resouce/戰敗.wav", false);
    }
    
    public static void stopAllSounds() {
        synchronized (activeClips) {
            for (Clip clip : new ArrayList<>(activeClips)) {
                if (clip != null && clip.isRunning()) {
                    clip.stop();
                    clip.close();
                }
            }
            activeClips.clear();
        }
        backgroundMusicClip = null; // Also reset background music clip reference
    }

    public static void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
            backgroundMusicClip.close();
            activeClips.remove(backgroundMusicClip);
            backgroundMusicClip = null;
        }
    }
    
    private static void playWavFileAsBackgroundMusic(final String filePath) {
        stopBackgroundMusic(); // Stop previous background music first
        playWavFile(filePath, true);
    }
    
    private static void playWavFile(final String filePath) {
        playWavFile(filePath, false);
    }

    private static void playWavFile(final String filePath, final boolean isLooping) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File soundFile = new File(filePath);
                    if (!soundFile.exists()) {
                        System.err.println("Sound file not found: " + filePath);
                        return;
                    }
                    final Clip clip = AudioSystem.getClip();
                    
                    // Add listener to remove clip when it stops
                    clip.addLineListener(new LineListener() {
                        @Override
                        public void update(LineEvent event) {
                            if (event.getType() == LineEvent.Type.STOP) {
                                clip.close();
                                synchronized (activeClips) {
                                    activeClips.remove(clip);
                                }
                            }
                        }
                    });

                    clip.open(AudioSystem.getAudioInputStream(soundFile));
                    
                    synchronized (activeClips) {
                        activeClips.add(clip);
                    }

                    if (isLooping) {
                        backgroundMusicClip = clip;
                        clip.loop(Clip.LOOP_CONTINUOUSLY);
                    } else {
                        clip.start();
                    }
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

package org.example;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class MusicManager {

    public static final int MINIMUM_VOLUME_LEVEL_PERCENT = 0;
    public static final int MAXIMUM_VOLUME_LEVEL_PERCENT = 100;
    public static final int DEFAULT_VOLUME_LEVEL_PERCENT = 70;

    private boolean musicEnabled;
    private String currentTrackType;
    private int volumeLevelPercent;
    private boolean currentlyPlaying;

    private Clip currentClip;

    public MusicManager() {
        this.musicEnabled = true;
        this.currentTrackType = null;
        this.volumeLevelPercent = DEFAULT_VOLUME_LEVEL_PERCENT;
        this.currentlyPlaying = false;
        this.currentClip = null;
    }

    public void playTrack(String musicTrackType, String audioFilePath) {
        if (!musicEnabled) return;

        stopMusic();

        try {
            var url = getClass().getClassLoader().getResource(audioFilePath);

            if (url == null) {
                System.out.println("Audio file not found: " + audioFilePath);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);

            currentClip = AudioSystem.getClip();
            currentClip.open(audioStream);

            applyVolume();
            currentClip.start();

            this.currentTrackType = musicTrackType;
            this.currentlyPlaying = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopMusic() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
        }
        currentlyPlaying = false;
    }

    public void pauseMusic() {
        if (currentClip != null && currentlyPlaying) {
            currentClip.stop();
            currentlyPlaying = false;
        }
    }

    public void resumeMusic() {
        if (currentClip != null && musicEnabled && !currentlyPlaying) {
            currentClip.start();
            currentlyPlaying = true;
        }
    }

    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;

        if (!musicEnabled) {
            stopMusic();
        }
    }

    public void setVolumeLevelPercent(int volumeLevelPercent) {
        validateVolumeLevelPercent(volumeLevelPercent);
        this.volumeLevelPercent = volumeLevelPercent;
        applyVolume();
    }

    public boolean getMusicEnabled() {
        return musicEnabled;
    }

    public int getVolumeLevelPercent() {
        return volumeLevelPercent;
    }

    public String getCurrentTrackType() {
        return currentTrackType;
    }

    private void validateVolumeLevelPercent(int volumeLevelPercent) {
        if (volumeLevelPercent < MINIMUM_VOLUME_LEVEL_PERCENT ||
                volumeLevelPercent > MAXIMUM_VOLUME_LEVEL_PERCENT) {
            throw new IllegalArgumentException("Volume must be between 0 and 100");
        }
    }

    private void applyVolume() {
        if (currentClip == null) return;

        try {
            FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);

            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();

            float volume = min + (max - min) * (volumeLevelPercent / 100.0f);
            gainControl.setValue(volume);

        } catch (IllegalArgumentException e) {
            System.out.println("Volume control not supported");
        }
    }
}

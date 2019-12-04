package com.github.walterfan.potato.common.util;

import lombok.extern.slf4j.Slf4j;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Author: Walter Fan
 **/
@Slf4j
public class SoundPlayer {

    public static void playSound(String soundFile) throws IOException {

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(soundFile);
        if(null != inputStream) {
            AudioStream audioStream = new AudioStream(inputStream);
            AudioPlayer.player.start(audioStream);
        }

    }

    public static void main(String[] args) {
        String audioFile = "cat.wav";

        try {
            playSound(audioFile);
        } catch (IOException e) {
            log.error("play error", e);
        }
    }

}

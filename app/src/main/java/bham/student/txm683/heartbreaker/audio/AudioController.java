package bham.student.txm683.heartbreaker.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import bham.student.txm683.heartbreaker.R;

import java.util.HashMap;

public class AudioController {

    private SoundPool soundPool;

    private HashMap<AudioCode, Integer> soundMap;

    public AudioController(int poolSize){
        soundMap = new HashMap<>();

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder().setMaxStreams(poolSize).setAudioAttributes(attributes).build();
    }

    public void initSounds(Context context){
        soundMap.put(AudioCode.COLLISION, soundPool.load(context, R.raw.clack, 1));
    }

    public void releaseResources(){
        soundPool.release();
    }

    public void play(AudioCode audioCode){
        if (soundMap.containsKey(audioCode))
            soundPool.play(soundMap.get(audioCode), 1, 1, 0, 0, 1.0f);
    }
}

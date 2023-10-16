package com.wook.web.lighten.nanumAED.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import com.wook.web.lighten.nanumAED.R;

public class SoundPoolHandler {
    Context context;
    private static final int streamType = AudioManager.STREAM_MUSIC;
    private AudioManager audioManager;
    private int maxVolumeIndex;
    private float volume;
    private SoundPool soundPool;
    private boolean isLoaded = false;
    private int soundBeep = 0;

    public SoundPoolHandler(Context context){
        this.context = context;
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolumeIndex = audioManager.getStreamVolume(streamType);
        maxVolumeIndex = audioManager.getStreamMaxVolume(streamType);
        volume = (float) currentVolumeIndex / (float)maxVolumeIndex;
        if(Build.VERSION.SDK_INT >= 21){
            AudioAttributes attr =  new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setAudioAttributes(attr).setMaxStreams(2);
            soundPool = builder.build();
        }else{
            soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        }
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> isLoaded = true);
        soundBeep = soundPool.load(context, R.raw.beep, 1);
    }
    public void playSound(){
        if(isLoaded)
            soundPool.play(soundBeep, volume, volume, 1, 0, 1f);
    }
}

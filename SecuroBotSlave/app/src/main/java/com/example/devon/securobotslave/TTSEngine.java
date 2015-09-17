package com.example.devon.securobotslave;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Devon on 7/5/2015.
 */
public class TTSEngine implements TextToSpeech.OnInitListener{
    public TextToSpeech t1;
    public boolean isSpeaking = false;
    public Handler mHandler;
    String speakString;
    int queueMode;
    HashMap<String, String> params;

    public TTSEngine(Context c) {
        t1 = new TextToSpeech(c, this);
        mHandler = new Handler();
    }

    public void onPause(){
        if(t1 !=null){
            Log.d("TTS", "Shutting down TTS...");
            t1.stop();
            t1.shutdown();
        }
    }

    public void onResume(Context c) {
        t1 = new TextToSpeech(c, this);
    }

    @Override
    public void onInit ( int status){
        if (status == TextToSpeech.SUCCESS) {
            Log.d("TTS", "Success!");
            t1.setLanguage(Locale.US);

            t1.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                @Override
                public void onUtteranceCompleted(String utteranceId) {
                    Log.i("Finished Speaking", utteranceId);
                    if(utteranceId.equals("AIFinished")) {
                        isSpeaking = false;
                    }
                }
            });

        }
    }

    public void speak(String string, int queueMode, HashMap<String, String> params){
        speakString = string;
        this.queueMode = queueMode;
        this.params = params;
        speakRunnable.run();
    }

    private Runnable speakRunnable = new Runnable() {
        @Override
        public void run() {
            while(t1.isSpeaking()); //wait until we finish speaking before saying something else
            if(!t1.isSpeaking()){
                //TODO: Add a one shot timer here to wait for a few ms for the TTS engine to begin speaking
                params = new HashMap<String, String>();
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"AIFinished");
                t1.speak(speakString, queueMode, params);
                isSpeaking = true;
            }
        }
    };
}

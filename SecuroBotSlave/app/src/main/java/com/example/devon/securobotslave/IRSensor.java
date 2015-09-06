package com.example.devon.securobotslave;

import android.util.Log;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * Created by Devon on 7/5/2015.
 */
public class IRSensor {
    public AnalogInput input;
    public int pin;
    public float baseValue=0, baseVolt=0;
    public final int iSamples = 10;    //number of samples to take when initializing
    private final float valThresh = .03f;   //.05
    private final float volThresh = 0.1f;   //.1
    private final float minVal = .06f;
    private final float minVolt = .2f;

    public IRSensor(int pin){
        this.pin = pin;
    }

    public void initialize(float baseValue, float baseVolt){
        if(baseValue>=minVal){
            this.baseValue = baseValue;
        }
        else{
            this.baseValue = minVal;
        }

        if(baseVolt>=minVolt){
            this.baseVolt = baseVolt;
        }
        else {
            this.baseVolt = minVolt;
        }
    }

    public boolean motionDetect(float measuredVal, float measuredVolt) throws ConnectionLostException, InterruptedException {
        //Log.d("Sensors", "meas Val: " + measuredVal + ", measVol: " + measuredVolt);
        boolean valTrig = false;
        boolean volTrig = false;

        if((measuredVal >= minVal) && (measuredVal>(baseValue+valThresh) || measuredVal<(baseValue-valThresh))) {
            valTrig=true;
            Log.d("Sensors", "Value Trig. Base = " + baseValue + ", Measure = " + measuredVal + ", delta = " + Math.abs(measuredVal-baseValue));
        }
        if((measuredVolt >= minVolt) && (measuredVolt>(baseVolt+volThresh) || measuredVolt<(baseVolt-volThresh))){
            volTrig = true;
            Log.d("Sensors", "Voltage Trig. Base = " + baseVolt + ", Measure = " + measuredVolt + ", delta = " + Math.abs(measuredVolt-baseVolt));
        }
        return valTrig || volTrig;
    }
}

/*

0.088954054, measVol: 0.29354838
09-05 19:22:23.149  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.09384164, measVol: 0.30967742
09-05 19:22:23.249  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.086021505, measVol: 0.28387097
09-05 19:22:23.349  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.086021505, measVol: 0.28387097
09-05 19:22:23.450  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08504399, measVol: 0.28064516
09-05 19:22:23.551  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08406647, measVol: 0.27741936
09-05 19:22:23.651  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08211144, measVol: 0.27096775
09-05 19:22:23.751  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08308896, measVol: 0.27419356
09-05 19:22:23.852  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08113392, measVol: 0.26774192
09-05 19:22:23.952  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08406647, measVol: 0.27741936
09-05 19:22:24.052  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08504399, measVol: 0.28064516
09-05 19:22:24.153  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08406647, measVol: 0.27741936
09-05 19:22:24.253  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08211144, measVol: 0.27096775
09-05 19:22:24.357  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08211144, measVol: 0.27096775
09-05 19:22:24.457  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.086021505, measVol: 0.28387097
09-05 19:22:24.557  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08211144, measVol: 0.27096775
09-05 19:22:24.758  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08308896, measVol: 0.27419356
09-05 19:22:24.859  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.086021505, measVol: 0.28387097
09-05 19:22:24.959  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08308896, measVol: 0.27419356
09-05 19:22:25.060  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08211144, measVol: 0.27096775
09-05 19:22:25.161  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.079178885, measVol: 0.2612903
09-05 19:22:25.262  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08504399, measVol: 0.28064516
09-05 19:22:25.362  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08406647, measVol: 0.27741936
09-05 19:22:25.463  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.086021505, measVol: 0.28387097
09-05 19:22:25.563  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08406647, measVol: 0.27741936
09-05 19:22:25.663  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08113392, measVol: 0.26774192
09-05 19:22:25.763  29439-30403/com.example.devon.securobotslave D/Sensors﹕ meas Val: 0.08308896, measVol: 0.27419356
 */
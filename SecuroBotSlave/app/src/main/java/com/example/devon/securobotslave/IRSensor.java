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

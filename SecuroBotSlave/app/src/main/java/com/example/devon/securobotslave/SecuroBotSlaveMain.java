package com.example.devon.securobotslave;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import java.lang.Math.*;


public class SecuroBotSlaveMain extends IOIOActivity {
    private static final int REQUEST_ACTION_PICK = 1;
    private static final int REQUEST_WEB_PAGE = 2;
    private Handler mHandler;
    private Random r = new Random();
    boolean actionEnable = true;
    TTSEngine t1;
    ActionEngine action;
    int currentPos = 1550;
    //**********************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_securo_bot_slave_main);

        t1 = new TTSEngine(this);
        action = new ActionEngine(t1);
        mHandler = new Handler();

        startRepeatingTask();
    }
//**************************************************************************************************
                                        //Threads
//**************************************************************************************************
    void startRepeatingTask() {
        fetchContent.run();
        populateContent.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(fetchContent);
        mHandler.removeCallbacks(populateContent);
    }

    Runnable fetchContent = new Runnable() {
        @Override
        public void run() {
            if(action!=null) {
                Log.d("FetchContent", "Fetching Twitter Content Now...");
                action.fetchContent();
                Log.d("FetchContent", "Fetched Twitter Content.");
                mHandler.postDelayed(fetchContent, 930000);
            }
            else mHandler.postDelayed(fetchContent, 50);
        }
    };

    Runnable populateContent = new Runnable() {
        @Override
        public void run() {
            if(action!=null) {
                if(action.twitE.getContentIsFetched()) {
                    Log.d("PopulateContent", "Placing content now...");
                    action.populateContent();
                    action.twitE.setContentFetched(false);
                    Log.d("PopulateContent", "Placed content in designated classes");
                }
                mHandler.postDelayed(populateContent, 1000);
            }
            else mHandler.postDelayed(populateContent, 50);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if(t1 !=null){
            //t1.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //t1.onResume(this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ACTION_PICK:
                if (resultCode == RESULT_OK) {
                    int actionType = data.getIntExtra("action", -1);

                    switch (actionType) {
                        case ActionEngine.ACTION_PAGE:
                            action.executePage();
                            Intent articleIntent = new Intent(SecuroBotSlaveMain.this, WebPageActivity.class);
                            articleIntent.putExtra("URL", action.getWebPage());
                            startActivityForResult(articleIntent, REQUEST_WEB_PAGE);
                            actionEnable = false;
                            break;
                        case ActionEngine.ACTION_QUIZ:
                            action.executeQuiz();
                            Intent quizIntent = new Intent(SecuroBotSlaveMain.this, WebPageActivity.class);
                            quizIntent.putExtra("URL", action.getQuiz());
                            startActivityForResult(quizIntent, REQUEST_WEB_PAGE);
                            actionEnable = false;
                            break;
                        case ActionEngine.ACTION_JOKE:
                            action.executeActivity(ActionEngine.ACTION_JOKE);
                            actionEnable = true;
                            break;
                        case ActionEngine.ACTION_TIP:
                            action.executeActivity(ActionEngine.ACTION_TIP);
                            actionEnable = true;
                            break;
                        case ActionEngine.ACTION_RSS:
                            action.executeActivity(ActionEngine.ACTION_RSS);
                            actionEnable = true;
                            break;
                        case ActionEngine.ACTION_TWEET:
                            action.executeActivity(ActionEngine.ACTION_TWEET);
                            actionEnable = true;
                            break;
                        case ActionEngine.ACTION_HACKED:
                            actionEnable = true;
                        case ActionEngine.ACTION_PICTURE:
                            actionEnable = true;
                        default:
                            Log.d("Action Chooser", "Uknown command: " + actionType);
                            actionEnable = true;
                            break;
                    }
                } else {
                    actionEnable = true;
                }
                break;
            case REQUEST_WEB_PAGE:
                actionEnable = true;
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(actionEnable) {
            action.executeGreeting();
            Intent pickActionIntent = new Intent(SecuroBotSlaveMain.this, ActivityChooser.class);
            startActivityForResult(pickActionIntent, REQUEST_ACTION_PICK);
            actionEnable = false;
            Log.d("Main", "Touch detected!");
        }
        return false;
    }

//**************************************************************************************************
    //Android IOIO stuff
//**************************************************************************************************
    /**
     * This is the thread on which all the IOIO activity happens. It will be run
     * every time the application is resumed and aborted when it is paused. The
     * method setup() will be called right after a connection with the IOIO has
     * been established (which might happen several times!). Then, loop() will
     * be called repetitively until the IOIO gets disconnected.
     */
    class Looper extends BaseIOIOLooper {
        /** The on-board LED. */
        private DigitalOutput led_;
        private IRSensor iRSensors = new IRSensor(33);
        private PwmOutput pwm;
        int newPos;    //set the initial position (looking straight forward)

        /**
         * Called every time a connection with IOIO has been established.
         * Typically used to open pins.
         *
         * @throws ConnectionLostException
         *             When IOIO connection is lost.
         *
         * @see ioio.lib.util.IOIOLooper
         */
        @Override
        protected void setup() throws ConnectionLostException, InterruptedException {
            showVersions(ioio_, "IOIO connected!");
            led_ = ioio_.openDigitalOutput(0, true);
            iRSensors.input = ioio_.openAnalogInput(iRSensors.pin);
            initIR();

            try {
                pwm= ioio_.openPwmOutput(35, 100);  //new DigitalOutput.Spec(35, DigitalOutput.Spec.Mode.OPEN_DRAIN)
                pwm.setPulseWidth(currentPos);
            } catch (ConnectionLostException e) {
                Log.d("Connection Lost", "IO Connection Lost");
            }
        }

        /**
         * Called repetitively while the IOIO is connected.
         *
         * @throws ConnectionLostException
         *             When IOIO connection is lost.
         * @throws InterruptedException
         * 				When the IOIO thread has been interrupted.
         *
         * @see ioio.lib.util.IOIOLooper#loop()
         */
        @Override
        public void loop() throws ConnectionLostException, InterruptedException {
            int re = r.nextInt(100-0); //random number between 0 and 100 for rotation enable
            int ra = r.nextInt(3-0); //random number between 0 and 100 for rotation angle

            if(actionEnable){
                if(re <= 1) {  //% chance that the head will rotate
                    switch(ra){
                        case 0: newPos = 1000; break;    //limit 600
                        case 1: newPos = 1550; break;
                        case 2: newPos = 2000; break;   //limit 2450
                        default: break;
                    }

                    if(newPos != currentPos)
                    {
                        moveToNewPosition();
                        initIR();
                    }
                }
                else{
                    float measVal = iRSensors.input.read();
                    float measVolt = iRSensors.input.getVoltage();
                    if(iRSensors.motionDetect(measVal, measVolt)) {
                        led_.write(false);
                        Log.d("MOTION", "Detected motion!"
                                        + " BaseVal: " + iRSensors.baseValue + "/" + measVal +
                                        ", BaseVolt: " + iRSensors.baseVolt + "/" + measVolt
                        );

                        action.executeGreeting();
                        Intent pickActionIntent = new Intent(SecuroBotSlaveMain.this, ActivityChooser.class);
                        startActivityForResult(pickActionIntent, REQUEST_ACTION_PICK);
                        actionEnable = false;

                        Log.d("IR SENSORS", "reinitializing...");
                        initIR();
                    }
                    else led_.write(true);
                }
            } else initIR();
            Thread.sleep(100);
        }

        private void moveToNewPosition() throws ConnectionLostException, InterruptedException {
            int ad = 100;   //acceleration distance - distance at which the acceleration take place
            int peakDelay = 500;   //starting/ending delay (the longest delay)
            int p1; //bottom peak 1 - acceleration end peak position
            int p2; //bottom peak 2 - decelleration start peak position
            boolean direction;  //reverse = false, forward = true
            int pos = currentPos;
            double af = 1;  //acceleration factor (multiplied by the peakDelay to get the delay for the current position)
            int dArrSize = Math.abs(newPos-currentPos);
            double delayArr[] = new double[dArrSize];

            if(currentPos<newPos) { //fwd
                p1 = currentPos+ad;
                p2 = newPos-ad;
                direction = true;
            }
            else {  //rev
                p1 = newPos+ad;
                p2 = currentPos-ad;
                direction = false;
            }

            for(int i=0, p; i<dArrSize; i++) {
                p = (direction) ? pos+1 : pos-1;
                delayArr[i] = (p<=p1) ? -Math.exp(-Math.pow((p-p1),2)/6000) + 1.2 :
                        (p2<=p) ? -Math.exp(-Math.pow((p-p2),2)/6000) + 1.2 : af;
            }

            for(int i=0; pos!=newPos; i+=1) {
                pos = (direction) ? pos+1 : pos-1;  //if forward increase, reverse decrease
                pwm.setPulseWidth(pos);
                Thread.sleep(0, (int) Math.floor(delayArr[i] * peakDelay));
            }
            Log.d("Position", "Done");
            currentPos = newPos;
        }

        public void initIR() throws ConnectionLostException, InterruptedException {
            float baseVal=0f, baseVolt=0f;

            for(int i=0; i<iRSensors.iSamples; i++) {
                baseVal += iRSensors.input.read();
                baseVolt += iRSensors.input.getVoltage();
            }
            iRSensors.initialize(baseVal / iRSensors.iSamples, baseVolt / iRSensors.iSamples);
/*
            Log.d("INIT IR", "Base Val: " + baseVal/iRSensors.iSamples +
                    ", base Volt: " + baseVolt/iRSensors.iSamples);*/
        }

        /**
         * Called when the IOIO is disconnected.
         *
         * @see ioio.lib.util.IOIOLooper#disconnected()
         */
        @Override
        public void disconnected() {
            toast("IOIO disconnected");
        }

        /**
         * Called when the IOIO is connected, but has an incompatible firmware version.
         *
         * @see ioio.lib.util.IOIOLooper#incompatible(IOIO)
         */
        @Override
        public void incompatible() {
            showVersions(ioio_, "Incompatible firmware version!");
        }
    }

    /**
     * A method to create our IOIO thread.
     *
     * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
     */
    @Override
    protected IOIOLooper createIOIOLooper() {
        return new Looper();
    }

    private void showVersions(IOIO ioio, String title) {
        toast(String.format("%s\n" +
                        "IOIOLib: %s\n" +
                        "Application firmware: %s\n" +
                        "Bootloader firmware: %s\n" +
                        "Hardware: %s",
                title,
                ioio.getImplVersion(IOIO.VersionType.IOIOLIB_VER),
                ioio.getImplVersion(IOIO.VersionType.APP_FIRMWARE_VER),
                ioio.getImplVersion(IOIO.VersionType.BOOTLOADER_VER),
                ioio.getImplVersion(IOIO.VersionType.HARDWARE_VER)));
    }

    private void toast(final String message) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}

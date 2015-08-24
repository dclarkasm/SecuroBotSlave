package com.example.devon.securobotslave;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
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


public class SecuroBotSlaveMain extends IOIOActivity {
    private static final int REQUEST_ACTION_PICK = 1;
    private Handler mHandler;
    private Random r = new Random();
    WebView webPageView;
    FrameLayout chooser;
    RelativeLayout home;
    boolean actionEnable = true;
    Queue pageQueue = new LinkedList();
    TTSEngine t1;
    ActionEngine action;
    //**********************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_securo_bot_slave_main);

        t1 = new TTSEngine(this);

//**************************************************************************************************
                                //SecuroBot setup stuffs
//**************************************************************************************************
        chooser = (FrameLayout) findViewById(R.id.chooserLayout);
        home = (RelativeLayout) findViewById(R.id.homeLayout);
        chooser.setVisibility(View.INVISIBLE);
        webPageView = (WebView) findViewById(R.id.webview);
        webPageView.setWebViewClient(new WebViewClient());
        WebSettings webPageSettings = webPageView.getSettings();
        webPageSettings.setJavaScriptEnabled(true);
        webPageView.setVisibility(View.INVISIBLE);

        webPageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("Timer", "Webpage view touched");
                if(!actionEnable){    //reset the interaction timer if we are displaying stuff
                    mHandler.removeCallbacks(interactionTimer);
                    mHandler.removeCallbacks(timerInterrupt);
                    interactionTimer.run();
                    //sendMessage("RS");  //send the Reset message to the master to reset the
                    Log.d("Timer", "Touch sensed. Timer was reset.");
                }
                return false;
            }
        });

        action = new ActionEngine(t1);
        mHandler = new Handler();

        startRepeatingTask();

        Queue testQueue = new ArrayBlockingQueue(3);
        testQueue.add(1);
        testQueue.add(2);
        testQueue.add(3);
        Iterator iterator = testQueue.iterator();
        while(iterator.hasNext()) {
            Log.d("TestIterator", "found: " + iterator.next());
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ACTION_PICK:
                if (resultCode == RESULT_OK) {
                    int actionType = data.getIntExtra("action", -1);

                    switch(actionType){
                        case ActionEngine.ACTION_PAGE:
                            action.executePage();
                            pageQueue.add(action.getWebPage());
                            actionEnable = false;
                            interactionTimer.run();
                            break;
                        case ActionEngine.ACTION_QUIZ:
                            action.executeQuiz();
                            pageQueue.add(action.getQuiz());
                            actionEnable = false;
                            interactionTimer.run();
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
                        default:
                            Log.d("Action Chooser", "Uknown command: " + actionType);
                            actionEnable = true;
                            break;
                    }
                }
                else
                {
                    actionEnable = true;
                }
        }
    }

//**************************************************************************************************
                                        //Threads
//**************************************************************************************************
    void startRepeatingTask() {
        openWebPage.run();
        fetchContent.run();
        populateContent.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(openWebPage);
        mHandler.removeCallbacks(fetchContent);
        mHandler.removeCallbacks(populateContent);
    }

    String lastURL = "";
    Runnable openWebPage = new Runnable() {
        String blankPage = "about:blank";

        @Override
        public void run() {
            if(!pageQueue.isEmpty()){
                lastURL = (String)pageQueue.remove();
                webPageView.loadUrl(lastURL);
                webPageView.setVisibility(View.VISIBLE);
            }
            else if(actionEnable && lastURL != blankPage) {
                lastURL = blankPage;
                webPageView.loadUrl(lastURL);
                webPageView.setVisibility(View.INVISIBLE);
            }
            mHandler.postDelayed(openWebPage, 100);
        }
    };

    //A timer that expires if the user does not interact with screen after X time
    Runnable interactionTimer = new Runnable(){
        @Override
        public void run() {
            Log.d("Timer", "Called timer");
            actionEnable = false;
            Log.d("Timer", "Delay Started...");
            mHandler.postDelayed(timerInterrupt, 30000);
        }
    };

    //this is called by the interaction timer when time has expired, as long as it hasnt been pulled
    //from the handler
    Runnable timerInterrupt = new Runnable() {
        @Override
        public void run() {
            actionEnable = true;
            mHandler.removeCallbacks(interactionTimer);
            Log.d("Timer", "Delay Stopped.");
        }
    };

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
/*
    final Handler viewHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

        }
    };
*/
    Runnable choiceTimer = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(choiceTimerInterrupt, 30000);
        }
    };

    Runnable choiceTimerInterrupt = new Runnable() {
        @Override
        public void run() {
            setResult(RESULT_CANCELED);
            stopChoiceRunnable();
        }
    };

    private void stopChoiceRunnable() {
        mHandler.removeCallbacks(choiceTimer);
        mHandler.removeCallbacks(choiceTimerInterrupt);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chooser.setVisibility(View.INVISIBLE);
                home.setVisibility(View.VISIBLE);
            }
        });
    }

    public void articleAction(View v) {
        stopChoiceRunnable();
        action.executePage();
        pageQueue.add(action.getWebPage());
        actionEnable = false;
        interactionTimer.run();
    }

    public void quizAction(View v) {
        stopChoiceRunnable();
        action.executeQuiz();
        pageQueue.add(action.getQuiz());
        actionEnable = false;
        interactionTimer.run();
    }

    public void jokeAction(View v) {
        stopChoiceRunnable();
        action.executeActivity(ActionEngine.ACTION_JOKE);
        actionEnable = true;
    }

    public void pictureAction(View v) {
        /*
        stopChoiceRunnable();
        action.executeActivity(ActionEngine.ACTION_PICTURE);
        actionEnable = true;
        */
    }

    public void hackedAction(View v) {
        /*
        stopChoiceRunnable();
        action.executeActivity(ActionEngine.ACTION_HACKED);
        actionEnable = true;
        */
    }

    public void rssAction(View v) {
        stopChoiceRunnable();
        action.executeActivity(ActionEngine.ACTION_RSS);
        actionEnable = true;
    }

    public void tipAction(View v) {
        stopChoiceRunnable();
        action.executeActivity(ActionEngine.ACTION_TIP);
        actionEnable = true;
    }

    public void tweetAction(View v) {
        stopChoiceRunnable();
        action.executeActivity(ActionEngine.ACTION_TWEET);
        actionEnable = true;
    }

    public void randomAction(View v) {
        stopChoiceRunnable();
        action.executeRandActivity();
        actionEnable = true;
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
        int newPos, currentPos;

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
                        led_.write(true);
                        pwm.setPulseWidth(newPos);
                        currentPos = newPos;
                        Log.d("ROTATE", "Moving to position: " + newPos + "...");
                        Thread.sleep(1000);
                        Log.d("ROTATE", "At position: " + newPos);
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                home.setVisibility(View.INVISIBLE);
                                chooser.setVisibility(View.VISIBLE);
                            }
                        });
                        choiceTimer.run();
                        //Intent pickActionIntent = new Intent(SecuroBotSlaveMain.this, ActionChooserActivity.class);
                        //startActivityForResult(pickActionIntent,REQUEST_ACTION_PICK);
                        actionEnable = false;

                        Log.d("IR SENSORS", "reinitializing...");
                        initIR();
                    }
                    else led_.write(true);
                }
            } else initIR();
            Thread.sleep(100);
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

package com.example.devon.securobotslave;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import java.lang.Math.*;
import java.util.regex.Pattern;


public class SecuroBotSlaveMain extends IOIOActivity implements LocationListener{
    private static final int REQUEST_ACTION_PICK = 1;
    private static final int REQUEST_WEB_PAGE = 2;
    private Handler mHandler;
    private Random r = new Random();
    boolean actionEnable = true;
    TTSEngine t1;
    ActionEngine action;
    int currentPos = 1550;
    LocationManager locationManager;
    Location currentLoc;
    String northSouth = "";
    String eastWest = "";
    ImageButton spkButton;
    ImageView logo;
    RelativeLayout progress;
    //LocationListener locationListener;
    TwitterEngine te;
    Dialog alertDialog;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final static String baseAPIURL = "https://siris.p.mashape.com/api" +
            "?clientFeatures=all" +
            "&out=simple" +
            "&mashape-key=" +
            Constants.SIRI_API_KEY +
            "&accept=text/plain" +
            "&input=";
    //**********************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_securo_bot_slave_main);

        t1 = new TTSEngine(this);
        action = new ActionEngine(t1);
        te = new TwitterEngine();
        mHandler = new Handler();

        startRepeatingTask();
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, this);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
        currentLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(currentLoc!=null) {
            //updateLocation();
        }

        spkButton = (ImageButton) findViewById(R.id.btnSpeak);
        logo = (ImageView) findViewById(R.id.imageView);
        progress = (RelativeLayout) findViewById(R.id.progress);
        progress.setVisibility(View.INVISIBLE);

        spkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionEnable) {
                    actionEnable = false;
                    promptSpeechInput();
                }
                else {
                    t1.t1.stop();
                    spkButton.setImageResource(R.drawable.mic_icon);
                    actionEnable = true;
                }
            }
        });

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionEnable) {

                    actionEnable = false;   //make sure we do this before anything else so we dont double trigger (IR & touch)
                    action.executeGreeting();
                    Intent pickActionIntent = new Intent(SecuroBotSlaveMain.this, ActivityChooser.class);
                    startActivityForResult(pickActionIntent, REQUEST_ACTION_PICK);

                    Log.d("Main", "Touch detected!");
                }
            }
        });
    }

    @Override
    public void onProviderEnabled(String something) {

    }

    @Override
    public void onProviderDisabled(String something) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle b) {

    }

    @Override
    public void onLocationChanged(Location loc) {
        currentLoc = loc;
        //updateLocation();
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

    boolean wasSpeaking = false;
    Runnable manageMicBtn = new Runnable() {
        @Override
        public void run() {
            if(t1.isSpeaking && !wasSpeaking) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spkButton.setImageResource(R.drawable.stop_audio);
                        spkButton.setVisibility(View.VISIBLE);
                        actionEnable = false;
                        wasSpeaking = true;
                    }
                });
                mHandler.postDelayed(manageMicBtn, 100);
            }
            else if(!t1.isSpeaking && wasSpeaking){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spkButton.setImageResource(R.drawable.mic_icon);
                        actionEnable = true;
                        wasSpeaking = false;
                    }
                });
                mHandler.removeCallbacks(manageMicBtn);
            }
            else {
                mHandler.postDelayed(manageMicBtn, 100);
            }
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
        //Intent pickActionIntent = new Intent(SecuroBotSlaveMain.this, ActivityChooser.class);

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
                            //startActivityForResult(pickActionIntent, REQUEST_ACTION_PICK);
                            actionEnable = true;
                            break;
                        case ActionEngine.ACTION_TIP:
                            action.executeActivity(ActionEngine.ACTION_TIP);
                            //startActivityForResult(pickActionIntent, REQUEST_ACTION_PICK);
                            actionEnable = true;
                            break;
                        case ActionEngine.ACTION_RSS:
                            action.executeActivity(ActionEngine.ACTION_RSS);
                            //startActivityForResult(pickActionIntent, REQUEST_ACTION_PICK);
                            actionEnable = true;
                            break;
                        case ActionEngine.ACTION_TWEET:
                            action.executeActivity(ActionEngine.ACTION_TWEET);
                            //startActivityForResult(pickActionIntent, REQUEST_ACTION_PICK);
                            actionEnable = true;
                            break;
                        case ActionEngine.ACTION_HACKED:
                            //startActivityForResult(pickActionIntent, REQUEST_ACTION_PICK);
                            actionEnable = true;
                        case ActionEngine.ACTION_PICTURE:
                            //startActivityForResult(pickActionIntent, REQUEST_ACTION_PICK);
                            actionEnable = true;
                        default:
                            Log.d("Action Chooser", "Uknown command: " + actionType);
                            //startActivityForResult(pickActionIntent, REQUEST_ACTION_PICK);
                            actionEnable = true;
                            break;
                    }
                } else {    //chooser timed out or back button pressed from chooser activity
                    actionEnable = true;
                }
                break;
            case REQUEST_WEB_PAGE:
                //startActivityForResult(pickActionIntent, REQUEST_ACTION_PICK);
                actionEnable = true;
                break;
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String urlString = baseAPIURL + makeString4GET(result.get(0));
                    new HIBPAPICall().execute(urlString);
                }
                else {
                    actionEnable = true;
                }
                break;
            }
        }
    }

    public void updateLocation() {
        //approximate location of UNH Buckman Hall: 72.96014647W, 41.29088209N
        northSouth = (currentLoc.getLongitude()<0) ? "N" : "S";
        eastWest = (currentLoc.getLatitude()<0) ? "E" : "W";
        Log.d("GPS", "Current location: " + Math.abs(currentLoc.getLatitude()) + northSouth +
                ", " + Math.abs(currentLoc.getLongitude()) + eastWest);
        Toast.makeText(getApplicationContext(), "Current Location: " + Math.abs(currentLoc.getLatitude()) + northSouth +
                ", " + Math.abs(currentLoc.getLongitude()) + eastWest, Toast.LENGTH_LONG).show();

        te.updateStatus("Where's SecuroBot? Current location: " + Math.abs(currentLoc.getLatitude()) + northSouth +
                ", " + Math.abs(currentLoc.getLongitude()) + eastWest, null);
    }

    public String makeString4GET(String request) {
        Log.d("AI", "Original: " + request);
        request = request.toLowerCase();
        Log.d("AI", "to Lower: " + request);
        request = request.replaceAll(" ", "+");
        Log.d("AI", "replace white: " + request);

        return request;
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Say Something");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry, speech recognition is not supported",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class HIBPAPICall extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String urlString = urls[0];
            String resultToDisplay = "";
            InputStream in = null;

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream());

                resultToDisplay = getStringFromInputStream(in);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            return resultToDisplay;
        }

        @Override
        protected void onPreExecute() {
            spkButton.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(final String result) {
            Log.d("Siri", "Recieved Result: " + result);
            parseResult(result);

            progress.setVisibility(View.INVISIBLE);

            manageMicBtn.run();
        }
    }

    public String parseResult(String original) {
        String parsed;
        String spoken;
        int origLen = original.length();
        int max = 200;  //max length of the string
        int expandedMax = 3000;
        Log.d("AI", "Original: " + original);
        Handler handler = new Handler();
        final String finalOrig;

        //find and replace the word "Jeannie" with "Ada"
        original = original.replace("Jeannie", "Ada");

        if(origLen <= 0) {
            parsed = "Sorry, I didn't quite understand that.";
            spoken = parsed;
        }
        else if(origLen > 0 && origLen <= max) {
            parsed = original;
            spoken = parsed;
        }
        else {  //origLen > max
            spoken = "Here is what I found about that topic.";
            finalOrig = original;
            Log.d("AI", "Long String Length: " + origLen);
            parsed = original.substring(0, max);

            actionEnable = false;

            alertDialog = new Dialog(SecuroBotSlaveMain.this);
            LayoutInflater inflater = getLayoutInflater();
            alertDialog.setContentView(inflater.inflate(R.layout.siri_dialog_layout, null));

            Button backButton = (Button) alertDialog.findViewById(R.id.backButton);
            Button readMoreButton = (Button) alertDialog.findViewById(R.id.readMore);
            final TextView siriTextView = (TextView) alertDialog.findViewById(R.id.siriText);
            ScrollView scrollView = (ScrollView) alertDialog.findViewById(R.id.scrollView);

            siriTextView.setText(parsed + "...");   //first set the text to first max-0 characters

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AITimerInterrupt.run();
                }
            });

            readMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetAIInterractionTimer();

                    siriTextView.setText(finalOrig);
                }
            });

            siriTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetAIInterractionTimer();
                }
            });

            alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    AITimerInterrupt.run();
                }
            });

            scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

                @Override
                public void onScrollChanged() {
                    resetAIInterractionTimer();
                }
            });

            alertDialog.show();
            AIInteractionTimer.run();
        }

        final String finalParse;
        if(origLen<=max) {
            finalParse = parsed;
        }
        else {
            finalParse = spoken;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                t1.speak(finalParse, TextToSpeech.QUEUE_FLUSH, null);
            }
        }, 1000); //adding one sec delay before talking to make sure UI changes stick

        Log.d("AI", "Parsed: " + parsed);
        return parsed;
    }

    private void resetAIInterractionTimer() {
        mHandler.removeCallbacks(AIInteractionTimer);
        mHandler.removeCallbacks(AITimerInterrupt);
        AIInteractionTimer.run();
        Log.d("AI", "Touch detected!");
    }

    //A timer that expires if the user does not interact with screen after X time
    Runnable AIInteractionTimer = new Runnable(){
        @Override
        public void run() {
            Log.d("AI", "Delay Started...");
            mHandler.postDelayed(AITimerInterrupt, 30000);
        }
    };

    //this is called by the interaction timer when time has expired, as long as it hasnt been pulled
    //from the handler
    Runnable AITimerInterrupt = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(AIInteractionTimer);
            Log.d("AI", "Delay Stopped.");
            alertDialog.dismiss();
            actionEnable = true;
        }
    };

    // convert InputStream to String
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
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
        //private PwmOutput pwm;
        private Uart uart;
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
            //showVersions(ioio_, "IOIO connected!");
            log("IOIO connected!");
            led_ = ioio_.openDigitalOutput(0, true);
            iRSensors.input = ioio_.openAnalogInput(iRSensors.pin);
            initSerial();
            initIR();
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

            int re = r.nextInt(100 - 0); //random number between 0 and 100 for rotation enable
            int ra = r.nextInt(3-0); //random number between 0 and 100 for rotation angle

            if(actionEnable){
                if(re <= 1) {  //% chance that the head will rotate
                    newPos = ra + 1;

                    if(newPos != currentPos)
                    {
                        moveToNewPosition();
                        initIR();
                        Log.d("ServoMove", "Returned to Loop");
                    }
                }
                else{
                    float measVal = iRSensors.input.read();
                    float measVolt = iRSensors.input.getVoltage();
                    //Log.d("Sensors", "meas Val: " + measVal + ", measVol: " + measVolt);
                    if(iRSensors.motionDetect(measVal, measVolt)) {
                        actionEnable = false;   //make sure we do this before anything else so we dont double trigger (IR & touch)
                        //pwm.close();
                        led_.write(false);
                        Log.d("MOTION", "Detected motion!"
                                        + " BaseVal: " + iRSensors.baseValue + "/" + measVal +
                                        ", BaseVolt: " + iRSensors.baseVolt + "/" + measVolt
                        );

                        moveToHomePosition();
                        action.executeGreeting();
                        Intent pickActionIntent = new Intent(SecuroBotSlaveMain.this, ActivityChooser.class);
                        startActivityForResult(pickActionIntent, REQUEST_ACTION_PICK);

                        Log.d("IR SENSORS", "reinitializing...");
                        initIR();
                    }
                    else led_.write(true);
                }
            } else initIR();

            Thread.sleep(100);
        }

        private void moveToNewPosition() throws ConnectionLostException, InterruptedException {

            OutputStream out = uart.getOutputStream();
            try {
                out.write(newPos);
                Log.d("Serial", "Sent: " + newPos + " to Arduino");
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(2000);

            Log.d("Serial", "Done");
            currentPos = newPos;
        }

        private void moveToHomePosition() throws ConnectionLostException, InterruptedException {
            OutputStream out = uart.getOutputStream();
            newPos = 2;
            try {
                out.write(newPos);
                Log.d("Serial", "Move to Home: " + newPos);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(2000);

            Log.d("Serial", "Done");
            currentPos = newPos;
        }

        public boolean initSerial() throws InterruptedException, ConnectionLostException{

            uart = ioio_.openUart(36, 35, 9600, Uart.Parity.EVEN, Uart.StopBits.ONE);

            return false;
        }

        public void initIR() throws ConnectionLostException, InterruptedException {
            float baseVal=0, baseVolt=0;

            for(int i=0; i<iRSensors.iSamples; i++) {
                baseVal += iRSensors.input.read();
                baseVolt += iRSensors.input.getVoltage();
            }
            iRSensors.initialize(baseVal / iRSensors.iSamples, baseVolt / iRSensors.iSamples);
            Thread.sleep(500);
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
            //toast("IOIO disconnected");
            log("IOIO disconnected");

            uart.close();
        }

        /**
         * Called when the IOIO is connected, but has an incompatible firmware version.
         *
         * @see ioio.lib.util.IOIOLooper#incompatible(IOIO)
         */
        @Override
        public void incompatible() {
            showVersions(ioio_, "Incompatible firmware version!");
            log("Incompatible firmware version!");
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

    private void log(final String message) {
        Log.d("IOIO", message);
    }
}

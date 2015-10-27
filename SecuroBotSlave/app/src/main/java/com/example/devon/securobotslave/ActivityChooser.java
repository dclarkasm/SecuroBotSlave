package com.example.devon.securobotslave;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

public class ActivityChooser extends Activity {
    private static final int REQUEST_HACKED_EMAIL = 1;
    private static final int REQUEST_CAMERA = 2;
    private Intent data = new Intent();
    private Random r = new Random();
    Handler mHandler;
    private AsyncTask AITask;
    ImageButton spkButton;
    TTSEngine t1;
    Dialog alertDialog;
    RelativeLayout progress;
    boolean actionEnable = true;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final static String baseAPIURL = "https://siris.p.mashape.com/api" +
            "?clientFeatures=all" +
            "&out=simple" +
            "&mashape-key=" +
            Constants.SIRI_API_KEY +
            "&accept=text/plain" +
            "&input=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_chooser);
        mHandler = new Handler();
        t1 = new TTSEngine(this);
        choiceTimer.run();

        spkButton = (ImageButton) findViewById(R.id.btnSpeak);
        progress = (RelativeLayout) findViewById(R.id.progress);
        progress.setVisibility(View.INVISIBLE);

        spkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionEnable) {
                    actionEnable = false;
                    mHandler.removeCallbacks(choiceTimer);
                    mHandler.removeCallbacks(timerInterrupt);
                    choiceTimer.run();
                    promptSpeechInput();
                }
                else {
                    t1.t1.stop();
                    spkButton.setImageResource(R.drawable.mic_icon);
                    mHandler.removeCallbacks(choiceTimer);
                    mHandler.removeCallbacks(timerInterrupt);
                    choiceTimer.run();
                    actionEnable = true;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_activity_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    Runnable choiceTimer = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(timerInterrupt, 29000);
        }
    };

    Runnable timerInterrupt = new Runnable() {
        @Override
        public void run() {
            setResult(RESULT_CANCELED);
            stopRunnable();
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
            mHandler.removeCallbacks(choiceTimer);
            mHandler.removeCallbacks(timerInterrupt);
            choiceTimer.run();
        }
    };

    Runnable manageActivities = new Runnable() {
        @Override
        public void run() {
            if(t1.isSpeaking && !wasSpeaking) {
                actionEnable = false;
            }
            else if(!t1.isSpeaking && wasSpeaking) {
                stopRunnable();
            }
            else {
                mHandler.removeCallbacks(choiceTimer);
                mHandler.removeCallbacks(timerInterrupt);
                choiceTimer.run();
            }
        }
    };

    private void stopRunnable() {
        mHandler.removeCallbacks(choiceTimer);
        mHandler.removeCallbacks(timerInterrupt);
        mHandler.removeCallbacks(manageMicBtn);
        finish();
    }

    public void articleAction(View v) {
        if(actionEnable) {
            data.putExtra("action", ActionEngine.ACTION_PAGE);
            setResult(RESULT_OK, data);
            stopRunnable();
        }
    }

    public void quizAction(View v) {
        if(actionEnable) {
            data.putExtra("action", ActionEngine.ACTION_QUIZ);
            setResult(RESULT_OK, data);
            stopRunnable();
        }
    }

    public void jokeAction(View v) {
        if(actionEnable) {
            data.putExtra("action", ActionEngine.ACTION_JOKE);
            setResult(RESULT_OK, data);
            stopRunnable();
        }
    }

    public void tweetAction(View v) {
        if(actionEnable) {
            data.putExtra("action", ActionEngine.ACTION_TWEET);
            setResult(RESULT_OK, data);
            stopRunnable();
        }
    }

    public void pictureAction(View v) {
        if(actionEnable) {
            data.putExtra("action", ActionEngine.ACTION_PICTURE);
            Intent hackedIntent = new Intent(ActivityChooser.this, CameraActivity.class);
            startActivityForResult(hackedIntent, REQUEST_CAMERA);
        }
    }

    public void hackedAction(View v) {
        if (actionEnable) {
            data.putExtra("action", ActionEngine.ACTION_HACKED);
            Intent hackedIntent = new Intent(ActivityChooser.this, HackedEmailInputActivity.class);
            startActivityForResult(hackedIntent, REQUEST_HACKED_EMAIL);
        }
    }

    public void rssAction(View v) {
        if(actionEnable) {
            data.putExtra("action", ActionEngine.ACTION_RSS);
            setResult(RESULT_OK, data);
            stopRunnable();
        }
    }

    public void tipAction(View v) {
        if(actionEnable) {
            data.putExtra("action", ActionEngine.ACTION_TIP);
            setResult(RESULT_OK, data);
            stopRunnable();
        }
    }

    public void randomAction(View v) {
        if(actionEnable) {
            int randAction = r.nextInt(8-0);
            data.putExtra("action", randAction);
            setResult(RESULT_OK, data);
            stopRunnable();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_HACKED_EMAIL:
                stopRunnable();
                break;
            case REQUEST_CAMERA:
                stopRunnable();
                break;
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String urlString = baseAPIURL + makeString4GET(result.get(0));
                    AITask = new AIAPICall().execute(urlString);
                }
                else {
                    actionEnable = true;
                }
                break;
            }
        }
    }

    public String makeString4GET(String request) {
        Log.d("StringConv", "Original: " + request);
        request = request.toLowerCase();
        Log.d("StringConv", "to Lower: " + request);
        request = request.replaceAll(" ", "+");
        Log.d("StringConv", "replace white: " + request);

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


    private class AIAPICall extends AsyncTask<String, Void, String> {

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
            AILoadTimer.run();
        }

        @Override
        protected void onPostExecute(final String result) {
            Log.d("Siri", "Recieved Result: " + result);
            parseResult(result);

            progress.setVisibility(View.INVISIBLE);

            mHandler.removeCallbacks(AILoadInterrupt);
            mHandler.removeCallbacks(AILoadTimer);

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

            alertDialog = new Dialog(ActivityChooser.this);
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

    Runnable AILoadTimer = (new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(AILoadInterrupt, 10000);
        }
    });

    Runnable AILoadInterrupt = (new Runnable() {
        @Override
        public void run() {
            progress.setVisibility(View.INVISIBLE);

            mHandler.removeCallbacks(AILoadInterrupt);
            mHandler.removeCallbacks(AILoadTimer);

            manageMicBtn.run();
            //stop the HIBPAPICall task
            AITask.cancel(true);

            t1.speak("Sorry, it seems my network connection is a bit slow. Lets talk about this later.", TextToSpeech.QUEUE_FLUSH, null);
        }
    });

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
}

package com.example.devon.securobotslave;

import android.app.Activity;
import android.app.ListActivity;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class HackedEmailInputActivity extends AppCompatActivity {
    Handler mHandler = new Handler();
    public final static String apiURL = "https://haveibeenpwned.com/api/v2/breachedaccount/";
    private ArrayList<Breach> breaches;
    ListView breachListView;
    String[] breachNameArray;
    TextView breachHeader;
    TTSEngine t2;
    //private String standardHeader;
    //private final static String noBreachHeader = "Your email has not been breached!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hacked_email_input);

        t2 = new TTSEngine(this);

        Button okButton = (Button) findViewById(R.id.ok);
        Button cancelButton = (Button) findViewById(R.id.cancel);
        final EditText emailAddress = (EditText) findViewById(R.id.emailAddressInput);
        Button clearButton = (Button) findViewById(R.id.clear);
        Button backButton = (Button) findViewById(R.id.back);

        /*
        Testing purposes only
         *************************/
        //new HIBPAPICall().execute(apiURL + "foo@bar.com");  //test URL so I dont have to type every time ;)
        //************************

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText email = (EditText) findViewById(R.id.emailAddressInput);
                String address = email.getText().toString();

                if( address != null && !address.isEmpty()) {
                    String urlString = apiURL + address;
                    new HIBPAPICall().execute(urlString);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please enter a valid email address.", Toast.LENGTH_LONG).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailAddress.setText("");
                breachHeader.setText("");
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                        R.layout.hacked_list_view_layout, R.id.hackedListItem, new String[0]);
                breachListView.setAdapter(adapter);
            }
        });

        emailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mHandler.removeCallbacks(interactionTimer);
                mHandler.removeCallbacks(timerInterrupt);
                interactionTimer.run();
                Log.d("Hacked6", "key press detected!");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        emailAddress.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_UP) {
                    Log.d("KeyListener", "Enter key pressed: " + keyCode);
                    EditText email = (EditText) findViewById(R.id.emailAddressInput);
                    String address = email.getText().toString();

                    if( address != null && !address.isEmpty()) {
                        String urlString = apiURL + address;
                        new HIBPAPICall().execute(urlString);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Please enter a valid email address.", Toast.LENGTH_LONG).show();
                    }
                }
                return false;
            }
        });

        breachListView = (ListView) findViewById(R.id.hackedListView);

        breachHeader = (TextView) findViewById(R.id.breachHeader);

        interactionTimer.run();

        waitForTTS.run();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(t2 !=null){
            t2.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        t2.onResume(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mHandler.removeCallbacks(interactionTimer);
        mHandler.removeCallbacks(timerInterrupt);
        interactionTimer.run();
        Log.d("Hacked", "Touch detected!");
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hacked_email_input, menu);
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

    //A timer that expires if the user does not interact with screen after X time
    Runnable interactionTimer = new Runnable(){
        @Override
        public void run() {
            Log.d("Timer", "Delay Started...");
            mHandler.postDelayed(timerInterrupt, 30000);
        }
    };

    //this is called by the interaction timer when time has expired, as long as it hasnt been pulled
    //from the handler
    Runnable timerInterrupt = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(interactionTimer);
            Log.d("Timer", "Delay Stopped.");
            exit();
        }
    };

    private void exit(){
        mHandler.removeCallbacks(interactionTimer);
        mHandler.removeCallbacks(timerInterrupt);
        mHandler.removeCallbacks(waitForTTS);
        mHandler.removeCallbacks(sayGreeting);
        setResult(RESULT_OK);
        finish();
    }

    Runnable waitForTTS = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(sayGreeting, 1000);
        }
    };

    Runnable sayGreeting = new Runnable() {
        @Override
        public void run() {
            t2.speak(getString(R.string.hibp_instructions_header), TextToSpeech.QUEUE_FLUSH, null);
        }
    };

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
        protected void onPostExecute(String result) {
            Log.d("Hacked", "Recieved Result: " + result);
            if(result.length()>0) {
                try {
                    JSONArray array = new JSONArray(result);

                    breaches = new ArrayList<Breach>();
                    breachNameArray = new String[array.length()];
                    for (int i=0; i<array.length(); i++){
                        breaches.add(new Breach(array.getJSONObject(i)));
                        breachNameArray[i] = breaches.get(i).getName();
                    }

                    String speakBreaches = "";
                    for(int i=0; i<breaches.size(); i++) {
                        breaches.get(i).printBreach();

                        if(i==(breaches.size()-2)) speakBreaches += breaches.get(i).getName() + ", and ";
                        else if(i==(breaches.size()-1)) speakBreaches += breaches.get(i).getName();
                        else speakBreaches += breaches.get(i).getName() + ", ";
                    }

                    breachHeader.setText(R.string.standard_breach_header);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                            R.layout.hacked_list_view_layout, R.id.hackedListItem, breachNameArray);
                    breachListView.setAdapter(adapter);
                    t2.speak("Your email account has been breached through association with " +
                            speakBreaches, TextToSpeech.QUEUE_FLUSH, null);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                Log.d("Breach", "No Breaches Detected");
                breachHeader.setText(R.string.no_breach_header);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                        R.layout.hacked_list_view_layout, R.id.hackedListItem, new String[0]);
                breachListView.setAdapter(adapter);
                t2.speak(getString(R.string.no_breach_header), TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

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

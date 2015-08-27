package com.example.devon.securobotslave;

import android.app.Activity;
import android.app.ListActivity;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.ContactsContract;
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
    private String standardHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hacked_email_input);

        Button okButton = (Button) findViewById(R.id.ok);
        Button cancelButton = (Button) findViewById(R.id.cancel);
        EditText emailAddress = (EditText) findViewById(R.id.emailAddressInput);

        /*
        Testing purposes only
         *************************/
        new HIBPAPICall().execute(apiURL + "foo@bar.com");  //test URL so I dont have to type every time ;)
        //************************

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText email = (EditText) findViewById(R.id.emailAddressInput);
                String address = email.getText().toString();

                Toast.makeText(getApplicationContext(), "Email address: " + address, Toast.LENGTH_SHORT).show();

                if( address != null && !address.isEmpty()) {
                    String urlString = apiURL + address;
                    new HIBPAPICall().execute(urlString);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please enter a valid email address.", Toast.LENGTH_LONG).show();
                }
                //exit();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
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

        breachListView = (ListView) findViewById(R.id.hackedListView);

        breachHeader = (TextView) findViewById(R.id.breachHeader);
        standardHeader = breachHeader.getText().toString();

        interactionTimer.run();
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
        setResult(RESULT_OK);
        finish();
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

                    for(Breach b : breaches) {
                        b.printBreach();
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                            R.layout.hacked_list_view_layout, R.id.hackedListItem, breachNameArray);
                    breachListView.setAdapter(adapter);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                Log.d("Breach", "No Breaches Detected");
                breachHeader.setText(standardHeader + "No Breaches Detected!");
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

package com.example.devon.securobotslave;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Random;

public class ActionChooserActivity extends AppCompatActivity {

    private Intent data = new Intent();
    private Random r = new Random();
    Handler mHandler;
    boolean wait = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_chooser);
        mHandler = new Handler();
        choiceTimer.run();
        //looper();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_action_chooser, menu);
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
            //mHandler.removeCallbacks(choiceTimer);
            //finish();
            //mHandler.removeCallbacks(timerInterrupt);
        }
    };

    private void stopRunnable() {
        mHandler.removeCallbacks(choiceTimer);
        mHandler.removeCallbacks(timerInterrupt);
        finish();
    }

    public void articleAction(View v) {
        data.putExtra("action", ActionEngine.ACTION_PAGE);
        setResult(RESULT_OK, data);
        stopRunnable();
    }

    public void quizAction(View v) {
        data.putExtra("action", ActionEngine.ACTION_QUIZ);
        setResult(RESULT_OK, data);
        stopRunnable();
    }

    public void jokeAction(View v) {
        data.putExtra("action", ActionEngine.ACTION_JOKE);
        setResult(RESULT_OK, data);
        stopRunnable();
    }

    public void pictureAction(View v) {
        /*
        data.putExtra("action", ActionEngine.ACTION_PICTURE);
        setResult(RESULT_OK, data);
        stopRunnable();
        finish();
        */
    }

    public void hackedAction(View v) {
        /*
        data.putExtra("action", ActionEngine.ACTION_HACKED);
        setResult(RESULT_OK, data);
        stopRunnable();
        finish();
        */
    }

    public void rssAction(View v) {
        data.putExtra("action", ActionEngine.ACTION_RSS);
        setResult(RESULT_OK, data);
        stopRunnable();
    }

    public void tipAction(View v) {
        data.putExtra("action", ActionEngine.ACTION_TIP);
        setResult(RESULT_OK, data);
        stopRunnable();
    }

    public void randomAction(View v) {
        int randAction = r.nextInt(8-0);
        data.putExtra("action", randAction);
        setResult(RESULT_OK, data);
        stopRunnable();
    }
}

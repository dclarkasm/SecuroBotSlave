package com.example.devon.securobotslave;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebPageActivity extends AppCompatActivity {
    WebView webPageView;
    Handler mHandler;
    String URL = "";
    Intent data = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_page);

        webPageView = (WebView) findViewById(R.id.webview);
        webPageView.setWebViewClient(new WebViewClient());
        WebSettings webPageSettings = webPageView.getSettings();
        webPageSettings.setJavaScriptEnabled(true);

        webPageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("Timer", "Webpage view touched");
                mHandler.removeCallbacks(interactionTimer);
                mHandler.removeCallbacks(timerInterrupt);
                interactionTimer.run();
                Log.d("Timer", "Touch sensed. Timer was reset.");
                return false;
            }
        });

        mHandler = new Handler();
        URL = getIntent().getStringExtra("URL");
        Log.d("WebPage", "URL: " + URL);
        openWebPage.run();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_web_page, menu);
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

    public void backButtonClick(View v) {
        exit();
    }

    Runnable openWebPage = new Runnable() {
        @Override
        public void run() {
            Log.d("WebPage", "About to load URL...");
            webPageView.loadUrl(URL);
            interactionTimer.run();
            Log.d("WebPage", "URL should have loaded...");
        }
    };

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
}

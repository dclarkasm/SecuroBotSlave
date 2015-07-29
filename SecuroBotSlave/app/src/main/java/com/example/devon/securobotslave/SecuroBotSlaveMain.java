package com.example.devon.securobotslave;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;


public class SecuroBotSlaveMain extends Activity {
    private static final String TAG = "Bluetooth";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;

    //**********************************************
    private Handler mHandler;
    Random r = new Random();
    WebView webPageView;
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
                                //Set up Bluetooth stuff
//**************************************************************************************************
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            // Initialize the BluetoothChatService to perform bluetooth connections
            mChatService = new BluetoothChatService(this, BTHandler);

            // Initialize the buffer for outgoing messages
            mOutStringBuffer = new StringBuffer("");
        }

        //at this point you could make the device discoverable, but lets just assume
        //we are using already paired devices for security reasons.
        //this is a cyber security robot anyway ;)
        //ensureDiscoverable();

        //then prompt the user to select a device tot connect to (secure method)
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);

        /* insecure method
        Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
         */

//**************************************************************************************************
                                //SecuroBot setup stuff
//**************************************************************************************************
        webPageView = (WebView) findViewById(R.id.webview);
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
                    sendMessage("RS");  //send the Reset message to the master to reset the
                    Log.d("Timer", "Touch sensed. Timer was reset.");
                }
                return false;
            }
        });

        action = new ActionEngine(t1);
        mHandler = new Handler();

        startRepeatingTask();
    }

//**************************************************************************************************
                                    //Bluetooth stuff
//**************************************************************************************************
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, "No devices connected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler BTHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.d("Bluetooth", "Wrote message: " + writeMessage);
                    //Toast.makeText(SecuroBotSlaveMain.this, "Wrote message: " + writeMessage, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d("Bluetooth", "Read message: " + readMessage);
                    Toast.makeText(SecuroBotSlaveMain.this, "Read message: " + readMessage, Toast.LENGTH_SHORT).show();
                    processMessage(readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(SecuroBotSlaveMain.this, "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(SecuroBotSlaveMain.this, msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    // Initialize the BluetoothChatService to perform bluetooth connections
                    mChatService = new BluetoothChatService(this, BTHandler);

                    // Initialize the buffer for outgoing messages
                    mOutStringBuffer = new StringBuffer("");
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Bluetooth was not enebled. Leaving chat",
                            Toast.LENGTH_SHORT).show();
                    this.finish();
                }
        }
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    public void processMessage(String message){
        if(actionEnable){
            switch(message){
                case "Webpage":
                    action.executeGreeting();
                    action.executePage();
                    pageQueue.add(action.getWebPage());
                    interactionTimer.run();
                    actionEnable = false;
                    break;
                case "Quiz":
                    action.executeGreeting();
                    action.executeQuiz();
                    pageQueue.add(action.getQuiz());
                    interactionTimer.run();
                    actionEnable = false;
                    break;
                case "Joke":
                    action.executeGreeting();
                    action.executeActivity(ActionEngine.ACTION_JOKE);
                    sendMessage("CC");
                    break;
                case "Tip":
                    action.executeGreeting();
                    action.executeActivity(ActionEngine.ACTION_TIP);
                    sendMessage("CC");
                    break;
                case "RSS":
                    action.executeGreeting();
                    action.executeActivity(ActionEngine.ACTION_RSS);
                    sendMessage("CC");
                    break;
                case "Tweet":
                    action.executeGreeting();
                    action.executeActivity(ActionEngine.ACTION_TWEET);
                    sendMessage("CC");
                    break;
                default: break;
            }
        }
        else sendMessage("CC");
    }

//**************************************************************************************************
                                        //Threads
//**************************************************************************************************
    void startRepeatingTask() {
        openWebPage.run();
        fetchContent.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(openWebPage);
        mHandler.removeCallbacks(fetchContent);
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
            sendMessage("CC");  //send the Command Complete message to SecuroBotMaster via BT
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
}

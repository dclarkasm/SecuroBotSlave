package com.example.devon.securobotslave;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class CameraActivity extends AppCompatActivity {
    TTSEngine t2;
    Handler mHandler = new Handler();
    private Camera mCamera;
    private CameraPreview mPreview;
    File pictureFile = null;
    byte[] pictureData = null;
    TwitterEngine te;
    FrameLayout preview;
    Dialog alertDialog;
    Bitmap overlay;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        // Create an instance of Camera
        mCamera = getCameraInstance();


        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        mPreview = new CameraPreview(this, mCamera);
        DrawOnTop mDraw = new DrawOnTop(this);
        overlay = BitmapFactory.decodeResource(getResources(), R.drawable.securobot_selfie_379x344);
        //setContentView(mPreview);
        addContentView(mDraw, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        //setContentView(R.layout.activity_camera);

        Log.d("OnCreate", "Camera activity on create called");
        t2 = new TTSEngine(this);
        te = new TwitterEngine();

        // Create our Preview view and set it as the content of our activity.
        //mPreview = new CameraPreview(this, mCamera);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        // Add a listener to the Capture button
        Button captureButton = (Button) findViewById(R.id.button_capture);
        Button cancelButton = (Button) findViewById(R.id.cancel);

        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        resetInterractionTimer();
                        // get an image from the camera
                        mCamera.takePicture(null, null, null, mPicture);
                    }
                }
        );

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, null, mPicture);
            }
        });

        interactionTimer.run();
        waitForTTS.run();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_camera, menu);
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

    class DrawOnTop extends View {
        public DrawOnTop(Context context) {
            super(context);

        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(overlay, 600, 900, null);
            super.onDraw(canvas);
        }
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            //c = Camera.open(); // attempt to get a Camera instance
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int cameraCount = Camera.getNumberOfCameras();
            for (int camIdx = 0; camIdx<cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        c = Camera.open(camIdx);
                    } catch (RuntimeException e) {
                        Log.e("Your_TAG", "Camera failed to open: " + e.getLocalizedMessage());
                    }
                }
            }
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            mCamera = camera;
            pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            pictureData = data;

            if(pictureData!=null && pictureFile!=null) {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    Bitmap finalBitmap;

                    try {
                        finalBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options).copy(Bitmap.Config.RGB_565, true);

                        int width = finalBitmap.getWidth();
                        int height = finalBitmap.getHeight();

                        // Perform matrix rotations/mirrors
                        float[] mirrorY = { -1, 0, 0, 0, 1, 0, 0, 0, 1};
                        Matrix matrixMirrorY = new Matrix();
                        matrixMirrorY.setValues(mirrorY);

                        Matrix matrix = new Matrix();
                        matrix.postConcat(matrixMirrorY);

                        // Create new Bitmap out of the old one
                        Bitmap bitPicFinal = Bitmap.createBitmap(finalBitmap, 0, 0, width, height,matrix, true);

                        Canvas canvas = new Canvas(bitPicFinal);

                        Bitmap overlayBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.securobot_selfie_379x344);
                        canvas.drawBitmap(overlayBitmap, 300, 600, new Paint());
                        canvas.scale(50, 0);
                        canvas.save();
                        //finalBitmap is the image with the overlay on it
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitPicFinal.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        fos.write(stream.toByteArray());
                        fos.close();
                    }
                    catch(OutOfMemoryError e) {
                        e.printStackTrace();
                    }

                } catch (FileNotFoundException e) {
                    Log.d("Picture", "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d("Picture", "Error accessing file: " + e.getMessage());
                }

            }

            alertDialog = new Dialog(CameraActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            alertDialog.setContentView(inflater.inflate(R.layout.tweet_picture_dialog_layout, null));

            Button cancelButton = (Button) alertDialog.findViewById(R.id.dialogCancel);
            Button tweetButton = (Button) alertDialog.findViewById(R.id.dialogTweet);
            Button clearButton = (Button) alertDialog.findViewById(R.id.dialogClear);
            ImageView pictureView = (ImageView) alertDialog.findViewById(R.id.dialogPictureView);
            final EditText usernameText = (EditText) alertDialog.findViewById(R.id.dialogUsername);

            Uri uri = Uri.fromFile(pictureFile);
            if(uri!=null) {
                Log.d("Camera", "Set imageview to picture");
                pictureView.setImageURI(uri);
            }

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetInterractionTimer();

                    resetPreview();
                    alertDialog.dismiss();
                }
            });

            tweetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetInterractionTimer();
                    new twitterSearchUIDTask().execute(usernameText.getText().toString());
                }
            });

            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetInterractionTimer();
                    usernameText.setText("");
                }
            });

            alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    resetInterractionTimer();
                    resetPreview();
                    alertDialog.dismiss();
                }
            });

            usernameText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    resetInterractionTimer();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            alertDialog.show();

            if (pictureFile == null){
                Log.d("Picture", "Error creating media file, check storage permissions: ");
                return;
            }

        }
    };


    private class twitterSearchUIDTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... UID) {
            try {
                User user = null;
                Log.d("Twitter", "UID length: " + UID[0].length() + ", UID: " + UID[0]);
                if(UID[0].length()>0) {
                    Log.d("Twitter", "Checking User ID...");
                    try{
                        user = te.twitter.showUser(UID[0]);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if(user!=null || UID[0].length()==0) {
                    try{
                        Log.d("Twitter", "Trying to update status...");

                        if(user!=null) {
                            uploadPic(pictureFile, "Hanging out and learning about #cybersecurity with @" +
                                    UID[0], te.twitter);
                        }
                        else {
                            uploadPic(pictureFile, "Hanging out and learning about #cybersecurity", te.twitter);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Uploading picture to twitter.",
                                        Toast.LENGTH_LONG).show();
                                resetPreview();
                                alertDialog.dismiss();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if(user==null && UID[0].length()>0) {
                    Log.d("Twitter", "Invalid User ID");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Please enter a valid twitter ID",
                            Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            /*
            Toast.makeText(getApplicationContext(),
                    "Finished uploading image to twitter.", Toast.LENGTH_LONG).show();
                    */
            exit();
            Log.d("Twitter", "Successfully updated the status.");
            super.onPostExecute(s);
        }
    }

    private void resetPreview() {
        preview.removeAllViews();
        mPreview = new CameraPreview(getApplicationContext(), mCamera);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("Camera", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            Log.d("File Path", mediaStorageDir.getPath());
            ///storage/emulated/0/Pictures/MyCameraApp
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "UNHCFREG_Tweet_Image.jpg");    //we want to overwrite the old files as to not take up space
            Log.d("File Path", "Saved File");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "UNHCFREG_Tweet_Video.mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (t2 !=null){
            t2.onPause();
            //mCamera.release();
        }
        mCamera.stopPreview();
    }

    @Override
    public void onResume() {
        super.onResume();
        t2.onResume(this);
        mPreview.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(t2 != null) {
            Log.d("TTS", "About to shutdown TTS from camera activity...");
            t2.t1.shutdown();
        }
        if(mCamera!=null) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        resetInterractionTimer();
        return false;
    }

    private void resetInterractionTimer() {
        mHandler.removeCallbacks(interactionTimer);
        mHandler.removeCallbacks(timerInterrupt);
        interactionTimer.run();
        Log.d("Camera", "Touch detected!");
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
        t2.onPause();
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
            t2.speak(getString(R.string.camera_greeting), TextToSpeech.QUEUE_FLUSH, null);
        }
    };

    public void uploadPic(File file, String message, Twitter twitter) throws Exception  {
        try{
            te.updateStatus(message, file);
            }
        catch(Exception e){
            Log.d("TAG", "Pic Upload error");
            throw e;
        }
    }
}

package com.example.stephenserene.shakewait;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.graphics.Bitmap;

import java.util.EventListener;

//
import android.graphics.PorterDuff.Mode;

public class taskLoop extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccel;
    // task progress variables
    private float stillFor;
    private long stillSince;
    private int waitFor;
    private long lastPoll = 0;
    private double totalAccel = 0;
    private int accelReadings = 0;
    private int pollEvery = 1000; // in ms
    private boolean giveReward = true;
    // task configuration variables
    private float threshold;
    private int headstart;
    private int shakeDown;
    // layout variables
    private RelativeLayout layout;
    private ImageView progressImage;
    private ImageView goalImage;
    // BLACK RECTANGLE
    private ImageView blackRectView;
    private ImageView whiteSquareView;
    private LinearLayout.LayoutParams blackRectSize;
    private LinearLayout.LayoutParams whiteSquareSize;

    private RelativeLayout.LayoutParams progressSize;
    private int progressSquare = R.drawable.progress1;
    private int goalSquare = R.drawable.goal1;
    private int blackRect = R.drawable.blackrect;
    private int whiteSquare = R.drawable.whitesquare;

    // audio port analog output variables
    private final int sampleRate = 20000;
    private final int numSamples = 2100;
    private final double sample[] = new double[numSamples];
    private final int lowFor = 50;
    private final int highFor = 2000;
    private final byte generatedSnd[] = new byte[2 * numSamples];
    private AudioTrack audioPulse;
    // debugging variables
    private TextView debugText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_loop);
        Intent startedBy = getIntent();

        //////////
        debugText = (TextView) findViewById(R.id.debugText);
        debugText.setText("waiting for accel");
        /////////////

        String waitTime = startedBy.getStringExtra(home.WAIT_TIME);
        waitFor = Math.round(1000*Float.valueOf(waitTime));

        String thresh = startedBy.getStringExtra(home.THRESHOLD);
        threshold = Float.valueOf(thresh);

        String shakeDownTmp = startedBy.getStringExtra(home.SHAKEDOWN);
        shakeDown = Math.round(1000*Float.valueOf(shakeDownTmp));

        String headstartTmp = startedBy.getStringExtra(home.HEADSTART);
        headstart = Math.round(1000*Float.valueOf(headstartTmp));

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.taskLoopContent);
        progressImage = (ImageView) findViewById(R.id.progressImage);
        progressImage.setImageResource(progressSquare);
        goalImage = (ImageView) findViewById(R.id.goalImage);
        goalImage.setImageResource(goalSquare);

        //BLACK RECTANGLE
        blackRectView = (ImageView) findViewById(R.id.blackRectView);
        blackRectView.setImageResource(blackRect);
        blackRectSize = new LinearLayout.LayoutParams(R.dimen.activity_horizontal_margin, 40);
        blackRectView.setLayoutParams(blackRectSize);

        //WHITE SQUARE
        whiteSquareView = (ImageView) findViewById(R.id.whiteSquareView);
        whiteSquareView.setImageResource(whiteSquare);
        whiteSquareSize = new LinearLayout.LayoutParams(40, 40);
        whiteSquareView.setLayoutParams(whiteSquareSize);

        progressSize = new RelativeLayout.LayoutParams(30, 30);
        progressImage.setLayoutParams(progressSize);

        stillFor = 0;
        stillSince = SystemClock.elapsedRealtime();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // see http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android

        int freqOfTone = 2000;
        for (int i = lowFor; i < (lowFor + highFor); ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        audioPulse = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STREAM);
        //audioPulse.write(generatedSnd, 0, generatedSnd.length);
        // DEBUG BEEP
        //audioPulse.play();

        //audioPulse.setVolume(audioPulse.getMaxVolume());
    }


    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    public final void onSensorChanged(SensorEvent event) {
        double accelX = event.values[0];
        double accelY = event.values[1];
        double accelZ = event.values[2];
        double accel = Math.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
        synchronized (this) {
            totalAccel += accel;
            accelReadings++;
        }
        if ((SystemClock.elapsedRealtime() - lastPoll) > pollEvery) {
            synchronized (this) {
                if (giveReward) {
                    whiteSquareView = (ImageView) findViewById(R.id.whiteSquareView);
                    whiteSquareView.setImageResource(whiteSquare);
                    whiteSquareView.setColorFilter(Color.BLACK);
                    giveReward = false;
                } else {
                    return;
                }
            }
            int goalWidth = goalImage.getWidth() - 20;
            int goalHeight = goalImage.getHeight() - 20;
            lastPoll = SystemClock.elapsedRealtime();

            //debugText.setText(Double.toString(totalAccel / accelReadings));
            if (totalAccel / accelReadings > threshold) {
                stillSince = Math.min(stillSince + shakeDown, SystemClock.elapsedRealtime());
            }
            debugText.setText(Long.toString(stillSince));
            synchronized (this) {
                totalAccel = 0;
                accelReadings = 0;
            }

            stillFor = SystemClock.elapsedRealtime() - stillSince;

            float waitFraction = stillFor / waitFor;
            int newWidth = Math.round(goalWidth * waitFraction);
            int newHeight = Math.round(goalHeight * waitFraction);
            progressSize = new RelativeLayout.LayoutParams(newWidth, newHeight);
            progressSize.addRule(RelativeLayout.CENTER_IN_PARENT);

            //set margins
            progressSize.setMargins(20, 0, 20, 0);

            progressImage.setLayoutParams(progressSize);
            if (stillFor > waitFor) {
                // give reward,
                // reset timer, with headstart (so he has a reason not to shake)
                stillSince = SystemClock.elapsedRealtime() - headstart;
//                audioPulse = new AudioTrack(AudioManager.STREAM_MUSIC,
//                        sampleRate, AudioFormat.CHANNEL_OUT_MONO,
//                        AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
//                        AudioTrack.MODE_STATIC);
                //audioPulse.write(generatedSnd, 0, generatedSnd.length);
                //audioPulse.play();
//                boolean done = false;
//                while (!done) {
//                    try {
//                        audioPulse.play();
//                        audioPulse.release();
//                        done = true;
//                    } catch (IllegalStateException e) {
//                    }
//                }
                whiteSquareView = (ImageView) findViewById(R.id.whiteSquareView);
                whiteSquareView.setImageResource(whiteSquare);
                whiteSquareView.setColorFilter(Color.WHITE);

            }
            synchronized (this) {
                giveReward = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

}

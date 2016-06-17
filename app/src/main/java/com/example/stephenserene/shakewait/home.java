package com.example.stephenserene.shakewait;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.EditText;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;



public class home extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public final static String WAIT_TIME = "shakeWait.WAIT_TIME";
    public final static String THRESHOLD = "shakeWait.THRESHOLD";
    public final static String SHAKEDOWN = "shakeWait.SHAKEDOWN";
    public final static String HEADSTART = "shakeWait.HEADSTART";
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
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

    /**
     * Called when the user clicks the play button
     */
    public void startTask(View view) {


        final int sampleRate = 8000;
        final int numSamples = 480;
        final double sample[] = new double[numSamples];
        final int lowFor = 80;
        final int highFor = 320;
        final byte generatedSnd[] = new byte[2 * numSamples];
        AudioTrack audioPulse;
        // see http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android

//        for (int i = 0; i < lowFor; ++i) {
//            sample[i] = 0;
//        }
//        for (int i = lowFor; i < numSamples; ++i) {
//            sample[i] = 32767; //this is the highest value allowed
//        }
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/1000.0));
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
                AudioTrack.MODE_STATIC);
        audioPulse.write(generatedSnd, 0, generatedSnd.length);
        audioPulse.play();




        Intent taskStarter = new Intent(this, taskLoop.class);

        EditText waitTimeText = (EditText) findViewById(R.id.wait_time_text);
        String waitTime = waitTimeText.getText().toString();
        taskStarter.putExtra(WAIT_TIME, waitTime);

        EditText thresholdText = (EditText) findViewById(R.id.threshold_text);
        String threshold = thresholdText.getText().toString();
        taskStarter.putExtra(THRESHOLD, threshold);

        EditText shakeDownText = (EditText) findViewById(R.id.shakeDown_text);
        String shakeDown = thresholdText.getText().toString();
        taskStarter.putExtra(SHAKEDOWN, shakeDown);

        EditText headstartText = (EditText) findViewById(R.id.headstart_text);
        String headstart = headstartText.getText().toString();
        taskStarter.putExtra(HEADSTART, headstart);
        startActivity(taskStarter);

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "home Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.stephenserene.shakewait/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "home Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.stephenserene.shakewait/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}

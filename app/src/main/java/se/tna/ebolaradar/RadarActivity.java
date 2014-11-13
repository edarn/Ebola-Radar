package se.tna.ebolaradar;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.AppEventsLogger;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class RadarActivity extends Activity {
    int i = 0;
    ImageView radarView;
    View button;
    TextView headline, text;

    Timer te;
    TimerTask tu;
    ProgressBar progress;
    private UiLifecycleHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar);

        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
        progress = (ProgressBar) findViewById(R.id.progress);
        radarView = (ImageView) findViewById(R.id.radar);
        headline = (TextView) findViewById(R.id.headline);
        text = (TextView) findViewById(R.id.text);
        button = findViewById(R.id.button);
        button.setClickable(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(RadarActivity.this)
                        .setLink("https://play.google.com/store/apps/details?id=se.tna.ebolaradar")
                        .build();
                uiHelper.trackPendingDialogCall(shareDialog.present());
            }
        });

        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        progress.setMax(720);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Dick Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Dick Success!");

/*
                FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(RadarActivity.this)
                        .setLink("https://developers.facebook.com/android")
                        .build();
                uiHelper.trackPendingDialogCall(shareDialog.present());
*/

            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
        i = 0;
        text.setVisibility(View.INVISIBLE);
        headline.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.VISIBLE);
        AppEventsLogger.activateApp(this);

        te = new Timer();
        tu = new TimerTask() {

            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        radarView.setRotation(i);
                        i += 1;
                        progress.setProgress(i);
                        if (i == 720) {
                            text.setVisibility(View.VISIBLE);
                            headline.setVisibility(View.VISIBLE);
                            progress.setVisibility(View.GONE);

                        }
                    }
                });
            }
        };


        te.scheduleAtFixedRate(tu, 1, 10);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
        AppEventsLogger.deactivateApp(this);
        te.cancel();
        tu.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
}

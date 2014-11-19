package se.tna.getluckyradar;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;


public class RadarActivity extends Activity {
    private static final int MAIN_TRACKER_ID = 1;
    int i = 0;
    ImageView radarView;
    View button;
    TextView headline, text;
    Tracker mainTracker;

    Timer te;
    TimerTask tu;
    ProgressBar progress;
    private UiLifecycleHelper uiHelper;
    private GoogleMap mMap;

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
                        .setLink("https://play.google.com/store/apps/details?id=se.tna.getluckyradar")
                        .setDescription("I have a 85% chance to Get Lucky tonight, what´s your chance?")
                        .build();
                uiHelper.trackPendingDialogCall(shareDialog.present());
                if (mainTracker != null) {
                    mainTracker.setScreenName("Share on Facebook");
                    mainTracker.send(new HitBuilders.AppViewBuilder().build());
                }
            }
        });

        AdView adView = (AdView) this.findViewById(R.id.adView);
        adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        mainTracker = analytics.newTracker(R.xml.global_tracker);
        mainTracker.setAppId("UA-56762502-3");
        mainTracker.enableAutoActivityTracking(true);
        mainTracker.enableAdvertisingIdCollection(true);
        mainTracker.enableExceptionReporting(true);

        progress.setMax(720);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {

            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {

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


        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mMap != null) {
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.getUiSettings().setZoomGesturesEnabled(false);
            mMap.getUiSettings().setAllGesturesEnabled(false);
        }


        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();


        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null && mMap != null)
        {

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to east
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }

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

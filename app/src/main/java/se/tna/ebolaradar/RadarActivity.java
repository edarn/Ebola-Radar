package se.tna.ebolaradar;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amazon.device.associates.AssociatesAPI;
import com.amazon.device.associates.LinkService;
import com.amazon.device.associates.NotInitializedException;
import com.amazon.device.associates.OpenSearchPageRequest;
import com.facebook.AppEventsLogger;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class RadarActivity extends Activity implements
                                            LocationListener,
                                            GoogleApiClient.ConnectionCallbacks,
                                            GoogleApiClient.OnConnectionFailedListener {
    private static final int MAIN_TRACKER_ID = 1;
    ImageView radarView;
    View button;
    TextView headline, text;

    Timer te;
    TimerTask tu;
    ProgressBar progress;
    private UiLifecycleHelper uiHelper;
    private GoogleMap mMap;

    private static final String AMAZON_APP_KEY = "6545a6092733453b9c8a9f7efea6a3ba";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final long POLLING_FREQ = 1000 * 30;
    private static final long FASTEST_UPDATE_FREQ = 1000 * 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar);

        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
        progress = (ProgressBar) findViewById(R.id.progress);
        radarView = (ImageView) findViewById(R.id.radar);
        progress.setMax(720);

        headline = (TextView) findViewById(R.id.headline);
        text = (TextView) findViewById(R.id.text);
        button = findViewById(R.id.button);
        button.setClickable(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(RadarActivity.this)
                        .setLink("https://play.google.com/store/apps/details?id=se.tna.ebolaradar")
                        .setDescription(getString(R.string.stay_safe))
                        .setCaption(getString(R.string.try_now))
                        .build();
                uiHelper.trackPendingDialogCall(shareDialog.present());
                Ads.trackScreenName("Share on Facebook");

            }
        });
        TextView amazonButton = (TextView) findViewById(R.id.amazonButton);
        amazonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenSearchPageRequest request = new OpenSearchPageRequest(getString(R.string.ebola));
                try {
                    LinkService linkService = AssociatesAPI.getLinkService();
                    linkService.openRetailPage(request);
                    Ads.trackScreenName("Shop on Amazon - Ebola");
                } catch (NotInitializedException e) {
                }
            }
        });

        Ads.setupGoogleAdwords(this, R.id.googleAdsViewTop);
        Ads.setupGoogleAdwords(this, R.id.googleAdsViewBottom);
        Ads.setupGoogleAnalytics(this, "UA-56762502-2");
        Ads.setupAmazonAds(this, AMAZON_APP_KEY, 0);
        progress.setMax(720);

        buildGoogleApiClient();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(POLLING_FREQ);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_FREQ);

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
        text.setVisibility(View.INVISIBLE);
        headline.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.VISIBLE);
        AppEventsLogger.activateApp(this);

        Ads.trackScreenName("Radar activity resumed.");

        te = new Timer();
        tu = new TimerTask() {
            int angle = 0;

            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        radarView.setRotation(angle);
                        progress.setProgress(angle);
                        angle += 1;
                        if (angle == 720) {

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

        if (mMap == null) {
            return;
        }

        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
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

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        // Schedule a runnable to unregister location listeners
        Executors.newScheduledThreadPool(1).schedule(new Runnable() {

            @Override
            public void run() {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, RadarActivity.this);
            }

        }, TimeUnit.MINUTES.toMillis(3), TimeUnit.MILLISECONDS);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {

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
}

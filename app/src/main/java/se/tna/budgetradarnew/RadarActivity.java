package se.tna.budgetradarnew;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AppEventsLogger;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;


public class RadarActivity extends Activity {
    ImageView radarView;
    ImageView redButton;
    ImageView retry;

    View button;
    TextView headline, text;


    Timer te;
    TimerTask tu;
    ProgressBar progress;
    private UiLifecycleHelper uiHelper;
    private GoogleMap mMap;

    RelativeLayout.LayoutParams redLayout;

    float[] mValuesMagnet = new float[3];
    float[] mValuesAccel = new float[3];
    float[] mValuesOrientation = new float[3];
    float[] mRotationMatrix = new float[9];

    double chance = Math.random() * 80 + 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar);

        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
        progress = (ProgressBar) findViewById(R.id.progress);
        radarView = (ImageView) findViewById(R.id.radar);
        redButton = (ImageView) findViewById(R.id.redIcon);
        retry = (ImageView) findViewById(R.id.retry);
        redLayout = (RelativeLayout.LayoutParams) redButton.getLayoutParams();

        progress.setMax(720);

        retry.setClickable(true);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Ads.trackScreenName("Rescan initiated");
                restart();
            }
        });

        redButton.setClickable(true);
        redButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog ad = new AlertDialog.Builder(RadarActivity.this).create();
                ad.setCancelable(false); // This blocks the 'BACK' button
                ad.setMessage("Tyvärr, denna budget var kass. Försök gärna igen.");
                ad.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.setButton(DialogInterface.BUTTON_POSITIVE, "Dela på Facebook", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(RadarActivity.this)
                                .setLink("https://play.google.com/store/apps/details?id=se.tna.budgetradarnew")
                                .setCaption("Var är vår budget?")
                                .setDescription("Ladda hem appen och försök hitta en bra budget.")
                                .build();
                        uiHelper.trackPendingDialogCall(shareDialog.present());
                        Ads.trackScreenName("Share on Faceboo Inner");
                    }
                });
                ad.show();
            }
        });

        redButton.setAlpha(0f);

        headline = (TextView) findViewById(R.id.headline);
        text = (TextView) findViewById(R.id.text);
        button = findViewById(R.id.button);
        button.setClickable(true);

        SensorManager sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        final SensorEventListener mEventListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                // Handle the events for which we registered
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        System.arraycopy(event.values, 0, mValuesAccel, 0, 3);
                        break;

                    case Sensor.TYPE_MAGNETIC_FIELD:
                        System.arraycopy(event.values, 0, mValuesMagnet, 0, 3);
                        break;
                }
            }

            ;
        };

        // You have set the event lisetner up, now just need to register this with the
        // sensor manager along with the sensor wanted.
        setListners(sensorManager, mEventListener);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(RadarActivity.this)
                        .setLink("https://play.google.com/store/apps/details?id=se.tna.budgetradarnew")
                        .setCaption("Var är vår budget?")
                        .setDescription("Simulera chanserna till att hitta en bra budget!")
                        .build();
                uiHelper.trackPendingDialogCall(shareDialog.present());
                Ads.trackScreenName("Share on Facebook");
            }
        });

        /*
        TextView amazonButton = (TextView) findViewById(R.id.amazonButton);
        amazonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Ads.trackScreenName("budget");
                OpenSearchPageRequest request = new OpenSearchPageRequest("flirt");
                try {
                    LinkService linkService = AssociatesAPI.getLinkService();
                    linkService.openRetailPage(request);
                } catch (NotInitializedException e) {

                }
            }
        });
        */

        Ads.setupGoogleAdwords(this, R.id.googleAdView);
        Ads.setupGoogleAnalytics(this, "UA-56762502-4");
        //Ads.setupAmazonAds(this, APP_KEY, R.id.amazonAdView);
    }


    // Register the event listener and sensor type.
    public void setListners(SensorManager sensorManager, SensorEventListener mEventListener) {
        sensorManager.registerListener(mEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                       SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(mEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                                       SensorManager.SENSOR_DELAY_NORMAL);
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

    private void restart() {
        radarView.setVisibility(View.VISIBLE);
        chance = Math.random() * 80 + 10;
        randomizer = Math.random() - 0.5;
        text.setVisibility(View.INVISIBLE);
        //headline.setVisibility(View.INVISIBLE);
        headline.setText("Söker efter en bra budget!!");
        progress.setVisibility(View.VISIBLE);
        redButton.setAlpha(0f);
        if (te != null) {
            te.cancel();
        }
        if (tu != null) {
            tu.cancel();
        }
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
                            te.cancel();
                            tu.cancel();
                            animation1.cancel();
                            redButton.setAlpha(1f);

                            redButton.setVisibility(View.VISIBLE);
                            //radarView.setVisibility(View.INVISIBLE);
                            text.setVisibility(View.VISIBLE);
                            headline.setText("Det kan finnas en budget!");
                            progress.setVisibility(View.GONE);

                        }

                        SensorManager.getRotationMatrix(mRotationMatrix, null, mValuesAccel, mValuesMagnet);
                        SensorManager.getOrientation(mRotationMatrix, mValuesOrientation);
                        final CharSequence test;

                        mValuesOrientation[1] += 0.9;
                        mValuesOrientation[2] += randomizer;
                        double leftMargin = (mValuesOrientation[2] / Math.PI + 0.5);
                        double topMargin = -(mValuesOrientation[1] / Math.PI - 0.5);

                        double x = mValuesOrientation[2];
                        double y = mValuesOrientation[1];
                        double rangle = Math.atan(Math.abs(x) / Math.abs(y));
                        if (x > 0 && y < 0) {
                            rangle = Math.atan(Math.abs(y) / Math.abs(x));
                            rangle += Math.PI / 2f;
                        } else if (x < 0 && y < 0) {
                            rangle += Math.PI;
                        } else if (x < 0 && y > 0) {
                            rangle = Math.atan(Math.abs(y) / Math.abs(x));
                            rangle += Math.PI + Math.PI / 2f;
                        }

                        rangle = rangle * 180 / Math.PI;

                        if (Math.round(rangle) == angle % 360) {
                            redLayout.topMargin = (int) (topMargin * 885);
                            redLayout.leftMargin = (int) (leftMargin * 885);
                            redButton.setLayoutParams(redLayout);
                            redButton.invalidate();
                            redButton.setAlpha(1f);
                            animation1 = new AlphaAnimation(1.0f, 0f);
                            animation1.setDuration(4000);
                            animation1.setFillAfter(true);
                            redButton.startAnimation(animation1);
                        }
                    }
                });
            }
        };

        te.scheduleAtFixedRate(tu, 1, 10);
    }

    AlphaAnimation animation1;
    double randomizer = Math.random() - 0.3;

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
        AppEventsLogger.activateApp(this);

        restart();


        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mMap != null) {
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.getUiSettings().setZoomGesturesEnabled(false);
            mMap.getUiSettings().setAllGesturesEnabled(false);
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null && mMap != null) {

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
        Ads.googleAdsDestroy();
    }
}

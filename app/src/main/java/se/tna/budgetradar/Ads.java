package se.tna.budgetradar;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.DefaultAdListener;
import com.amazon.device.associates.AssociatesAPI;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


/**
 * Created by 23007272 on 2014-11-28.
 */
public class Ads {
    private static final String LOG_TAG = "Ads Utility";

    public static void setupGoogleAdwords(Activity activity, int resId)
    {
        AdView adView = (AdView) activity.findViewById(resId);
        adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private static Tracker mainTracker;
    public static void setupGoogleAnalytics(Activity activity, String id){
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(activity);
        mainTracker = analytics.newTracker(R.xml.global_tracker);
        mainTracker.setAppId(id);
        mainTracker.enableAutoActivityTracking(true);
        mainTracker.enableAdvertisingIdCollection(true);
        mainTracker.enableExceptionReporting(true);
    }

    public static void trackScreenName(String name)
    {
        if (mainTracker != null) {
            mainTracker.setScreenName(name);
            mainTracker.send(new HitBuilders.AppViewBuilder().build());
        }
    }

    public static void setupGoogleMaps(){


    }

    private static AdLayout amazonAd;
    public static void setupAmazonAds(Activity activity, String amazonAccountKey, int resId) {
        // For debugging purposes enable logging, but disable for production builds.
        AdRegistration.enableLogging(false);
        // For debugging purposes flag all ad requests as tests, but set to false for production builds.
        AdRegistration.enableTesting(false);

        amazonAd = (AdLayout) activity.findViewById(resId);
        amazonAd.setListener(new SampleAdListener());
        try {
            AdRegistration.setAppKey(amazonAccountKey);
        } catch (final IllegalArgumentException e) {
            Log.e(LOG_TAG, "IllegalArgumentException thrown: " + e.toString());
            return;
        }
        amazonAd.loadAd();
        AssociatesAPI.initialize(new AssociatesAPI.Config(amazonAccountKey, activity));
    }

    static class SampleAdListener extends DefaultAdListener {
        /**
         * This event is called once an ad loads successfully.
         */
        @Override
        public void onAdLoaded(final Ad ad, final AdProperties adProperties) {
            Log.i(LOG_TAG, adProperties.getAdType().toString() + " ad loaded successfully.");
        }

        /**
         * This event is called if an ad fails to load.
         */
        @Override
        public void onAdFailedToLoad(final Ad ad, final AdError error) {
            Log.w(LOG_TAG, "Ad failed to load. Code: " + error.getCode() + ", Message: " + error.getMessage());
        }

        /**
         * This event is called after a rich media ad expands.
         */
        @Override
        public void onAdExpanded(final Ad ad) {
            Log.i(LOG_TAG, "Ad expanded.");
            // You may want to pause your activity here.
        }

        /**
         * This event is called after a rich media ad has collapsed from an expanded state.
         */
        @Override
        public void onAdCollapsed(final Ad ad) {
            Log.i(LOG_TAG, "Ad collapsed.");
            // Resume your activity here, if it was paused in onAdExpanded.
        }
    }
}

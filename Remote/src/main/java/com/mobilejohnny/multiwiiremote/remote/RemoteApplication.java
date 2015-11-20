package com.mobilejohnny.multiwiiremote.remote;

import android.app.Application;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.xiaomi.mistatistic.sdk.MiStatInterface;
import com.xiaomi.mistatistic.sdk.URLStatsRecorder;

/**
 * Created by admin on 2015/11/20.
 */
public class RemoteApplication extends Application {

    private static final String MI_APPID = "2882303761517413298";
    private static final String MI_APP_KEY = "5981741321298";
    private static final String CHANNEL = "default channel";
    private GoogleAnalytics analytics;
    private static Tracker tracker;

    public static Tracker tracker()
    {
        return tracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        analytics = GoogleAnalytics.getInstance(this);
        analytics.enableAutoActivityReports(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker("UA-15153331-6"); // Replace with actual tracker/property Id
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);

//        tracker.send(new HitBuilders.ScreenViewBuilder().build());
//        analytics.dispatchLocalHits();

        MiStatInterface.initialize(this, MI_APPID, MI_APP_KEY, CHANNEL);
        MiStatInterface.setUploadPolicy(MiStatInterface.UPLOAD_POLICY_BATCH,0);
        MiStatInterface.enableLog();

        // enable exception catcher.
        MiStatInterface.enableExceptionCatcher(true);
        // enable network monitor
        URLStatsRecorder.enableAutoRecord();
    }
}

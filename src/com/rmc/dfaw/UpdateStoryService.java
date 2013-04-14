package com.rmc.dfaw;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.RemoteViews;
import org.w3c.dom.Document;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;

public class UpdateStoryService extends IntentService {
    final int TITLE_INDEX = 0;
    final int SUMMARY_INDEX = 1;
    final String WIKI_FEATURED_ARTICLE_PATH = "https://en.wikipedia.org/w/api.php?action=featuredfeed&feed=featured&feedformat=atom";
    final String WIKI_TODAY_IN_HISTORY_PATH = "https://en.wikipedia.org/w/api.php?action=featuredfeed&feed=onthisday&feedformat=atom";
    final String WIKI_BASE_URL = "https://en.wikipedia.org";
    final String WIKI_SLASH_WIKI_URL = WIKI_BASE_URL + "/wiki/";
    final String YEAR_PATTERN = "([0-9]{3,4}[\\s])([–])([\\s])";

    public UpdateStoryService() {
        super("IntentService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
                .getApplicationContext());

        int[] allWidgetIds = intent
                .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        ComponentName thisWidget = new ComponentName(getApplicationContext(),
                WikiWidgetProviderBase.class);

        // load in the sharefprefs to see the update settings.
        SharedPreferences settings = this.getSharedPreferences(
                WikiWidgetActivity.SHARED_PREF_NAME, MODE_PRIVATE);

        long settingsWidgetType = settings.getLong(
                WikiWidgetActivity.WIDGET_TYPE_KEY, 0);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connManager != null) {
            NetworkInfo wifiInfo = connManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobileInfo = connManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            long savedWidgetType;
            for (int i = 0; i < allWidgetIds.length; i++) {
                boolean wifiOnly = false;
                boolean configDone = true;
                // for compat reasons need to check both
                if (settings
                        .contains((allWidgetIds[i]
                                + WikiWidgetActivity.NETWORK_TYPE_PREF + WikiWidgetActivity.APP_EXTENSION))) {
                    wifiOnly = settings.getBoolean(allWidgetIds[i]
                            + WikiWidgetActivity.NETWORK_TYPE_PREF
                            + WikiWidgetActivity.APP_EXTENSION, false);
                } else {
                    configDone = false;
                }

                if ((((mobileInfo != null && mobileInfo.isConnected()) && !wifiOnly) || (wifiInfo != null && wifiInfo
                        .isConnected())) && configDone) {

                    if (settings.contains(allWidgetIds[i]
                            + WikiWidgetActivity.WIDGET_TYPE_PREF
                            + WikiWidgetActivity.APP_EXTENSION)) {
                        savedWidgetType = settings.getLong(allWidgetIds[i]
                                + WikiWidgetActivity.WIDGET_TYPE_PREF
                                + WikiWidgetActivity.APP_EXTENSION, -1);
                        Log.v("UpdateStory", allWidgetIds[i]
                                + " WDW CONTAINS TYPEPREF " + savedWidgetType);
                    } else {
                        // new widget so set as settingsWidgetType and record
                        SharedPreferences.Editor settingsEditor = settings
                                .edit();
                        settingsEditor.putLong(allWidgetIds[i]
                                + WikiWidgetActivity.WIDGET_TYPE_PREF
                                + WikiWidgetActivity.APP_EXTENSION,
                                settingsWidgetType);
                        settingsEditor.commit();
                        savedWidgetType = settingsWidgetType;
                        Log.v("UpdateStory", allWidgetIds[i]
                                + " WDW DOESNT CONTAIN TYPEPREF "
                                + savedWidgetType);

                    }
                    WikiWidgetHandler wwh = null;

                    if (savedWidgetType == WikiWidgetActivity.FEATURED_OPTION) {
                        wwh = new FeaturedArticleHandler();

                    } else if (savedWidgetType == WikiWidgetActivity.TODAY_HISTORY_OPTION) {
                        wwh = new OnThisDayHandler();

                    } else {
                        Log.e("UpdateStory", "WDW SELECTED_URL UNKNOWN "
                                + savedWidgetType);
                        return;
                    }

                    HttpsURLConnection connection = wwh.getHTTPSConnection();

                    if (connection != null) {
                        InputStream urlInputStream = wwh
                                .getURLInputStream(connection);
                        if (urlInputStream != null) {
                            Document wikiDocument = wwh.getDOM(urlInputStream,
                                    connection);
                            if (wikiDocument != null) {
                                String[] story = wwh.parseFeed(wikiDocument);
                                if (story != null) {
                                    String storyURL = wwh
                                            .generateClickURL(story[1]);
                                    Intent browserIntent = new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(storyURL));
                                    PendingIntent openStory = PendingIntent
                                            .getActivity(getBaseContext(), 0,
                                                    browserIntent, 0);

                                    RemoteViews views = new RemoteViews(
                                            thisWidget.getPackageName(),
                                            R.layout.wikiwidgetlayout_background);

                                    views.setOnClickPendingIntent(
                                            R.id.storySummary, openStory);

                                    Spanned summary = Html.fromHtml(
                                            story[SUMMARY_INDEX], null, null);

                                    String strSummary = wwh
                                            .cleanupSummary(summary.toString());

                                    if (!strSummary.equals("...")) {
                                        // update labels
                                        views.setTextViewText(R.id.storyHeading,
                                                story[TITLE_INDEX]);
                                        views.setTextViewText(R.id.storySummary,
                                                strSummary);

                                        // Tell the AppWidgetManager to perform an
                                        // update on
                                        // the
                                        // current
                                        appWidgetManager.updateAppWidget(
                                                allWidgetIds[i], views);

                                    }
                                }
                            }

                        }
                        connection.disconnect();

                    }

                }
            }
        }
    }
}

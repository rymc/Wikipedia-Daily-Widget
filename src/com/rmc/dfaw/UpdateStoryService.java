package com.rmc.dfaw;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.text.Html;
import android.text.Spanned;
import android.widget.RemoteViews;

public class UpdateStoryService extends IntentService {
	final int TITLE_INDEX = 0;
	final int SUMMARY_INDEX = 1;

	final String WIKI_FEATURED_ARTICLE_PATH = "https://en.wikipedia.org/w/api.php?action=featuredfeed&feed=featured&feedformat=atom";
	final String WIKI_TODAY_IN_HISTORY_PATH = "https://en.wikipedia.org/w/api.php?action=featuredfeed&feed=onthisday&feedformat=atom";
	final String WIKI_BASE_URL = "https://en.wikipedia.org";
	final String WIKI_SLASH_WIKI_URL = WIKI_BASE_URL + "/wiki/";

	public UpdateStoryService() {
		super("IntentService");
	}

	private String extractStoryURL(String summary) {

		org.jsoup.nodes.Document doc = Jsoup.parse(summary);

		Elements links = doc.select("a[href]");
		String storyLink = null;
		for (int z = 0; z < links.size(); z++) {
			if (links.get(z).hasAttr("href")
					&& links.get(z).text().equals("more..."))
				storyLink = links.get(z).attr("href");
		}

		return storyLink;
	}

	/**
	 * Removes unwanted characters from the text
	 * 
	 * @param strSummary
	 *            the Text to perform the replace on
	 * @param widgetType
	 * @return the cleaned String
	 */
	private String cleanupSummary(String strSummary, long widgetType) {
		strSummary = strSummary.replace('￼', '\0');
		strSummary = strSummary.replace('\n', '\0');
		// remove the unicode character that are inserted
		strSummary = strSummary.replaceAll("[\u0000]", "");

		if (widgetType == WikiWidgetActivity.TODAY_HISTORY_OPTION) {
			String temp[] = strSummary.split("More anniversaries:");
			strSummary = temp[0] + "\nTap to see more..";
		} else if (widgetType == WikiWidgetActivity.FEATURED_OPTION) {
			String temp[] = strSummary.split("\\(more...\\)");
			strSummary = temp[0] + "\nTap to see more..";
		}

		return strSummary;

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream getURLInputStream(String urlString) {
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e1) {
			System.err.println("MalformedURLException " + e1.getCause());
			return null;
		}

		URLConnection s = null;
		InputStream s2;
		try {
			s = url.openConnection();
			s2 = s.getInputStream();

		} catch (IOException e1) {
			System.err.println("IOException opening url inputsteam"
					+ e1.getCause());
			return null;
		}
		return s2;
	}

	public Document getDOM(InputStream urlInputStream) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err
					.println("ParserConfigurationException creating dom builder "
							+ e.getCause());
			return null;
		}
		Document dom;

		try {
			dom = builder.parse(urlInputStream);
		} catch (SAXException e) {
			System.err.println("SAXException parsing dom " + e.getCause());
			e.printStackTrace();
			return null;

		} catch (IOException e) {
			System.err.println("IOException parsing dom" + e.getCause());
			return null;
		}

		try {
			urlInputStream.close();
		} catch (IOException e) {
			System.err.println("IOException closing url input stream"
					+ e.getCause());
		}

		return dom;
	}

	public String[] parseFeed(Document dom) {
		String[] story = new String[2];
		if (dom != null) {

			final String HTML_ENTRY_TAG = "entry";
			final String HTML_TITLE_TAG = "title";
			final String HTML_SUMMARY_TAG = "summary";
			String val = null;
			String summary = null;

			Element root = dom.getDocumentElement();

			NodeList items = root.getElementsByTagName(HTML_ENTRY_TAG);
			int i = items.getLength() - 1;
			// stops it breaking < 4.0
			items.item(i).normalize();
			Node item = items.item(i);
			NodeList properties = item.getChildNodes();
			for (int j = 0; j < properties.getLength(); j++) {
				Node property = properties.item(j);
				String name = property.getNodeName();
				if (name.equalsIgnoreCase(HTML_TITLE_TAG)) {
					val = property.getFirstChild().getNodeValue();
				} else if (name.equalsIgnoreCase(HTML_SUMMARY_TAG)) {
					for (int k = 0; k < property.getChildNodes().getLength(); k++) {
						summary = property.getFirstChild().getNodeValue();
					}
				}
			}

			story[0] = val;
			story[1] = summary;
		} else {
			story = null;
		}
		return story;
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
		boolean wifiOnly = settings.getBoolean(
				WikiWidgetActivity.WIFI_MOBILE_KEY, false);
		long widgetType = settings.getLong(WikiWidgetActivity.WIDGET_TYPE_KEY,
				0);

		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo wifiInfo = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobileInfo = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		String SELECTED_URL;
		if (widgetType == WikiWidgetActivity.FEATURED_OPTION) {
			SELECTED_URL = WIKI_FEATURED_ARTICLE_PATH;
		} else if (widgetType == WikiWidgetActivity.TODAY_HISTORY_OPTION) {
			SELECTED_URL = WIKI_TODAY_IN_HISTORY_PATH;
		} else {
			SELECTED_URL = WIKI_BASE_URL;
		}
		if ((mobileInfo.isConnected() && !wifiOnly) || wifiInfo.isConnected()) {

			for (int i = 0; i < allWidgetIds.length; i++) {

				RemoteViews views = new RemoteViews(
						thisWidget.getPackageName(),
						R.layout.wikiwidgetlayout_background);

				InputStream urlInputStream = getURLInputStream(SELECTED_URL);

				String[] story = parseFeed(getDOM(urlInputStream));

				if (story != null) {

					String storyURL;
					if (widgetType == WikiWidgetActivity.FEATURED_OPTION) {
						storyURL = extractFeaturedArticleURL(story);
					} else {
						storyURL = generateTodayInHistoryURL();
					}
					Intent browserIntent = new Intent(Intent.ACTION_VIEW,
							Uri.parse(storyURL));
					PendingIntent openStory = PendingIntent.getActivity(
							getBaseContext(), 0, browserIntent, 0);

					views.setOnClickPendingIntent(R.id.storySummary, openStory);

					Spanned summary = Html.fromHtml(story[SUMMARY_INDEX], null,
							null);

					String strSummary = cleanupSummary(summary.toString(),
							widgetType);

					if (widgetType == WikiWidgetActivity.TODAY_HISTORY_OPTION) {
						strSummary = formatTodayInHistoryText(strSummary);
					}

					// update labels
					views.setTextViewText(R.id.storyHeading, story[TITLE_INDEX]);
					views.setTextViewText(R.id.storySummary, strSummary);

					// Tell the AppWidgetManager to perform an update on the
					// current
					appWidgetManager.updateAppWidget(allWidgetIds[i], views);
				}

			}
		}
		stopSelf();

	}

	private String formatTodayInHistoryText(String strSummary) {
		LinkedList<String> yearToItem;
		yearToItem = insertNewlines(strSummary);

		String[] s = strSummary.split("([0-9][0-9][0-9][0-9][\\s-])");

		strSummary = "";
		for (int j = 0; j < s.length; j++) {
			if (j > 0) {
				strSummary += yearToItem.get(j - 1) + s[j] + "\n";
			} else {
				strSummary = s[j] + "\n";
			}
		}
		return strSummary;
	}

	private String generateTodayInHistoryURL() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat month = new SimpleDateFormat("MMMMMMMMM");
		String monthName = month.format(calendar.getTime());
		SimpleDateFormat day = new SimpleDateFormat("dd");
		String dayName = day.format(calendar.getTime());

		return WIKI_SLASH_WIKI_URL + monthName + "_" + dayName;
	}

	private LinkedList<String> insertNewlines(String strSummary) {

		LinkedList<String> yearToItem = new LinkedList<String>();

		Pattern years = Pattern.compile("([0-9][0-9][0-9][0-9][\\s-])");
		Matcher m = years.matcher(strSummary);
		while (m.find()) {
			yearToItem.add(m.group(0));
		}

		return yearToItem;

	}

	private String extractFeaturedArticleURL(String[] story) {

		String storyLink = extractStoryURL(story[SUMMARY_INDEX]);

		String storyURL = null;
		if (storyLink != null) {
			storyURL = WIKI_BASE_URL + storyLink;
		} else {
			storyURL = WIKI_BASE_URL;
			System.err.println("Unable to get URL for the complete article");
		}

		return storyURL;
	}
}

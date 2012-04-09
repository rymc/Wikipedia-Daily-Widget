package com.ryan;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.text.Html;
import android.text.Spanned;
import android.widget.RemoteViews;

public class UpdateStoryService extends Service {

	public void onStart(Intent intent, int startID) {
		final int TITLE_INDEX = 0;
		final int SUMMARY_INDEX = 1;
		final String WIKI_FEATURED_ARTICLE_PATH = "https://en.wikipedia.org/w/api.php?action=featuredfeed&feed=featured&feedformat=atom";
		String wikiBaseURL = "https://en.wikipedia.org";

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		int[] allWidgetIds = intent
				.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				WikiWidgetProviderBase.class);

		for (int i = 0; i < allWidgetIds.length; i++) {

			RemoteViews views = new RemoteViews(thisWidget.getPackageName(),
					R.layout.wikiwidgetlayout_background);

			String[] story = parse(WIKI_FEATURED_ARTICLE_PATH);

			String storyLink = extractStoryURL(story[SUMMARY_INDEX]);

			String storyURL = null;
			if (storyLink != null) {
				storyURL = wikiBaseURL + storyLink;
			} else {
				storyURL = wikiBaseURL;
				System.err
						.println("Unable to get URL for the complete article");
			}

			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(storyURL));
			PendingIntent openStory = PendingIntent.getActivity(
					getBaseContext(), 0, browserIntent, 0);

			views.setOnClickPendingIntent(R.id.storySummary, openStory);

			Spanned summary = Html.fromHtml(story[SUMMARY_INDEX], null, null);

			String strSummary = cleanupSummary(summary.toString());
			

			// update labels
			views.setTextViewText(R.id.storyHeading, story[TITLE_INDEX]);
			views.setTextViewText(R.id.storySummary, strSummary);

			// Tell the AppWidgetManager to perform an update on the current app
			// widget
			appWidgetManager.updateAppWidget(allWidgetIds[i], views);

		}

		stopSelf();
		System.out.println("Finished service");
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
	 * @return the cleaned String
	 */
	private String cleanupSummary(String strSummary) {
		strSummary = strSummary.replace('￼', '\0');
		strSummary = strSummary.replace('\n', '\0');
		// remove the unicode character that are inserted
		strSummary = strSummary.replaceAll("[\u0000]", "");

		return strSummary;

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] parse(String urlString) {

		final String HTML_ENTRY_TAG = "entry";
		final String HTML_TITLE_TAG = "title";
		final String HTML_SUMMARY_TAG = "summary";
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		String val = null;
		String summary = null;

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(urlString);
			Element root = dom.getDocumentElement();

			NodeList items = root.getElementsByTagName(HTML_ENTRY_TAG);
			for (int i = 0; i < items.getLength(); i++) {
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
						for (int k = 0; k < property.getChildNodes()
								.getLength(); k++)
							summary = property.getFirstChild().getNodeValue();
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Unable to parse atom feed");
		}
		String[] story = { val, summary };
		return story;
	}
}

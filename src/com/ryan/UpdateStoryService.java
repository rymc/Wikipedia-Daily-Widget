package com.ryan;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

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
import android.os.Build;
import android.os.IBinder;
import android.text.Html;
import android.text.Spanned;
import android.widget.RemoteViews;

public class UpdateStoryService extends Service {
	private static final String WIKI_FEATURED_ARTICLE_PATH = "https://en.wikipedia.org/w/api.php?action=featuredfeed&feed=featured&feedformat=atom";
	private String wikiBaseURL = "https://en.wikipedia.org";

	public void onStart(Intent intent, int startID) {

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		int[] allWidgetIds = intent
				.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				WikiWidgetProviderBase.class);

		for (int i = 0; i < allWidgetIds.length; i++) {

			RemoteViews views = new RemoteViews(thisWidget.getPackageName(),
					R.layout.wikiwidgetlayout_background);

			System.out.println("layout is " + views.getLayoutId());
			URL feedUrl = null;

			try {
				feedUrl = new URL(WIKI_FEATURED_ARTICLE_PATH);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			String[] story = parse(feedUrl.toString());

			org.jsoup.nodes.Document doc = Jsoup.parse(story[1]);

			Elements links = doc.select("a[href]");
			String storyLink = null;
			for (int z = 0; z < links.size(); z++) {
				if (links.get(z).hasAttr("href")
						&& links.get(z).text().equals("more..."))
					storyLink = links.get(z).attr("href");
			}

			String storyURL = wikiBaseURL + storyLink;

			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(storyURL));
			PendingIntent pi = PendingIntent.getActivity(getBaseContext(), 0,
					browserIntent, 0);

			views.setOnClickPendingIntent(R.id.storySummary, pi);

			Spanned summary = Html.fromHtml(story[1], null, null);
			String strSummary = summary.toString();

			strSummary = strSummary.replace('￼', '\0');
			strSummary = strSummary.replace('\n', '\0');
			// remove the unicode character that are inserted
			strSummary = strSummary.replaceAll("[\u0000]", "");

			System.out.println("Summary Finished " + strSummary);
			// update labels
			views.setTextViewText(R.id.storyHeading, story[0]);
			views.setTextViewText(R.id.storySummary, strSummary);

			// Tell the AppWidgetManager to perform an update on the current app
			// widget
			appWidgetManager.updateAppWidget(allWidgetIds[i], views);

		}

		stopSelf();
		System.out.println("Finished service");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] parse(String string) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		String val = null;
		String summary = null;

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(string);
			Element root = dom.getDocumentElement();

			NodeList items = root.getElementsByTagName("entry");
			for (int i = 0; i < items.getLength(); i++) {
				// stops it breaking < 4.0
				items.item(i).normalize();
				Node item = items.item(i);
				NodeList properties = item.getChildNodes();
				for (int j = 0; j < properties.getLength(); j++) {
					Node property = properties.item(j);
					String name = property.getNodeName();
					if (name.equalsIgnoreCase("title")) {
						val = property.getFirstChild().getNodeValue();
					} else if (name.equalsIgnoreCase("summary")) {
						for (int k = 0; k < property.getChildNodes()
								.getLength(); k++)
							summary = property.getFirstChild().getNodeValue();
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		String[] story = { val, summary };
		return story;
	}
}

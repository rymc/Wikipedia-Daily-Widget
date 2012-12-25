package com.rmc.dfaw;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import android.util.Log;

public class FeaturedArticleHandler extends WikiWidgetHandler {
	final static String WIKI_FEATURED_ARTICLE_PATH = "https://en.wikipedia.org/w/api.php?action=featuredfeed&feed=featured&feedformat=atom";

	public FeaturedArticleHandler() {
		super(WIKI_FEATURED_ARTICLE_PATH);

	}

	public String generateClickURL(String summary) {

		org.jsoup.nodes.Document doc = Jsoup.parse(summary);

		Elements links = doc.select("a[href]");
		String storyLink = null;
		for (int z = 0; z < links.size(); z++) {
			if (links.get(z).hasAttr("href")
					&& links.get(z).text().equals("Full article...")) {
				storyLink = links.get(z).attr("href");
			}
		}
		if (storyLink == null) {
			storyLink = "";
		}
		return WIKI_BASE_URL + storyLink;
	}

	public String extractFeaturedArticleURL(String[] story) {

		String storyLink = generateClickURL(story[SUMMARY_INDEX]);

		String storyURL = null;
		if (storyLink != null) {
			storyURL = WIKI_BASE_URL + storyLink;
		} else {
			storyURL = WIKI_BASE_URL;
			Log.e("UpdateStory", "Unable to get URL for the complete article");
		}

		return storyURL;
	}

	public String cleanupSummary(String strSummary) {
		strSummary = super.cleanupSummary(strSummary);
		String temp[] = strSummary.split("\\(more...\\)");
		return strSummary = temp[0] + "\nTap to see more..";
	}
}

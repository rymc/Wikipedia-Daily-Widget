package com.rmc.dfaw;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;

public class WikiWidgetHandler {
	final int TITLE_INDEX = 0;
	final int SUMMARY_INDEX = 1;

	final String WIKI_BASE_URL = "https://en.wikipedia.org";
	final String WIKI_SLASH_WIKI_URL = WIKI_BASE_URL + "/wiki/";
	private String urlString;

	public WikiWidgetHandler(String urlString) {
		this.urlString = urlString;
	}

	public HttpsURLConnection getHTTPSConnection() {
		URL url = null;

		try {
			url = new URL(urlString);
		} catch (MalformedURLException e1) {
			Log.e("UpdateStory", "MalformedURLException " + e1.getCause());
			return null;
		}

		System.setProperty("http.keepAlive", "false");
		HttpsURLConnection s = null;
		try {
			s = (HttpsURLConnection) url.openConnection();
		} catch (IOException e) {
			Log.e("UpdateStory", "IOException opening url" + e.getCause());
			e.printStackTrace();
			return null;
		}
		s.setDoOutput(true);
		return s;
	}

	public InputStream getURLInputStream(HttpsURLConnection s) {

		if (s != null) {
			InputStream s2 = null;
			try {
				s2 = new BufferedInputStream(s.getInputStream());
			} catch (IOException e) {
				Log.e("UpdateStory",
						"IOException getInputStream " + e.getCause());
				e.printStackTrace();
				return null;
			}

			return s2;
		}
		return null;
	}

	public Document getDOM(InputStream urlInputStream, HttpsURLConnection s) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			Log.e("UpdateStory",
					"ParserConfigurationException creating dom builder "
							+ e.getCause());
			return null;
		}
		Document dom;

		try {
			dom = builder.parse(urlInputStream);
		} catch (SAXException e) {
			Log.e("UpdateStory", "SAXException parsing dom " + e.getCause());
			e.printStackTrace();
			try {
				urlInputStream.close();
				s.disconnect();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;

		} catch (IOException e) {
			Log.e("UpdateStory", "IOException parsing dom" + e.getCause());
			try {
				urlInputStream.close();
				s.disconnect();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
		}

		try {
			urlInputStream.close();
			s.disconnect();
		} catch (IOException e) {
			Log.e("UpdateStory",
					"IOException closing url input stream" + e.getCause());
		}
		// builder.reset();

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

	/**
	 * Removes unwanted characters from the text
	 * 
	 * @param strSummary
	 *            the Text to perform the replace on
	 * @return the cleaned String
	 */
	public String cleanupSummary(String strSummary) {
		strSummary = strSummary.replace('￼', '\0');
		strSummary = strSummary.replace('\n', '\0');
		// remove the unicode character that are inserted
		strSummary = strSummary.replaceAll("[\u0000]", "");
		strSummary = strSummary.replaceAll("\\(pictured\\)", "");
		return strSummary;
	}

	public String generateClickURL(String summary) {
		return summary;

	}

}

package com.rmc.dfaw;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OnThisDayHandler extends WikiWidgetHandler {
	final static String WIKI_TODAY_IN_HISTORY_PATH = "https://en.wikipedia.org/w/api.php?action=featuredfeed&feed=onthisday&feedformat=atom";

	final String YEAR_PATTERN = "([0-9]{3,4}[\\s])([–])([\\s])";

	public OnThisDayHandler() {
		super(WIKI_TODAY_IN_HISTORY_PATH);

	}

	public String formatTodayInHistoryText(String strSummary) {
		LinkedList<String> yearToItem = insertNewlines(strSummary);

		String[] s = strSummary.split(YEAR_PATTERN);

		strSummary = "";
		for (int j = 0; j < s.length; j++) {
			if (j > 0) {
				strSummary += yearToItem.get(j - 1) + "\n" + s[j] + "\n";
			} else {
				strSummary = s[j].trim() + ".\n";
			}
		}
		return strSummary;
	}

	private LinkedList<String> insertNewlines(String strSummary) {

		LinkedList<String> yearToItem = new LinkedList<String>();
		Pattern years = Pattern.compile(YEAR_PATTERN);
		Matcher m = years.matcher(strSummary);
		while (m.find()) {
			yearToItem.add(m.group(1));
		}

		return yearToItem;
	}

	public String cleanupSummary(String strSummary) {
		strSummary = super.cleanupSummary(strSummary);
		String temp[] = strSummary.split("More anniversaries:");
		strSummary = temp[0] + "\nTap to see more..";
		return formatTodayInHistoryText(strSummary);

	}

	public String generateClickURL(String strSummary) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat month = new SimpleDateFormat("MMMMMMMMM");
		String monthName = month.format(calendar.getTime());
		SimpleDateFormat day = new SimpleDateFormat("dd");
		String dayName = day.format(calendar.getTime());

		return WIKI_SLASH_WIKI_URL + monthName + "_" + dayName;
	}

}

package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventResourceClassifier;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;

public class ResourceTimeUtils {

	private final static XEventClassifier classifier = new XEventResourceClassifier();

	public static String getResource(XEvent event) {
		return classifier.getClassIdentity(event);
	}

	public static Long getTimestamp(XEvent event) {
		Date date = XTimeExtension.instance().extractTimestamp(event);
		if (date != null) {
			return date.getTime();
		}
		return null;
	}

	public static final String getTimePerUnitString(double ms, String otherUnit) {
		if (ms > 1) {
			return String.format("%.2f", ms) + " " + otherUnit + "\\milisecond";
		}
		double s = ms * 1000;
		if (s > 1) {
			return String.format("%.2f", s) + " " + otherUnit + "\\second";
		}
		double m = s * 60;
		if (m > 1) {
			return String.format("%.2f", m) + " " + otherUnit + "\\minute";
		}
		double h = m * 60;
		if (h > 1) {
			return String.format("%.2f", h) + " " + otherUnit + "\\hour";
		}
		double d = h * 24;
		return String.format("%.2f", d) + " " + otherUnit + "\\day";
	}
	
	public static final String getTimeUnitWithoutMeasure(double ms, String otherUnit) {
		if (ms > 1) {
			return " " + otherUnit + "\\milisecond";
		}
		double s = ms * 1000;
		if (s > 1) {
			return " " + otherUnit + "\\second";
		}
		double m = s * 60;
		if (m > 1) {
			return " " + otherUnit + "\\minute";
		}
		double h = m * 60;
		if (h > 1) {
			return " " + otherUnit + "\\hour";
		}
		double d = h * 24;
		return " " + otherUnit + "\\day";
	}

	private static final ThreadLocal<SoftReference<DateFormat>> DATE_FORMAT_0 = new ThreadLocal<SoftReference<DateFormat>>();
	private static final ThreadLocal<SoftReference<DateFormat>> DATE_FORMAT_1 = new ThreadLocal<SoftReference<DateFormat>>();
	private static final ThreadLocal<SoftReference<DateFormat>> DATE_FORMAT_2 = new ThreadLocal<SoftReference<DateFormat>>();
	private static final ThreadLocal<SoftReference<DateFormat>> DATE_FORMAT_3 = new ThreadLocal<SoftReference<DateFormat>>();
	private static final ThreadLocal<SoftReference<DateFormat>> DATE_FORMAT_4 = new ThreadLocal<SoftReference<DateFormat>>();

	@SuppressWarnings("deprecation")
	public static String timeToString(Long timestamp) {
		if (timestamp != null) {
			Date date = new Date(timestamp);
			if (date.getTime() % 1000 != 0) {
				return getThreadLocaleDateFormat("dd-MM-yyyy HH:mm:ss:SSS", DATE_FORMAT_0).format(date);
			} else if (date.getSeconds() != 0) {
				return getThreadLocaleDateFormat("dd-MM-yyyy HH:mm:ss", DATE_FORMAT_1).format(date);
			} else if (date.getMinutes() != 0) {
				return getThreadLocaleDateFormat("dd-MM-yyyy HH:mm", DATE_FORMAT_2).format(date);
			} else if (date.getHours() != 0) {
				return getThreadLocaleDateFormat("dd-MM-yyyy HHh", DATE_FORMAT_3).format(date);
			} else {
				return getThreadLocaleDateFormat("dd-MM-yyyy", DATE_FORMAT_4).format(date);
			}
		} else {
			return null;
		}
	}

	private static DateFormat getThreadLocaleDateFormat(String formatString,
			ThreadLocal<SoftReference<DateFormat>> threadLocal) {
		SoftReference<DateFormat> softReference = threadLocal.get();
		if (softReference != null) {
			DateFormat dateFormat = softReference.get();
			if (dateFormat != null) {
				return dateFormat;
			}
		}
		DateFormat result = new SimpleDateFormat(formatString);
		softReference = new SoftReference<DateFormat>(result);
		threadLocal.set(softReference);
		return result;
	}
}

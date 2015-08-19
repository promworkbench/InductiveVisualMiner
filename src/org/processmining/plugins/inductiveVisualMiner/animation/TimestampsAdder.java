package org.processmining.plugins.inductiveVisualMiner.animation;

import gnu.trove.map.hash.THashMap;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;

public class TimestampsAdder {

	public static double animationDuration = 20;
	public static double beginEndEdgeDuration = 1;

	public static List<XEventClass> getTraceLogProjection(IMTrace trace, XEventClasses performanceEventClasses) {
		List<XEventClass> lTrace = new ArrayList<>();
		for (XEvent event : trace) {
			lTrace.add(performanceEventClasses.getClassOf(event));
		}
		return lTrace;
	}

	/*
	 * Make a log-projection hashmap
	 */
	public static THashMap<List<XEventClass>, AlignedTrace> getIMTrace2AlignedTrace(MultiSet<AlignedTrace> aLog) {
		THashMap<List<XEventClass>, AlignedTrace> result = new THashMap<>();
		for (AlignedTrace aTrace : aLog) {
			List<XEventClass> trace = new ArrayList<>();
			for (Move m : aTrace) {
				if (m.getPerformanceEventClass() != null && !m.isTauStart()) {
					trace.add(m.getPerformanceEventClass());
				}
			}
			result.put(trace, aTrace);
		}
		return result;
	}

	public static Long getTimestamp(XEvent event) {
		Date date = XTimeExtension.instance().extractTimestamp(event);
		if (date != null) {
			return date.getTime();
		}
		return null;
	}

	private static final ThreadLocal<SoftReference<DateFormat>> DATE_FORMAT_0 = new ThreadLocal<SoftReference<DateFormat>>();
	private static final ThreadLocal<SoftReference<DateFormat>> DATE_FORMAT_1 = new ThreadLocal<SoftReference<DateFormat>>();
	private static final ThreadLocal<SoftReference<DateFormat>> DATE_FORMAT_2 = new ThreadLocal<SoftReference<DateFormat>>();
	private static final ThreadLocal<SoftReference<DateFormat>> DATE_FORMAT_3 = new ThreadLocal<SoftReference<DateFormat>>();
	private static final ThreadLocal<SoftReference<DateFormat>> DATE_FORMAT_4 = new ThreadLocal<SoftReference<DateFormat>>();

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

	public static String toString(Long timestamp) {
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
}

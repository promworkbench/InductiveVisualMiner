package org.processmining.plugins.inductiveVisualMiner.attributes;

import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttributable;
import org.processmining.plugins.inductiveminer2.attributes.AttributeUtils;
import org.processmining.plugins.inductiveminer2.attributes.AttributeVirtualTraceDurationAbstract;

public class AttributeVirtualTraceDuration extends AttributeVirtualTraceDurationAbstract {

	@Override
	public String getName() {
		return "trace duration";
	}

	@Override
	public double getDuration(XAttributable x) {
		long durationStart = Long.MAX_VALUE;
		long durationEnd = Long.MIN_VALUE;

		if (x instanceof Iterable<?>) {
			for (Object event : (Iterable<?>) x) {
				if (event instanceof XAttributable) {
					long timestamp = getTimestamp((XAttributable) event);
					if (timestamp != Long.MIN_VALUE) {
						durationStart = Math.min(durationStart, timestamp);
						durationEnd = Math.max(durationEnd, timestamp);
					}
				}
			}
		} else {
			return -Double.MAX_VALUE;
		}

		if (durationStart != Long.MAX_VALUE) {
			return durationEnd - durationStart;
		}
		return -Double.MAX_VALUE;
	}

	private static long getTimestamp(XAttributable x) {
		if (!x.hasAttributes() || !x.getAttributes().containsKey(XTimeExtension.KEY_TIMESTAMP)) {
			return Long.MIN_VALUE;
		}
		return AttributeUtils.parseTimeFast(x.getAttributes().get(XTimeExtension.KEY_TIMESTAMP));
	}
}
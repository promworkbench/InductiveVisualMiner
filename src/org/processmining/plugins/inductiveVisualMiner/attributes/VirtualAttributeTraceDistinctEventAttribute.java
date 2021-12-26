package org.processmining.plugins.inductiveVisualMiner.attributes;

import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.AttributeImpl;
import org.processmining.plugins.inductiveminer2.attributes.AttributeVirtualTraceNumericAbstract;

import gnu.trove.set.TDoubleSet;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TDoubleHashSet;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TLongHashSet;

public class VirtualAttributeTraceDistinctEventAttribute extends AttributeVirtualTraceNumericAbstract {

	private final AttributeImpl attribute;

	public VirtualAttributeTraceDistinctEventAttribute(AttributeImpl attribute) {
		this.attribute = attribute;
	}

	@Override
	public String getName() {
		return "distinct values per trace of " + attribute.getName();
	}

	@Override
	public double getNumeric(XAttributable x) {
		if (attribute.isNumeric()) {
			return getNumericNumeric(x);
		} else if (attribute.isDuration()) {
			return getNumericDuration(x);
		} else if (attribute.isLiteral()) {
			return getNumericLiteral(x);
		} else if (attribute.isTime()) {
			return getNumericTime(x);
		}
		return -Double.MAX_VALUE;
	}

	public double getNumericNumeric(XAttributable x) {
		if (x instanceof IvMTrace) {
			TDoubleSet result = new TDoubleHashSet();
			for (IvMMove move : (IvMTrace) x) {
				double value = attribute.getNumeric(move);
				if (value != -Double.MAX_VALUE) {
					result.add(value);
				}
			}
			return result.size();
		}
		if (x instanceof IMTrace) {
			TDoubleSet result = new TDoubleHashSet();
			for (XEvent event : (IMTrace) x) {
				double value = attribute.getNumeric(event);
				if (value != -Double.MAX_VALUE) {
					result.add(value);
				}
			}
			return result.size();
		}
		return -Double.MAX_VALUE;
	}

	public double getNumericDuration(XAttributable x) {
		if (x instanceof IvMTrace) {
			TLongSet result = new TLongHashSet();
			for (IvMMove move : (IvMTrace) x) {
				long value = attribute.getDuration(move);
				if (value != Long.MIN_VALUE) {
					result.add(value);
				}
			}
			return result.size();
		}
		if (x instanceof IMTrace) {
			TLongSet result = new TLongHashSet();
			for (XEvent event : (IMTrace) x) {
				long value = attribute.getDuration(event);
				if (value != Long.MIN_VALUE) {
					result.add(value);
				}
			}
			return result.size();
		}
		return -Double.MAX_VALUE;
	}

	public double getNumericTime(XAttributable x) {
		if (x instanceof IvMTrace) {
			TLongSet result = new TLongHashSet();
			for (IvMMove move : (IvMTrace) x) {
				long value = attribute.getTime(move);
				if (value != Long.MIN_VALUE) {
					result.add(value);
				}
			}
			return result.size();
		}
		if (x instanceof IMTrace) {
			TLongSet result = new TLongHashSet();
			for (XEvent event : (IMTrace) x) {
				long value = attribute.getTime(event);
				if (value != Long.MIN_VALUE) {
					result.add(value);
				}
			}
			return result.size();
		}
		return -Double.MAX_VALUE;
	}

	public double getNumericLiteral(XAttributable x) {
		if (x instanceof IvMTrace) {
			THashSet<String> result = new THashSet<>();
			for (IvMMove move : (IvMTrace) x) {
				String value = attribute.getLiteral(move);
				if (value != null) {
					result.add(value);
				}
			}
			return result.size();
		}
		if (x instanceof IMTrace) {
			THashSet<String> result = new THashSet<>();
			for (XEvent event : (IMTrace) x) {
				String value = attribute.getLiteral(event);
				if (value != null) {
					result.add(value);
				}
			}
			return result.size();
		}
		return -Double.MAX_VALUE;
	}
}
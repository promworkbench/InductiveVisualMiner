package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.Arrays;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XVisitor;

public class XEventPerformanceClassifier implements XEventClassifier, Comparable<XEventClassifier> {

	private final XEventClassifier activityClassifier;

	public XEventPerformanceClassifier(XEventClassifier activityClassifier) {
		this.activityClassifier = activityClassifier;
	}

	public XEventClassifier getActivityClassifier() {
		return activityClassifier;
	}

	public void accept(XVisitor visitor, XLog log) {
		/*
		 * First call.
		 */
		visitor.visitClassifierPre(this, log);
		/*
		 * Last call.
		 */
		visitor.visitClassifierPost(this, log);
	}

	public String getClassIdentity(XEvent event) {
		return activityClassifier.getClassIdentity(event) + "+" + getLifecycle(event);
	}

	public String getLifecycle(XEvent event) {
		XAttribute attribute = event.getAttributes().get(XLifecycleExtension.KEY_TRANSITION);
		if (attribute != null) {
			return attribute.toString().trim().toLowerCase();
		}
		return "complete";
	}

	public String[] getDefiningAttributeKeys() {
		return new String[] { XLifecycleExtension.KEY_TRANSITION };
	}

	public String name() {
		return "Lifecycle transition case independent";
	}

	public boolean sameEventClass(XEvent eventA, XEvent eventB) {
		return getClassIdentity(eventA).equals(getClassIdentity(eventB));
	}

	public void setName(String arg0) {

	}

	public int compareTo(XEventClassifier other) {
		return other.name().compareTo(name());
	}

	/**
	 * The hashcode and equals methods are used in the caching of alignments.
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(getActivityClassifier().getDefiningAttributeKeys());
	}

	/**
	 * The hashcode and equals methods are used in the caching of alignments.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XEventPerformanceClassifier other = (XEventPerformanceClassifier) obj;
		return Arrays.equals(other.getActivityClassifier().getDefiningAttributeKeys(), this.getActivityClassifier()
				.getDefiningAttributeKeys());
	}
}

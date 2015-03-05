package org.processmining.plugins.inductiveVisualMiner.performance;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XVisitor;

public class XEventPerformanceClassifier implements XEventClassifier, Comparable<XEventClassifier> {
	
	public final XEventClassifier otherClassifier;
	
	public XEventPerformanceClassifier(XEventClassifier otherClassifier) {
		this.otherClassifier = otherClassifier;
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
		return otherClassifier.getClassIdentity(event) + "+" + getLifecycle(event);
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
	
	@Override
	public int hashCode() {
		return 3;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof XEventPerformanceClassifier;
	}

}

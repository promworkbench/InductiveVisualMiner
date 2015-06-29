package org.processmining.plugins.inductiveVisualMiner.animation;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventResourceClassifier;
import org.deckfour.xes.model.XEvent;

public class ResourceFunctions {
	
	private final static XEventClassifier classifier = new XEventResourceClassifier();
	
	public static String getResource(XEvent event) {
		return classifier.getClassIdentity(event);
	}
}

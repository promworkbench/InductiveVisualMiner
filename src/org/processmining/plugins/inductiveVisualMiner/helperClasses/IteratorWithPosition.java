package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.Iterator;

public interface IteratorWithPosition<T> extends Iterator<T> {
	public int getPosition();
}

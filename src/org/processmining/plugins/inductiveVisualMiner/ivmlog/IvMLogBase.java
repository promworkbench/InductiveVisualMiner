package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import java.util.ArrayList;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class IvMLogBase extends ArrayList<IvMTrace> implements IvMLog {

	private static final long serialVersionUID = -7682834325820831659L;

	public IteratorWithPosition<IvMTrace> iterator() {
		return new IteratorWithPosition<IvMTrace>() {
			int i = -1;
			public void remove() {
				throw new NotImplementedException();
			}
			
			public IvMTrace next() {
				i++;
				return get(i);
			}
			
			public boolean hasNext() {
				return i < size() - 1;
			}
			
			public int getPosition() {
				return i;
			}
		};
	}
}
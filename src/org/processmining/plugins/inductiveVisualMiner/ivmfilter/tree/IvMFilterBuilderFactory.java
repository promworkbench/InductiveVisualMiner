package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree;

import java.util.List;

public interface IvMFilterBuilderFactory {
	/**
	 * 
	 * @param <X>
	 * @param clazz
	 * @return a list of -new- filter builders. Filter builders may not be
	 *         reused between calls.
	 */
	public <X> List<IvMFilterBuilder<X, ?, ?>> get(Class<X> clazz);
}
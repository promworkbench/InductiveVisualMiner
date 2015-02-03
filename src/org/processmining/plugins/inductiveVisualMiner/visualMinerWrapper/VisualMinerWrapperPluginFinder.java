package org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.processmining.framework.plugin.PluginContext;

public class VisualMinerWrapperPluginFinder {

	/**
	 * Find all classes with the Visual Miner Wrapper annotation.
	 * 
	 * @param context
	 * @param select
	 *            include this instance instead of creating a new one.
	 * @return
	 */
	public static VisualMinerWrapper[] find(PluginContext context, VisualMinerWrapper select) {
		List<VisualMinerWrapper> miners = new LinkedList<VisualMinerWrapper>();
		miners.add(select);

		Set<Class<?>> coverageEstimatorClasses = context.getPluginManager().getKnownClassesAnnotatedWith(
				VisualMinerWrapperAnnotation.class);
		if (coverageEstimatorClasses != null) {
			for (Class<?> coverClass : coverageEstimatorClasses) {
				try {
					Constructor<?> constructor = coverClass.getConstructor();
					Object xyz = constructor.newInstance();

					if (xyz instanceof VisualMinerWrapper && !select.getClass().equals(coverClass)) {
						miners.add((VisualMinerWrapper) xyz);
					}
				} catch (Exception e) {
					//Catch and ignore all exceptions to be resistant to external faults. 
					//e.printStackTrace();
				}
			}
		}

		Collections.sort(miners);
		VisualMinerWrapper[] array = new VisualMinerWrapper[miners.size()];
		miners.toArray(array);
		return array;
	}
}

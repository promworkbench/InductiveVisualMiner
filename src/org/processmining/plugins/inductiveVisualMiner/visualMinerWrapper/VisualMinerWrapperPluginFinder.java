package org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners.AllOperatorsMiner;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners.LifeCycleMiner;

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
		Set<VisualMinerWrapper> miners = new HashSet<VisualMinerWrapper>();
		miners.add(select);

		/**
		 * TODO: hard-coded for now, such that they show up in the
		 * QuickVisualiser.
		 * 
		 */
		miners.add(new AllOperatorsMiner());
		miners.add(new LifeCycleMiner());
		//miners.add(new DfgMiner());

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

		List<VisualMinerWrapper> miners2 = new ArrayList<>(miners);
		Collections.sort(miners2);
		VisualMinerWrapper[] array = new VisualMinerWrapper[miners2.size()];
		miners2.toArray(array);
		return array;
	}
}

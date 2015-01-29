package org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.processmining.framework.plugin.PluginContext;

public class VisualMinerWrapperPluginFinder {
	
	public static VisualMinerWrapper[] find(PluginContext context) {
		List<VisualMinerWrapper> miners = new LinkedList<VisualMinerWrapper>();

		Set<Class<?>> coverageEstimatorClasses = context.getPluginManager().getKnownClassesAnnotatedWith(
				VisualMinerWrapperAnnotation.class);
		if (coverageEstimatorClasses != null) {
			for (Class<?> coverClass : coverageEstimatorClasses) {
				try {
					Constructor<?> constructor = coverClass.getConstructor();
					Object xyz = constructor.newInstance();

					if (xyz instanceof VisualMinerWrapper) {
						miners.add((VisualMinerWrapper) xyz);
					}
				} catch (Exception e) {
					//Catch and ignore all exceptions to be resistant to external faults. 
					//e.printStackTrace();
				}
			}
		}

		VisualMinerWrapper[] array = new VisualMinerWrapper[miners.size()];
		miners.toArray(array);
		return array;
	}
}

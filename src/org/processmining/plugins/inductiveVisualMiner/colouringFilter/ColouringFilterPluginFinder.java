package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;

public class ColouringFilterPluginFinder {

	public static List<ColouringFilter> getAllMetricInfos(PluginContext context, JComponent parent, XLog xLog,
			Runnable onUpdate) {
		List<ColouringFilter> colouringFilters = new LinkedList<ColouringFilter>();

		Set<Class<?>> coverageEstimatorClasses = context.getPluginManager().getKnownClassesAnnotatedWith(
				ColouringFilterAnnotation.class);
		if (coverageEstimatorClasses != null) {
			for (Class<?> coverClass : coverageEstimatorClasses) {
				try {
					Constructor<?> constructor = coverClass.getConstructor();
					Object xyz = constructor.newInstance();

					if (xyz instanceof ColouringFilter) {
						colouringFilters.add((ColouringFilter) xyz);
						((ColouringFilter) xyz).initialiseFilter(xLog, onUpdate);
					}
				} catch (Exception e) {
					//Catch and ignore all exceptions to be resistant to external faults. 
					//e.printStackTrace();
				}
			}
		}

		return colouringFilters;
	}

}

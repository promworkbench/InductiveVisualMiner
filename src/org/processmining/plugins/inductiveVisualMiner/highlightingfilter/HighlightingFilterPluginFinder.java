package org.processmining.plugins.inductiveVisualMiner.highlightingfilter;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;

public class HighlightingFilterPluginFinder {

	public static List<HighlightingFilter> findFilteringPlugins(PluginContext context, JComponent parent, XLog xLog) {
		List<HighlightingFilter> colouringFilters = new LinkedList<HighlightingFilter>();

		Set<Class<?>> coverageEstimatorClasses = context.getPluginManager().getKnownClassesAnnotatedWith(
				HighlightingFilterAnnotation.class);
		if (coverageEstimatorClasses != null) {
			for (Class<?> coverClass : coverageEstimatorClasses) {
				try {
					Constructor<?> constructor = coverClass.getConstructor();
					Object xyz = constructor.newInstance();

					if (xyz instanceof HighlightingFilter) {
						colouringFilters.add((HighlightingFilter) xyz);
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

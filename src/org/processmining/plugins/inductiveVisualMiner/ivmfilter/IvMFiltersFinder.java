package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterTraceWithEventTwice;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.MultiEventAttributeFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.MultiLogMoveAttributeFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.MultiTraceAttributeFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters.PreMiningFilterEvents;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters.PreMiningFilterTraceWithEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters.PreMiningFilterTraceWithEventTwice;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters.PreMiningFrequentTracesFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters.PreMiningMultiTraceAttributeFilter;

public class IvMFiltersFinder {

	@SafeVarargs
	public static List<IvMFilter> findFilteringPlugins(PluginContext context, Class<? extends IvMFilter> clazz,
			Class<? extends Annotation>... annotations) {
		Set<IvMFilter> colouringFilters = new HashSet<>();

		/**
		 * TODO add filters manually to show them in QuickVisualiser
		 * 
		 */
		maybeAdd(colouringFilters, new HighlightingFilterTraceWithEventTwice(), annotations);
		maybeAdd(colouringFilters, new MultiEventAttributeFilter(), annotations);
		maybeAdd(colouringFilters, new MultiLogMoveAttributeFilter(), annotations);
		maybeAdd(colouringFilters, new MultiTraceAttributeFilter(), annotations);
		maybeAdd(colouringFilters, new PreMiningFilterEvents(), annotations);
		maybeAdd(colouringFilters, new PreMiningFilterTraceWithEvent(), annotations);
		maybeAdd(colouringFilters, new PreMiningFilterTraceWithEventTwice(), annotations);
		maybeAdd(colouringFilters, new PreMiningFrequentTracesFilter(), annotations);
		maybeAdd(colouringFilters, new PreMiningMultiTraceAttributeFilter(), annotations);

		for (Class<? extends Annotation> annotation : annotations) {
			Set<Class<?>> coverageEstimatorClasses = context.getPluginManager()
					.getKnownClassesAnnotatedWith(annotation);
			if (coverageEstimatorClasses != null) {
				for (Class<?> coverClass : coverageEstimatorClasses) {
					try {
						Constructor<?> constructor = coverClass.getConstructor();
						Object xyz = constructor.newInstance();

						if (clazz.isInstance(xyz)) {
							colouringFilters.add((IvMFilter) xyz);
						}
					} catch (Exception e) { //Catch and ignore all exceptions to be resistant to external faults. 
						//e.printStackTrace(); 
					}
				}
			}
		}

		List<IvMFilter> filters2 = new ArrayList<>(colouringFilters);
		Collections.sort(filters2);
		return filters2;
	}

	private static void maybeAdd(Set<IvMFilter> colouringFilters, IvMFilter filter,
			Class<? extends Annotation>... annotations) {
		for (Class<? extends Annotation> annotation : annotations) {
			if (filter.getClass().isAnnotationPresent(annotation)) {
				colouringFilters.add(filter);
			}
		}
	}
}

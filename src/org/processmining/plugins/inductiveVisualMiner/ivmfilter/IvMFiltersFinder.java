package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterCompleteEventTwice;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterEventTwice;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterFollows;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterLogMove;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterTraceEndsWithEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterTraceStartsWithEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters.PreMiningFilterEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters.PreMiningFilterTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters.PreMiningFilterTraceWithEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters.PreMiningFilterTraceWithEventTwice;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters.PreMiningFrequentTracesFilter;

public class IvMFiltersFinder {

	@SafeVarargs
	public static List<IvMFilter> findFilteringPlugins(PluginContext context, Class<? extends IvMFilter> clazz,
			Class<? extends Annotation>... annotations) {
		Set<IvMFilter> colouringFilters = new HashSet<>();

		/**
		 * TODO add filters manually to show them in QuickVisualiser
		 * 
		 */
		maybeAdd(colouringFilters, new HighlightingFilterEventTwice(), annotations);
		maybeAdd(colouringFilters, new HighlightingFilterEvent(), annotations);
		maybeAdd(colouringFilters, new HighlightingFilterLogMove(), annotations);
		maybeAdd(colouringFilters, new HighlightingFilterTrace(), annotations);
		maybeAdd(colouringFilters, new HighlightingFilterCompleteEventTwice(), annotations);
		maybeAdd(colouringFilters, new HighlightingFilterLogMove(), annotations);
		maybeAdd(colouringFilters, new HighlightingFilterFollows(), annotations);
		maybeAdd(colouringFilters, new HighlightingFilterTraceStartsWithEvent(), annotations);
		maybeAdd(colouringFilters, new HighlightingFilterTraceEndsWithEvent(), annotations);
		maybeAdd(colouringFilters, new PreMiningFilterEvent(), annotations);
		maybeAdd(colouringFilters, new PreMiningFilterTraceWithEvent(), annotations);
		maybeAdd(colouringFilters, new PreMiningFilterTraceWithEventTwice(), annotations);
		maybeAdd(colouringFilters, new PreMiningFrequentTracesFilter(), annotations);
		maybeAdd(colouringFilters, new PreMiningFilterTrace(), annotations);

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

package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Triple;

import com.kitfox.svg.animation.Bezier;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * Keep a set of elements. The index is kept.
 * @author sleemans
 *
 * @param <T>
 */
public class BezierList {

	private final TObjectIntHashMap<Triple<Bezier, Double, Double>> hash;
	private final List<Bezier> beziers;
	private final TDoubleArrayList startOpacities;
	private final TDoubleArrayList endOpacities;
	
	public BezierList() {
		hash = new TObjectIntHashMap<>();
		beziers = new ArrayList<>();
		startOpacities = new TDoubleArrayList();
		endOpacities = new TDoubleArrayList();
	}
	
	/**
	 * Adds an object to the list and returns the index at which it was inserted.
	 * @param bezier
	 * @return
	 */
	public int add(Bezier bezier, double startOpacity, double endOpacity) {
		int result = hash.putIfAbsent(Triple.of(bezier, startOpacity, endOpacity), beziers.size());
		if (result == hash.getNoEntryValue()) {
			beziers.add(bezier);
			startOpacities.add(startOpacity);
			endOpacities.add(endOpacity);
			return beziers.size() - 1;
		}
		return result;
	}
	
	public Bezier getBezier(int index) {
		return beziers.get(index);
	}
	
	public double getStartOpacity(int index) {
		return startOpacities.get(index);
	}
	
	public double getEndOpacity(int index) {
		return endOpacities.get(index);
	}
	
	public int getNumberOfBeziers() {
		return beziers.size();
	}
	
	/**
	 * removes temporary storage
	 */
	public void cleanUp() {
		hash.clear();
	}
}

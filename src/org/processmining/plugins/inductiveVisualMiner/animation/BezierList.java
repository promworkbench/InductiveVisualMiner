package org.processmining.plugins.inductiveVisualMiner.animation;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Triple;

import com.kitfox.svg.animation.Bezier;

/**
 * Keep a set of elements. The index is kept.
 * @author sleemans
 *
 * @param <T>
 */
public class BezierList {

	private final TObjectIntHashMap<Triple<Bezier, Double, Double>> hash;
	private final List<Bezier> beziers;
	private final List<AffineTransform> transforms;
	private final List<AffineTransform> transformInverses;
	private final TDoubleArrayList startOpacities;
	private final TDoubleArrayList endOpacities;
	
	public BezierList() {
		hash = new TObjectIntHashMap<>();
		beziers = new ArrayList<>();
		transforms = new ArrayList<>();
		transformInverses = new ArrayList<>();
		startOpacities = new TDoubleArrayList();
		endOpacities = new TDoubleArrayList();
	}
	
	/**
	 * Adds an object to the list and returns the index at which it was inserted.
	 * @param bezier
	 * @return
	 */
	public int add(Bezier bezier, AffineTransform transform, AffineTransform transformInverse, double startOpacity, double endOpacity) {
		int result = hash.putIfAbsent(Triple.of(bezier, startOpacity, endOpacity), beziers.size());
		if (result == hash.getNoEntryValue()) {
			beziers.add(bezier);
			transforms.add(transform);
			transformInverses.add(transformInverse);
			startOpacities.add(startOpacity);
			endOpacities.add(endOpacity);
			return beziers.size() - 1;
		}
		return result;
	}
	
	public Bezier getBezier(int index) {
		return beziers.get(index);
	}
	
	public AffineTransform getTransform(int index) {
		return transforms.get(index);
	}
	
	public AffineTransform getTransformInverse(int index) {
		return transformInverses.get(index);
	}
	
	public double getStartOpacity(int index) {
		return startOpacities.get(index);
	}
	
	public double getEndOpacity(int index) {
		return endOpacities.get(index);
	}
	
	/**
	 * removes temporary storage
	 */
	public void cleanUp() {
		hash.clear();
	}
}

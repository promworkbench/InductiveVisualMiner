package org.processmining.plugins.inductiveVisualMiner.causal.mvpc;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Combinations;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class JavaHelperClasses {
	public static boolean isAnyNan(double[][] data, int column) {
		for (double[] datum : data) {
			if (Double.isNaN(datum[column])) {
				return true;
			}
		}
		return false;
	}

	public static boolean isAnyNan(double[][] data, TIntCollection rows, TIntCollection columns) {
		for (TIntIterator itr = rows.iterator(); itr.hasNext();) {
			int row = itr.next();
			for (TIntIterator itc = columns.iterator(); itc.hasNext();) {
				int column = itc.next();

				if (Double.isNaN(data[row][column])) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isAllNan(double[][] data, int column) {
		for (double[] datum : data) {
			if (!Double.isNaN(datum[column])) {
				return false;
			}
		}
		return true;
	}

	public static int getNumberOfColumns(double[][] data) {
		return data[0].length;
	}

	public static double[][] select_columns(double[][] data, TIntCollection columns) {
		double[][] result = new double[data.length][columns.size()];
		for (int row = 0; row < data.length; row++) {
			int newColumn = 0;
			for (TIntIterator it = columns.iterator(); it.hasNext();) {
				int oldColumn = it.next();
				result[row][newColumn] = data[row][oldColumn];
				newColumn++;
			}
		}
		return result;
	}

	public static double[][] select_rows(double[][] data, TIntCollection rows) {
		double[][] result = new double[rows.size()][];
		int i = 0;
		for (TIntIterator it = rows.iterator(); it.hasNext();) {
			result[i] = data[it.next()];
			i++;
		}
		return result;
	}

	public static int getNumberOfRows(double[][] data) {
		return data.length;
	}

	public static RealMatrix ix(RealMatrix matrix, TIntList rows, TIntList columns) {
		RealMatrix result = new Array2DRowRealMatrix(rows.size(), columns.size());

		int newRow = 0;
		for (TIntIterator itRow = rows.iterator(); itRow.hasNext();) {
			int oldRow = itRow.next();

			int newColumn = 0;
			for (TIntIterator itColumn = columns.iterator(); itColumn.hasNext();) {
				int oldColumn = itColumn.next();

				result.setEntry(newRow, newColumn, matrix.getEntry(oldRow, oldColumn));

				newColumn++;
			}

			newRow++;
		}

		//self.correlation_matrix[np.ix_(var, var)]
		return result;
	}

	public static Iterable<TIntList> combinations(TIntList array, int depth) {
		return new Iterable<TIntList>() {
			public Iterator<TIntList> iterator() {
				return new Iterator<TIntList>() {

					Iterator<int[]> it = new Combinations(array.size(), depth).iterator();

					public TIntList next() {
						int[] indices = it.next();
						TIntList result = new TIntArrayList();
						for (int index : indices) {
							result.add(array.get(index));
						}
						return result;
					}

					public boolean hasNext() {
						return it.hasNext();
					}
				};
			}
		};
	}

	public static double[][] copy(double[][] data) {
		double[][] result = new double[data.length][data[0].length];
		for (int i = 0; i < data.length; i++) {
			System.arraycopy(data[i], 0, result[i], 0, result[i].length);
		}
		return result;
	}

	public static void removeAll(TIntList list, int y) {
		for (TIntIterator itn = list.iterator(); itn.hasNext();) {
			if (itn.next() == y) {
				itn.remove();
			}
		}
	}

	public static Iterable<int[][]> permutationPairs(List<Pair<Integer, Integer>> list) {

		return new Iterable<int[][]>() {
			public Iterator<int[][]> iterator() {
				return new Iterator<int[][]>() {

					Iterator<Pair<Integer, Integer>> itIndex = permutationsPairs(list.size()).iterator();

					public int[][] next() {
						Pair<Integer, Integer> indices = itIndex.next();

						Integer indexA = indices.getA();
						Integer indexB = indices.getB();

						Pair<Integer, Integer> pA = list.get(indexA);
						Pair<Integer, Integer> pB = list.get(indexB);

						return new int[][] { { pA.getA(), pA.getB() }, { pB.getA(), pB.getB() } };
					}

					public boolean hasNext() {
						return itIndex.hasNext();
					}
				};
			}
		};
	}

	public static Iterable<int[][]> permutationTriples(List<Triple<Integer, Integer, Integer>> list) {

		return new Iterable<int[][]>() {
			public Iterator<int[][]> iterator() {
				return new Iterator<int[][]>() {

					Iterator<Pair<Integer, Integer>> itIndex = permutationsPairs(list.size()).iterator();

					public int[][] next() {
						Pair<Integer, Integer> indices = itIndex.next();

						Integer indexA = indices.getA();
						Integer indexB = indices.getB();

						Triple<Integer, Integer, Integer> pA = list.get(indexA);
						Triple<Integer, Integer, Integer> pB = list.get(indexB);

						return new int[][] { { pA.getA(), pA.getB(), pA.getC() }, { pB.getA(), pB.getB(), pB.getC() } };
					}

					public boolean hasNext() {
						return itIndex.hasNext();
					}
				};
			}
		};
	}

	public static Iterable<Pair<Integer, Integer>> permutationsPairs(int no_of_var) {
		//list(permutations(node_ids, 2));

		assert no_of_var >= 2;
		return new Iterable<Pair<Integer, Integer>>() {
			public Iterator<Pair<Integer, Integer>> iterator() {
				return new Iterator<Pair<Integer, Integer>>() {

					int nextA = 0;
					int nextB = 1;

					public Pair<Integer, Integer> next() {
						Pair<Integer, Integer> result = Pair.of(nextA, nextB);

						setNext();

						return result;
					}

					private void setNext() {
						do {
							nextB++;
							if (nextB == no_of_var) {
								nextB = 0;
								nextA++;
							}
						} while (nextA == nextB);
					}

					public boolean hasNext() {
						return nextA < no_of_var;
					}
				};
			}
		};
	}

	public static TIntList range(int length) {
		TIntList result = new TIntArrayList();
		for (int i = 0; i < length; i++) {
			result.add(i);
		}
		return result;
	}
}
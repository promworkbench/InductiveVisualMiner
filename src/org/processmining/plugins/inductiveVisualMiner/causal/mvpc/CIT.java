package org.processmining.plugins.inductiveVisualMiner.causal.mvpc;

import java.util.BitSet;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.processmining.plugins.InductiveMiner.Triple;

import gnu.trove.TIntCollection;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class CIT implements Cloneable {

	public IndependenceTest method;
	public Map<Triple<Integer, Integer, TIntSet>, Double> pvalue_cache;

	public double[][] data;
	private int sample_size;
	private int num_features;
	private RealMatrix correlation_matrix;

	//	 def __init__(self, data, method='fisherz', **kwargs):
	//	        '''
	//	        Parameters
	//	        ----------
	//	        data: numpy.ndarray of shape (n_samples, n_features)
	//	        method: str, in ["fisherz", "mv_fisherz", "mc_fisherz", "kci", "chisq", "gsq"]
	//	        kwargs: placeholder for future arguments, or for KCI specific arguments now
	//	        '''
	//	        self.data = data
	//	        self.data_hash = hash(str(data))
	//	        self.sample_size, self.num_features = data.shape
	//	        self.method = method
	//	        self.pvalue_cache = {}
	//
	//	        if method == 'kci':
	//	            # parse kwargs contained in the KCI method
	//	            kci_kwargs = {k: v for k, v in kwargs.items() if k in
	//	                          ['kernelX', 'kernelY', 'null_ss', 'approx', 'est_width', 'polyd', 'kwidthx', 'kwidthy']}
	//	            self.kci_ui = KCI_UInd(**kci_kwargs)
	//	            self.kci_ci = KCI_CInd(**kci_kwargs)
	//	        elif method in ['fisherz', 'mv_fisherz', 'mc_fisherz']:
	//	            self.correlation_matrix = np.corrcoef(data.T)
	//	        elif method in ['chisq', 'gsq']:
	//	            def _unique(column):
	//	                return np.unique(column, return_inverse=True)[1]
	//	            self.data = np.apply_along_axis(_unique, 0, self.data).astype(np.int64)
	//	            self.data_hash = hash(str(self.data))
	//	            self.cardinalities = np.max(self.data, axis=0) + 1
	//	        else:
	//	            raise NotImplementedError(f"CITest method {method} is not implemented.")
	//
	//	        self.named_caller = {
	//	            'fisherz': self.fisherz,
	//	            'mv_fisherz': self.mv_fisherz,
	//	            'mc_fisherz': self.mc_fisherz,
	//	            'kci': self.kci,
	//	            'chisq': self.chisq,
	//	            'gsq': self.gsq
	//	        }
	public CIT(double[][] data, IndependenceTest method, String... kwargs) {

		this.data = data;
		this.sample_size = JavaHelperClasses.getNumberOfRows(data);
		this.num_features = JavaHelperClasses.getNumberOfColumns(data);
		this.method = method;
		this.pvalue_cache = new THashMap<>();

		//		        if method == 'kci':
		//		            # parse kwargs contained in the KCI method
		//		            kci_kwargs = {k: v for k, v in kwargs.items() if k in
		//		                          ['kernelX', 'kernelY', 'null_ss', 'approx', 'est_width', 'polyd', 'kwidthx', 'kwidthy']}
		//		            self.kci_ui = KCI_UInd(**kci_kwargs)
		//		            self.kci_ci = KCI_CInd(**kci_kwargs)
		//		        elif method in ['fisherz', 'mv_fisherz', 'mc_fisherz']:
		this.correlation_matrix = new PearsonsCorrelation().computeCorrelationMatrix(data);
		//		        elif method in ['chisq', 'gsq']:
		//		            def _unique(column):
		//		                return np.unique(column, return_inverse=True)[1]
		//		            self.data = np.apply_along_axis(_unique, 0, self.data).astype(np.int64)
		//		            self.data_hash = hash(str(self.data))
		//		            self.cardinalities = np.max(self.data, axis=0) + 1
		//		        else:
		//		            raise NotImplementedError(f"CITest method {method} is not implemented.")
		//
		//		        self.named_caller = {
		//		            'fisherz': self.fisherz,
		//		            'mv_fisherz': self.mv_fisherz,
		//		            'mc_fisherz': self.mc_fisherz,
		//		            'kci': self.kci,
		//		            'chisq': self.chisq,
		//		            'gsq': self.gsq
		//		        }
	}

	public double call(int X, int Y, Object... args) {
		return call(X, Y, null, args);
	}

	//    def __call__(self, X, Y, condition_set=None, *args):
	//        if self.method != 'mc_fisherz':
	//            assert len(args) == 0, "Arguments more than X, Y, and condition_set are provided."
	//        else:
	//            assert len(args) == 2, "Arguments other than skel and prt_m are provided for mc_fisherz."
	//        if condition_set is None: condition_set = tuple()
	//        assert X not in condition_set and Y not in condition_set, "X, Y cannot be in condition_set."
	//        i, j = (X, Y) if (X < Y) else (Y, X)
	//        cache_key = (i, j, frozenset(condition_set))
	//
	//        if self.method != 'mc_fisherz' and cache_key in self.pvalue_cache: return self.pvalue_cache[cache_key]
	//        pValue = self.named_caller[self.method](X, Y, condition_set) if self.method != 'mc_fisherz' else \
	//                 self.mc_fisherz(X, Y, condition_set, *args)
	//        self.pvalue_cache[cache_key] = pValue
	//        return pValue
	public double call(int X, int Y, TIntCollection condition_set, Object... args) {
		if (method != IndependenceTest.mc_fisherz) {
			assert args.length == 0 : "Arguments more than X, Y, and condition_set are provided.";
		} else {
			assert args.length == 2 : "Arguments other than skel and prt_m are provided for mc_fisherz.";
		}
		if (condition_set == null) {
			condition_set = new TIntHashSet();
		}
		assert !condition_set.contains(X) && !condition_set.contains(Y) : "X, Y cannot be in condition_set.";

		int i;
		int j;
		if (X < Y) {
			i = X;
			j = Y;
		} else {
			i = Y;
			j = X;
		}
		Triple<Integer, Integer, TIntSet> cache_key = Triple.of(i, j, new TIntHashSet(condition_set));

		if (method != IndependenceTest.mc_fisherz && pvalue_cache.containsKey(cache_key)) {
			return pvalue_cache.get(cache_key);
		}

		double pValue;
		switch (method) {
			case fisherz :
				pValue = fisherz(X, Y, condition_set);
				break;
			case mv_fisherz :
				pValue = mv_fisherz(X, Y, condition_set);
				break;
			default :
				throw new RuntimeException("not implemented");
		}

		pvalue_cache.put(cache_key, pValue);
		return pValue;
	}

	//	def fisherz(self, X, Y, condition_set):
	//        """
	//        Perform an independence test using Fisher-Z's test
	//
	//        Parameters
	//        ----------
	//        data : data matrices
	//        X, Y and condition_set : column indices of data
	//
	//        Returns
	//        -------
	//        p : the p-value of the test
	//        """
	//        var = list((X, Y) + condition_set)
	//        sub_corr_matrix = self.correlation_matrix[np.ix_(var, var)]
	//        inv = np.linalg.inv(sub_corr_matrix)
	//        r = -inv[0, 1] / sqrt(inv[0, 0] * inv[1, 1])
	//        Z = 0.5 * log((1 + r) / (1 - r))
	//        X = sqrt(self.sample_size - len(condition_set) - 3) * abs(Z)
	//        p = 2 * (1 - norm.cdf(abs(X)))
	//        return p
	public double fisherz(int X, int Y, TIntCollection condition_set) {
		TIntList var = new TIntArrayList(condition_set);
		var.add(X);
		var.add(Y);

		RealMatrix sub_corr_matrix = JavaHelperClasses.ix(correlation_matrix, var, var);
		RealMatrix inv = new LUDecomposition(sub_corr_matrix).getSolver().getInverse();

		double r = -inv.getEntry(0, 1) / Math.sqrt(inv.getEntry(0, 0) * inv.getEntry(1, 1));
		double Z = 0.5 * Math.log((1 + r) / (1 - r));
		double Xx = Math.sqrt(sample_size - condition_set.size() - 3) * Math.abs(Z);
		double p = 2 * (1 - new NormalDistribution().cumulativeProbability(Math.abs(Xx)));
		return p;
	}

	//	def mv_fisherz(self, X, Y, condition_set):
	//        """
	//        Perform an independence test using Fisher-Z's test for data with missing values
	//
	//        Parameters
	//        ----------
	//        mvdata : data with missing values
	//        X, Y and condition_set : column indices of data
	//
	//        Returns
	//        -------
	//        p : the p-value of the test
	//        """
	//        def _get_index_no_mv_rows(mvdata):
	//            nrow, ncol = np.shape(mvdata)
	//            bindxRows = np.ones((nrow,), dtype=bool)
	//            indxRows = np.array(list(range(nrow)))
	//            for i in range(ncol):
	//                bindxRows = np.logical_and(bindxRows, ~np.isnan(mvdata[:, i]))
	//            indxRows = indxRows[bindxRows]
	//            return indxRows
	//        var = list((X, Y) + condition_set)
	//        test_wise_deletion_XYcond_rows_index = _get_index_no_mv_rows(self.data[:, var])
	//        assert len(test_wise_deletion_XYcond_rows_index) != 0, \
	//            "A test-wise deletion fisher-z test appears no overlapping data of involved variables. Please check the input data."
	//        test_wise_deleted_cit = CIT(self.data[test_wise_deletion_XYcond_rows_index], "fisherz")
	//        assert not np.isnan(self.data[test_wise_deletion_XYcond_rows_index][:, var]).any()
	//        return test_wise_deleted_cit(X, Y, condition_set)
	//        # TODO: above is to be consistent with the original code; though below is more accurate (np.corrcoef issues)
	//        # test_wise_deleted_data_var = self.data[test_wise_deletion_XYcond_rows_index][:, var]
	//        # sub_corr_matrix = np.corrcoef(test_wise_deleted_data_var.T)
	//        # inv = np.linalg.inv(sub_corr_matrix)
	//        # r = -inv[0, 1] / sqrt(inv[0, 0] * inv[1, 1])
	//        # Z = 0.5 * log((1 + r) / (1 - r))
	//        # X = sqrt(self.sample_size - len(condition_set) - 3) * abs(Z)
	//        # p = 2 * (1 - norm.cdf(abs(X)))
	//        # return p
	public double mv_fisherz(int X, int Y, TIntCollection condition_set) {
		TIntList var = new TIntArrayList(condition_set);
		var.add(X);
		var.add(Y);

		TIntCollection test_wise_deletion_XYcond_rows_index = mv_fisherz_get_index_no_mv_rows(
				JavaHelperClasses.select_columns(data, var));
		assert !test_wise_deletion_XYcond_rows_index
				.isEmpty() : "A test-wise deletion fisher-z test appears no overlapping data of involved variables. Please check the input data.";
		CIT test_wise_deleted_cit = new CIT(JavaHelperClasses.select_rows(data, test_wise_deletion_XYcond_rows_index),
				IndependenceTest.fisherz);
		assert JavaHelperClasses.isAnyNan(data, test_wise_deletion_XYcond_rows_index, var);
		return test_wise_deleted_cit.call(X, Y, condition_set);
		//        # TODO: above is to be consistent with the original code; though below is more accurate (np.corrcoef issues)
		//        # test_wise_deleted_data_var = self.data[test_wise_deletion_XYcond_rows_index][:, var]
		//        # sub_corr_matrix = np.corrcoef(test_wise_deleted_data_var.T)
		//        # inv = np.linalg.inv(sub_corr_matrix)
		//        # r = -inv[0, 1] / sqrt(inv[0, 0] * inv[1, 1])
		//        # Z = 0.5 * log((1 + r) / (1 - r))
		//        # X = sqrt(self.sample_size - len(condition_set) - 3) * abs(Z)
		//        # p = 2 * (1 - norm.cdf(abs(X)))
		//        # return p
	}

	//	def _get_index_no_mv_rows(mvdata):
	//        nrow, ncol = np.shape(mvdata)
	//        bindxRows = np.ones((nrow,), dtype=bool)
	//        indxRows = np.array(list(range(nrow)))
	//        for i in range(ncol):
	//            bindxRows = np.logical_and(bindxRows, ~np.isnan(mvdata[:, i]))
	//        indxRows = indxRows[bindxRows]
	//        return indxRows
	public TIntCollection mv_fisherz_get_index_no_mv_rows(double[][] mvdata) {
		int nrow = JavaHelperClasses.getNumberOfRows(mvdata);
		int ncol = JavaHelperClasses.getNumberOfColumns(mvdata);

		BitSet bindxRows = new BitSet();
		bindxRows.set(0, nrow);

		for (int col = 0; col < ncol; col++) {
			for (int row = 0; row < nrow; row++) {
				bindxRows.set(row, bindxRows.get(row) && !Double.isNaN(mvdata[row][col]));
			}
		}

		TIntCollection indxRows = new TIntArrayList();
		for (int row = bindxRows.nextSetBit(0); row >= 0; row = bindxRows.nextSetBit(row + 1)) {

			indxRows.add(row);

			if (row == Integer.MAX_VALUE) {
				break; // or (i+1) would overflow
			}
		}

		return indxRows;
	}

	public CIT clone() {
		CIT r = null;
		try {
			r = (CIT) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		r.method = method;
		r.pvalue_cache = new THashMap<>(pvalue_cache);
		r.data = new double[data.length][data[0].length];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				r.data[i][j] = data[i][j];
			}
		}
		r.sample_size = sample_size;
		r.num_features = num_features;
		r.correlation_matrix = new Array2DRowRealMatrix(correlation_matrix.getData());

		return r;
	}
}
package uk.ac.soton.ecs.gp4j.util;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;

import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.CovarianceFunction;
import Jama.CholeskyDecomposition;
import Jama.Matrix;

public class GaussianProcessUtils {
	public static Matrix drawSample(CovarianceFunction function,
			double[] logHyper, Matrix testX, RandomData data) {
		Matrix covarianceMatrix = function.calculateCovarianceMatrix(logHyper,
				testX);

		return drawSample(covarianceMatrix, testX, data);
	}

	public static Matrix drawSample(CovarianceFunction function,
			double[] logHyper, Matrix testX) {
		return drawSample(function, logHyper, testX, new RandomDataImpl());
	}

	public static Matrix drawSample(Matrix covarianceMatrix, Matrix testX,
			RandomData data) {
		Matrix randomVector = new Matrix(testX.getRowDimension(), 1);

		for (int i = 0; i < randomVector.getRowDimension(); i++) {
			randomVector.set(i, 0, data.nextGaussian(0, 1));
		}

		CholeskyDecomposition chol = covarianceMatrix.chol();

		return chol.getL().times(randomVector);
	}

	public static Matrix drawSample(Matrix covarianceMatrix, Matrix testX) {
		return drawSample(covarianceMatrix, testX, new RandomDataImpl());
	}
}

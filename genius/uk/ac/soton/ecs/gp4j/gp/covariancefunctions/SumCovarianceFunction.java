package uk.ac.soton.ecs.gp4j.gp.covariancefunctions;

import Jama.Matrix;

public class SumCovarianceFunction extends
		CommutativeCompositeCovarianceFunction {

	public SumCovarianceFunction(CovarianceFunction... functions) {
		super(functions);
	}

	@Override
	protected Matrix operation(Matrix result, Matrix matrix) {
		return result.plus(matrix);
	}

}

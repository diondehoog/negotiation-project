package uk.ac.soton.ecs.gp4j.bmc;

import uk.ac.soton.ecs.gp4j.gp.GaussianProcessPrediction;
import Jama.Matrix;

public class GaussianProcessMixturePrediction extends GaussianProcessPrediction {

	public GaussianProcessMixturePrediction(Matrix testX, Matrix mean,
			Matrix variance) {
		super(testX, mean, variance);
	}
}

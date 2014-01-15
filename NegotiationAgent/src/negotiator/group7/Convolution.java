package negotiator.group7;

public class Convolution {
	
	/**
	 * Applies convolution to the whole array and returns the chosen part. The convolution
	 * repeats the boundary values in order to get a same-length result.
	 * @param input: input array to which to apply the convolution
	 * @param k: Convolution filter
	 * @param type: use "valid", "same" (it uses "same" by default).
	 * @return The input with convolution applied.
	 */
	public static double[] apply(double[] input, double[] k, String type) throws Exception{
		int hfs = (k.length - 1)/2;
		
		int minI = 0;
		int maxI = input.length -1;
		if (type == "valid")
		{
			minI = hfs;
			maxI = input.length - 1 - hfs;
		}
		if (minI > maxI)
			throw new Exception("Unable to apply convolution: unput too short");
		double[] output = new double[maxI - minI + 1];
		for (int i = minI; i <= maxI; i++)
		{
			output[i - minI] = Convolution.apply(input, i, k);
		}
		return output;
	}
	
	/**
	 * Applies convolution kernel k to input array at position x
	 * 
	 * input = 	Double array containing values to be smoothed
	 * x = 		Location where to apply smooth
	 * k = 		Kernel
	 *
	 * @return
	 */
	public static double apply(double[] input, int x, double[] k) {
		
		// Build double array with end values repeated
		int hfs = (k.length - 1)/2;
		double[] toConvolve = new double[input.length+(2*hfs)];
		toConvolve[0] = input[0];
		for (int i = 0; i < toConvolve.length; i++) {
			if (i < hfs) 
				toConvolve[i] = input[0];
			if (i >= hfs && i <= toConvolve.length- hfs - 1)
				toConvolve[i] = input[i - hfs];
			if (i > toConvolve.length- hfs - 1)
				toConvolve[i] = input[input.length - 1];
		}
	
		double output = 0;	

		for (int i = 0; i < k.length; i++) {
			output = output + (toConvolve[x+i]*k[i]);
		}
		
		return output;
	}

}

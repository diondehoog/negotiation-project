package negotiator.group7;

import java.util.Iterator;
import java.util.List;

import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;

public class OpponentTypeEstimator {
	public static final int filterSize = 10;
	public static final double concedingnessThreshold = -0.01;
	public static final double titLikelihoodThreshold = 0.8;
	
	/**
	 * Returns the estimated opponent type, based on baseOnXBids bids. 
	 * @param negSession NegotiationSession to use
	 * @param om OpponentModel used to estimate opponents utilities
	 * @param baseOnXBids Number of bids to base the estimate on
	 * @return The estimated opponent type
	 */
	public static OpponentType EstimateType(NegotiationSession negSession, OpponentModel om, int baseOnXBids)
	{
		OurAndEnemyUtils oaeu = new OurAndEnemyUtils(negSession, om, baseOnXBids);
		double[] oppHUtils = oaeu.getEnemyUtils();
		double[] ownHUtils = oaeu.getOurUtils();
		
		double tit = titLikelihood(oppHUtils, ownHUtils);
		Log.dln("titLikelihood = " + tit);
		if (tit > titLikelihoodThreshold) 
			return OpponentType.TitForTat;
		else {
			double conced = Concedingness(oppHUtils);
			Log.dln("Concedingness = " + conced);
			if (conced < concedingnessThreshold)
				return OpponentType.Conceder;
			else
				return OpponentType.HardHeaded;
		}
		
	}
	
	/**
	 * Estimates the concedingness of an opponent
	 * @param negSession negotiationSession to base the concedingness on.
	 * @param om Opponent model used to estimate the opponents utilities and therefore the concedingness.
	 * @param baseOnXBids Number of bids to base the concedingness.
	 * @return The estimated conedingness of the opponent
	 */
	public static double Concedingness(NegotiationSession negSession, OpponentModel om, int baseOnXBids)
	{
		OurAndEnemyUtils oaeu = new OurAndEnemyUtils(negSession, om, baseOnXBids);
		double[] oppHUtils = oaeu.getEnemyUtils();
		return Concedingness(oppHUtils);		
	}
	
	/**
	 * Estimates the likelihood that our enemy uses tit-for-that by checking for each bid if
	 *  we conceded that he conceded as well and visa versa. 
	 * @param enemyUtils: Approximated utils of the last x enemy bids.
	 * @param ourUtils: Undiscounted utils of our last bids.
	 */
	private static double titLikelihood(double[] enemyUtils, double[] ourUtils) {
		double s = 1.0/((double)enemyUtils.length - 1);
		double sum = 0.0;
		for (int i = 0; i < enemyUtils.length-1; i++) {
			double difEnemy = enemyUtils[i] - enemyUtils[i + 1];
			double difOwn = ourUtils[i] - ourUtils[i + 1];
			if ((difOwn < 0 && difEnemy > 0) || (difOwn > 0 && difEnemy < 0))
				// Only if he succeeded after us, or he didn't concede after us, then it's a conceder.
				sum += s;
		}
		return sum;
	}
	
	private static double Concedingness(double[] enemyUtils) {
		// Create average filter kernel
		double[] k = new double[filterSize];
		double x = 1.0/filterSize;
		for (int i = 0; i < filterSize; i++) {
			k[i] = x;
		}
		try {
			double[] output = Convolution.apply(enemyUtils, k, "valid");
			double differencesSum = 0.0; 
			//Log.d("average enemyUtils: " + output[0]);
			for (int i = output.length - 1; i > 0; i--) {
				differencesSum += output[i] - output[i - 1];
				//Log.d(", " + output[i]);
			}
			Log.dln("");
			return differencesSum / ((double)output.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return 0;
	}
	

	
	private static class OurAndEnemyUtils {
		private double[] enemyUtils;
		private double[] ourUtils;
		
		public OurAndEnemyUtils(NegotiationSession negSession, OpponentModel om, int baseOnXBids)	{
			List<BidDetails> oppH = negSession.getOpponentBidHistory().sortToTime().getHistory();
			List<BidDetails> ownH = negSession.getOwnBidHistory().sortToTime().getHistory();
			int n = Math.min(Math.min(baseOnXBids, oppH.size()), ownH.size());
			
			this.enemyUtils = new double[n];
			this.ourUtils = new double[n];
			Iterator<BidDetails> oppHI = oppH.iterator();
			Iterator<BidDetails> ownHI = ownH.iterator();
			int i = 0;
			while (i < n && oppHI.hasNext() && ownHI.hasNext())
			{
				this.enemyUtils[i] = om.getBidEvaluation(oppHI.next().getBid());
				this.ourUtils[i] = ownHI.next().getMyUndiscountedUtil();
				i++;
			}
		}
		
		private double[] getEnemyUtils() {
			return this.enemyUtils;
		}
		
		private double[] getOurUtils() {
			return this.ourUtils;
		}
	}
}

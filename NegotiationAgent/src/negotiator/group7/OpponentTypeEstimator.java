package negotiator.group7;

import java.util.Iterator;
import java.util.List;

import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;

public class OpponentTypeEstimator {
	
	public static OpponentType EstimateType(NegotiationSession negSession, OpponentModel om, int baseOnXBids)
	{
		OurAndEnemyUtils oaeu = new OurAndEnemyUtils(negSession, om, baseOnXBids);
		double[] oppHUtils = oaeu.getEnemyUtils();
		double[] ownHUtils = oaeu.getOurUtils();
		
		double tit = titLikelihood(oppHUtils, ownHUtils);
		Log.dln("titLikelihood = " + tit);
		if (tit > 0.8) 
			return OpponentType.TitForTat;
		else {
			double conced = Concedingness(oppHUtils);
			if (conced < 0.1)
				return OpponentType.Conceder;
			else
				return OpponentType.HardHeaded;
		}
		
	}
	
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
		//TODO: Use Average filter and then guess the derivative
		return 0;
	}
	

	
	private static class OurAndEnemyUtils {
		private double[] enemyUtils;
		private double[] ourUtils;
		
		public OurAndEnemyUtils(NegotiationSession negSession, OpponentModel om, int baseOnXBids)	{
			List<BidDetails> oppH = negSession.getOpponentBidHistory().sortToTime().getHistory();
			List<BidDetails> ownH = negSession.getOwnBidHistory().sortToTime().getHistory();
			int n = Math.min(Math.min(baseOnXBids, oppH.size()), ownH.size());
			
			double[] oppHUtils = new double[n];
			double[] ownHUtils = new double[n];
			Iterator<BidDetails> oppHI = oppH.iterator();
			Iterator<BidDetails> ownHI = ownH.iterator();
			int i = 0;
			while (i < n && oppHI.hasNext() && ownHI.hasNext())
			{
				oppHUtils[i] = om.getBidEvaluation(oppHI.next().getBid());
				ownHUtils[i] = ownHI.next().getMyUndiscountedUtil();
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

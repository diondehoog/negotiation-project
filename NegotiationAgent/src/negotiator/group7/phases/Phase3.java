package negotiator.group7.phases;

import java.util.List;
import java.util.Random;

import misc.Range;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;
import negotiator.group7.Helper;
import negotiator.group7.Log;

public class Phase3 extends Phase {

	private double phaseStart = 0.95;
	private Helper ourHelper;
	public Phase3(NegotiationSession negSession, OpponentModel opponentModel, double phaseStart, double phaseEnd) {
		super(negSession, opponentModel, phaseStart, phaseEnd);
		ourHelper = Helper.get(negotiationSession);
	}

	@Override
	public BidDetails determineNextBid() {
		
		int opponentStrategy = ourHelper.getOMStrategy().getOpponentModel();

		Random randgen = new Random();
		BidDetails fallback;
		List<BidDetails> bh = negotiationSession.getOwnBidHistory().getHistory();
		fallback = bh.get(randgen.nextInt(bh.size()));
		
		if (opponentStrategy == 1) // Opponent is assumed to be HardHeaded
		{
			
			Log.rln("Opponent is assumed to be HardHeaded, decreasingly offering random bid that approaches the pareto");

			BidDetails best = null;
			double time = (negotiationSession.getTime() - phaseStart) * (1 / (1 - phaseStart));
			int tries = 15;
			
			while (best == null && tries > 0) 
			{
				double u = randgen.nextDouble() * 0.1 + 0.7 + 0.2 * (1 - time);
				Range r = new Range(u - 0.01, u + 0.01);
				List<BidDetails> randBid = negotiationSession.getOutcomeSpace().getBidsinRange(r);
				
				double bestValue = 0.0;
				for (BidDetails b : randBid) 
				{
					double value = ourHelper.getOpponentModel().getBidEvaluation(b.getBid());
					if (value > bestValue) 
					{
						best = b;
						bestValue = value;
					}
				}
				tries--;
			}
			
			if (best == null)
				return fallback;
			else
				return best;
		} 
		else 
		{
			// Opponent is assumed to be Conceder
			Log.rln("Opponent is assumed to be Conceder, offering KS point");
			if (ourHelper.getKalaiPoint() != null)
				return ourHelper.getKalaiPoint();
			else
				return fallback;
		}
		
	}
}
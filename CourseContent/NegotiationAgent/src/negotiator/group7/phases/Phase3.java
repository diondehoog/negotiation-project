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
		this.phaseStart = phaseStart; 
	}

	@Override
	public BidDetails determineNextBid() {
		
		/** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		  * ~~~~~~~~~ INITIALIZE ~~~~~~~~~~~~~~
		  * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
		
		// get the opponent stategy model so that we can ask for the opponent's strategy
		int opponentStrategy = 1;
		if (ourHelper.getOMStrategy() != null)
			opponentStrategy = ourHelper.getOMStrategy().getOpponentModel();

		// random generator
		Random randgen = new Random(); 
		// a fallback bid, if nothing better was found
		BidDetails fallback; 
		// the best bid found
		BidDetails best = null;
		// normalize the time that is spent within this phase so time goes from 0 to 1
		double time = (negotiationSession.getTime() - phaseStart) * (1 / (1 - phaseStart));
		// maximum amount of tries
		int tries = 15;
		// get history of our bids in order to find the fallback
		List<BidDetails> bh = negotiationSession.getOwnBidHistory().getHistory();
		// get a random bid out of our history as a fallback
		fallback = bh.get(randgen.nextInt(bh.size()));

		/** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		  * ~~~~~~~~~ ALGORITHM ~~~~~~~~~~~~~~~
		  * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
		
		// run while no bid was found yet and as long as we are still allowed to try
		while (best == null && tries > 0) 
		{
			// the utility to search a nice (close to pareto) bid for
			double u;
			// as long as there are still enough bids to make do this
			if (ourHelper.getBidsLeft() == null || ourHelper.getBidsLeft() > 3) {
				// find a utility never higher than 0.7, add a time-dependant conceding value of 0.2, and finally add random(0.1)
				// thus max is 1.0 at t = 0, and 0.8 at t = 1
				//      min is 0.9 at t = 0, and 0.7 at t = 1
				// therefore, we choose randomly within a range of size 0.1, but concede to 0.7
				u = randgen.nextDouble() * 0.1 + 0.7 + 0.2 * (1 - time);
			} 
			// if only a few bids are left do this
			else {
				// we need to panic, so we concede super fast by choosing a value randomly between
				// 0.5 and 0.7. TODO: base upon opponent's best bid so that we never end up below that.
				Log.newLine("\n Panic!! Conceding faster..\n");
				u = randgen.nextDouble() * 0.2 + 0.5;
			}

			if (opponentStrategy != 1 && ourHelper.getKalaiPoint() != null) // Opponent is assumed to be Conceder
			{
				// cap around the kalai
				u = Math.max(ourHelper.getKalaiPoint().getMyUndiscountedUtil(), u);
			}
			
			// build the range with the desired utility
			Range r = new Range(u - 0.01, u + 0.01);
			// get bids in range
			List<BidDetails> randBid = negotiationSession.getOutcomeSpace().getBidsinRange(r);
			
			// if we have an opponent model available, use it
			if (ourHelper.getOpponentModel() != null) {
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
			}
			// no opponent model available
			else {
				// get a random bid from the given bids close around the desired utility
				int size = randBid.size();
				if (size > 0)
					best = randBid.get(randgen.nextInt(size));
				else 
					best = null;
			}
			
			tries--;
		}

		/** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		  * ~~~~~~~~~ END ~~~~~~~~~~~~~~~~~~~~~
		  * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
		
		// if no bid was found, then use the fallback
		if (best == null)
			return fallback;
		// yaay
		else
			return best;
		
	}
}
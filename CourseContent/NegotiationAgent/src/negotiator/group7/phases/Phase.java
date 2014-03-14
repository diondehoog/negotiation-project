package negotiator.group7.phases;

import java.util.List;
import java.util.Random;

import misc.Range;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;

public abstract class Phase {
	protected NegotiationSession negotiationSession;
	protected double phaseStart;
	protected double phaseEnd;
	protected OpponentModel opponentModel;
	
	public Phase(NegotiationSession negSession, OpponentModel opponentModel, double phaseStart, double phaseEnd)
	{
		this.negotiationSession = negSession;
		this.phaseStart = phaseStart;
		this.phaseEnd = phaseEnd;
		this.opponentModel = opponentModel;
	}
	public abstract BidDetails determineNextBid();
	
	public BidDetails getRandomBid (double lb, double ub) {
		Range r = new Range(lb, ub);
		return getRandomBid(r);
	}
	
	/**
	 * Returns a random bid within the range [lb, ub]
	 * 
	 * @param lb
	 * @param ub
	 */
	public BidDetails getRandomBid (Range r) {
		
		List<BidDetails> bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);
		
		int numBids = bidsInRange.size(); // Number of found bids
		BidDetails randBid;
		
		if (numBids > 0) {
			// One or more bids within range are found.
			// Select a random bid and return it.
			Random randgen = new Random();
			randBid = bidsInRange.get(randgen.nextInt(numBids));
			
		} else {
			// No bids within range are found, now we selected the bid that is closest 
			// to the UPPER bound of the given range.
			randBid = negotiationSession.getOutcomeSpace().getBidNearUtility(r.getUpperbound());

		}
		
		return randBid;
		
	}
}

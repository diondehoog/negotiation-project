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
		
//		Log.newLine.out.println("################################################");
//		Log.newLine("Generating random bids within range [" + lb + ", " + ub + "]");

		List<BidDetails> bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);
		
		// Just for testing, print all bids in range
		//for (BidDetails b : bidsInRange) {
		//	Log.newLine("Found bid: " + b.getMyUndiscountedUtil());
		//}
		
		int numBids = bidsInRange.size(); // Number of found bids
		BidDetails randBid;
		
//		Log.newLine("Found " + numBids + " within range.");
		
		if (numBids > 0) {
			// One or more bids within range are found.
			// Select a random bid and return it.
			Random randgen = new Random();
			randBid = bidsInRange.get(randgen.nextInt(numBids));
			
			//Log.newLine("Selected random bid with utility " + randBid.getMyUndiscountedUtil());
			
		} else {
			// No bids within range are found, now we selected the bid that is closest 
			// to the UPPER bound of the given range.
			randBid = negotiationSession.getOutcomeSpace().getBidNearUtility(r.getUpperbound());
			
			//Log.newLine("No bids found, selecting bid closest to upper bound: " + randBid.getMyUndiscountedUtil());
		}
		
		//		Log.newLine("################################################");
		return randBid;
		
	}
}

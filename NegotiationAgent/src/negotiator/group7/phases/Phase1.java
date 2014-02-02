package negotiator.group7.phases;

import misc.Range;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;
import negotiator.group7.Log;


public class Phase1 extends Phase {
	private double phase1LowerBound;
	private double phase1UpperBound;
	
	public Phase1(NegotiationSession negSession, OpponentModel opponentModel, double phaseStart, double phaseEnd, 
				  double phase1LowerBound, double phase1UpperBound) {
		super(negSession, opponentModel, phaseStart, phaseEnd);
		this.phase1LowerBound = phase1LowerBound;
		this.phase1UpperBound = phase1UpperBound;
	}

	@Override
	public BidDetails determineNextBid() {
		// First negotiation phase (implemented by Tom)
		// During the first phase we select random bids.
		double time = negotiationSession.getTime();
		Range randBidRange = getRangeFunctionFirstPhase(time, 0.02);
		
		Log.sln("Phase 1");
		/* Code below checks if offer was already used... 
		BidDetails bd = null;
		boolean foundOffer = false;
		int iterations = 0;
		
		// Iterate until we found offer that was not offered before...
		while (!foundOffer && iterations < 5) {
			bd = getRandomBid(randBidRange);
			Log.newLine("Generated random bid within range: " + bd.getMyUndiscountedUtil());
			
			if (isAlreadyOffered(bd)) {
				// Bid was already offered, generate new one...
				Log.rln("Generating NEW random bid since current was already offered!");
			} else {
				// We found a new available offer :-)
				foundOffer = true;
			}
			
			iterations++;
		} */
		
		BidDetails bd = getRandomBid(randBidRange);
		
		return bd;
	}
	
	public Range getRangeFunctionFirstPhase (double t, double margin) {

		double normTime = t/this.phaseEnd; // Normalized time
		
		double val = 1-(normTime/10); // Center of the utility range
		Range r = new Range(val-margin, val+margin); // Range in which bids are randomly generated
		
		// Set upper bound to 1 if it exceeds
		if (r.getUpperbound() > 1) r.setUpperbound(1.0);
		
		return r;
	}

}

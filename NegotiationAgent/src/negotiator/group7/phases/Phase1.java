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
		
		// Constructor of Phase class
		super(negSession, opponentModel, phaseStart, phaseEnd);
		
		// Set the phase time boundaries
		this.phase1LowerBound = phase1LowerBound;
		this.phase1UpperBound = phase1UpperBound;
	
	}

	@Override
	public BidDetails determineNextBid() {
		
		// During the first phase we select random bids.
		double time = negotiationSession.getTime();

		// Calculate the bid range based on current time
		// The margin for the bid range is set to 0.02
		Range randBidRange = getBidRange(time, 0.02);
		
		// Generate random bid within the range
		BidDetails bd = getRandomBid(randBidRange);
		
		return bd;
	}
	
	public Range getBidRange (double t, double margin) {
		double upperBound = 1.0;
		double lowerBound = 0.9;
		double normTime = t/this.phaseEnd; // Normalized time
		
		// Center of the utility range
		
		//double val = 1-(normTime/10);
		
		// Best bid that the opponent has offered so far
		BidDetails bestOpponent = negotiationSession.getOpponentBidHistory().getBestBidDetails();
		double bestUtilOpp = 0.0;
		if (bestOpponent != null) {
			bestUtilOpp = bestOpponent.getMyUndiscountedUtil();
		}
		
		double utilDiff = 0.1; // minimum utility difference between best opp offer and lower bound
		if (bestUtilOpp + utilDiff > lowerBound) // if opponent offer to close to lowerbound,
			// change lowerbound:
			lowerBound = bestUtilOpp + utilDiff;
		
		// lower bound may be too high, fix:
		if (lowerBound + 0.01 > upperBound) { 
			lowerBound = upperBound - 0.01;
		}
		
		// find the utility value to offer
		double val = upperBound-(upperBound-lowerBound)*normTime;
		
		// Set the bounds for the range
		double lb = val-margin;
		double ub = val+margin;
		/*
		if (bestOpponent.getMyUndiscountedUtil() > val){
			// The opponent has offered a better bid (for us)
			// than the center of our bid range, we counter this
			// by choosing a bid higher (+0.05) than the opponents bid.
			
			
			// Update boundaries
			lb = val; ub = val+margin;
			
			Log.newLine("NOTICE: Updated boundary because opponent has chosen higher bid.");
		}
		*/
		//Log.rln("Center of our bidRange = " + val + ", Best opponents bid = " + bestOpponent.getMyUndiscountedUtil());
		
		// Range in which bids are randomly generated
		Range r = new Range(lb, ub); 
		
		// Set upper bound to 1 if it exceeds upper bound
		if (r.getUpperbound() > 1) r.setUpperbound(1.0);
		
		return r;
	}

}


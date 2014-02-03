package negotiator.group7.phases;

import java.util.ArrayList;
import java.util.List;

import misc.Range;
import negotiator.Bid;
import negotiator.analysis.BidPoint;
import negotiator.analysis.BidSpace;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.group7.Helper;
import negotiator.group7.Log;
import negotiator.utility.UtilitySpace;

public class Phase2 extends Phase {

	/** Initialize variables */
	private double Ppareto = 0.5; // probability of offering pareto
	private int averageOver = 5; // how many bids to average over to determine concession of opponent
	private double niceFactor = 0.33; // when opponent concedes, their concession is multiplied by this
	private double Pconcede = 0.05; // probability of conceding to make opponent happy
	private double concedeFactor = 0.3; // amount of distance to concede to KS
	private int concedeSteps = 10; // concession steps taken after each other
	private int concedeStep = -1; // current concession step
	
	/** Outcome space */
	SortedOutcomeSpace outcomespace;
	UtilitySpace ourUtilitySpace;
	
	/** ArrayLists for saving history */
	private BidSpace bidSpace; 
	
	private ArrayList<Double> distOpponentBidsToKS = new ArrayList<Double>();
	
	private double ourDist = -1.0;
	private double ourMaxDist = -1.0;
	
	private BidPoint ks = null;
	private int it = 0;
	
	public Phase2(NegotiationSession negSession, OpponentModel opponentModel, double phaseStart, double phaseEnd, 
			double Ppareto, int averageOver, double niceFactor, double Pconcede, double concedeFactor, int concedeSteps, 
			double k, double e, double[] phaseBoundary, double phase2LowerBound, double phase2range,
			SortedOutcomeSpace outcomespace) {
		super(negSession, opponentModel, phaseStart, phaseEnd);
		this.outcomespace = outcomespace;
		
		// Set our utility space once
		ourUtilitySpace = negotiationSession.getUtilitySpace();
		
		updateBidSpace();
	}
	
	@Override
	public BidDetails determineNextBid() {
		
		// Update bid space every iteration
		updateBidSpace();
		
		// Last bid of the opponent
		BidDetails lastBid = negotiationSession.getOpponentBidHistory().getLastBidDetails();
		BidPoint lastBidPoint = getBidPointFromBidDetails(lastBid);
		
		// Calculate distance between last opponents bid to estimated KS
		double ksDist = ks.getDistance(lastBidPoint);
		distOpponentBidsToKS.add(ksDist);
		
		double avg = getAvgDifferenceKS(averageOver);
		
		UtilitySpace utilitySpaceOpponent = opponentModel.getOpponentUtilitySpace();
		
		BidPoint myBB = getBestBidPointFromUtilitySpace(ourUtilitySpace);
		Log.vln("my best: " + myBB);
		BidPoint theirBB = getBestBidPointFromUtilitySpace(utilitySpaceOpponent);
		Log.vln("their best: " + theirBB);
		BidPoint ks = getKalaiSmorodisky();

		double theirMaxDist = ks.getDistance(theirBB);
		ourMaxDist = ks.getDistance(myBB);
		
		Log.vln("Our max dist:" + ourMaxDist);
		Log.vln("Their max dist: " + theirMaxDist);
		
		if (ourDist < -0.05) { // if first time ever
			ourDist = (ourMaxDist/theirMaxDist)*ksDist; // match their concession
			Log.vln("Match concession: " + ourDist);
			ourMaxDist = ks.getDistance(myBB); // find our max distance to kalai
		}
		/*
		double deltaDist = 0; 
		double concedestep = 0;
		if ((concedeStep == -1) && (Math.random()<this.Pconcede)) { // sometimes randomly concede
			concedeStep = 1;
		}
		if (concedeStep > 0) { // concede
			concedestep = concedeStep/concedeSteps*concedeFactor;
			concedeStep++;
			if (concedeStep >= this.concedeSteps) {
				concedeStep = -1;
				for (int i = 1; i < 10; i++) { // walk back after conceding
					deltaDist += i/concedeSteps*concedeFactor*(ourMaxDist/theirMaxDist);
				}
			}
		}
		*/
		if (Double.isNaN(avg)) {
			Log.vln("NAN!!!");
		}
		
		double concedestep = 0;
		double deltaDist = 0;
		// play tit for tat and concede sometimes
		deltaDist += (avg*niceFactor-concedestep)*(ourMaxDist/theirMaxDist);
		ourDist += deltaDist; // add their difference distance to our distance
		
		if (ourDist > ourMaxDist) 
			ourDist = ourMaxDist;
		if (ourDist < 0) 
			ourDist = 0;
		
		double W = ourDist/ourMaxDist;
		W = Double.isNaN(W) ? 1.0 : W;
		if (W > 1) W = 1;
		if (W < 0) W = 0;
		if (it < 150) {
			if (W < 0.5) {
				W = 1;
			}
		}
		it++;
		Log.vln("W: " + W);
		BidDetails nextBid = interpolateBidPoints(ks, myBB, W); // W = 1 means return myBB, W = 0 means return ks
		
		if (Math.random() < this.Ppareto) { // sometimes offer pareto outcome that is closest to our offer
			BidPoint closest = FindClosestParetoBidPoint(nextBid);
			if (closest != null)
				nextBid = findBidDetailsFromBidPoint(closest);
		}
		return nextBid;
	}
	
	// ----------------------- AVERAGING FUNCTION KS ------------------------- //
	
	/**
	 * Get the average difference between the distances of the opponent bid
	 * to the kalai point
	 * @param N amount of bids to average over
	 * @return average difference (positive if walking away, negative if walking towards KS)
	 */
	public double getAvgDifferenceKS (int N) {
		// [1 3 4 5 3] 	length = N
		// [ 2 1 1 2 ]	diff list, length N-1 
		int curSize = distOpponentBidsToKS.size();
		
		// No difference is less than 2 values
		if (curSize <= 2) return 0.0;
		
		// Determine lower bound
		int lower = 0;
		if (curSize >= N) lower = curSize-N-1;
		if (lower < 0) lower = 0;
		
		List<Double> sub = distOpponentBidsToKS.subList(lower, curSize-1);
		double[] diffs = new double[sub.size()-1];
		
		// Calculate differences
		for (int i = 0; i < sub.size()-1; i++) {
			diffs[i] = sub.get(i+1)-sub.get(i);
		}
		double val = 0;
		for (int j = 0; j < diffs.length; j++) {
			val += diffs[j]; // sum
		}
		
		Log.vln("KOE: " + val/(double)diffs.length);
		return val/(double)diffs.length;
	}

	
	// ----------------------- BID FINDING FUNCTIONS ------------------------- //
	
	/**
	 * Find closest pareto bidpoint
	 * @param bid - the bid of which we want to find the closest pareto bid for
	 * @return closest pareto bidpoint
	 */
	public BidPoint FindClosestParetoBidPoint(BidDetails bid) {
		List<BidPoint> pareto = null;
		try {
			pareto = bidSpace.getParetoFrontier();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		BidPoint nextBidPoint = getBidPointFromBidDetails(bid);
		
		double minDist = 5.0; 
		BidPoint closest = null;
		for (BidPoint B : pareto) { // loop over pareto to find closest bid
			// our utility distance is more important than theirs
			// so we weight our distance with weight 2 and theirs with 1
			double dist = getAdjustedDistanceBetweenBidPoints(B, nextBidPoint, 2.0, 1.0);
			if (dist < minDist) {
				minDist = dist;
				closest = B;
			}
		}
		return closest;
	}
	
	/**
	 * Interpolates between two bidpoints, and returns the bid details of the interpolated found (nearest) bid
	 * @param B1 
	 * @param B2
	 * @param W1 if 1: B2 is returned, if 0: B1 is returned
	 * @return found biddetails
	 */
	public BidDetails interpolateBidPoints(BidPoint B1, BidPoint B2, double W1) {
		double U1A = B1.getUtilityA();
		double U1B = B2.getUtilityB();
		double U2A = B2.getUtilityA();
		double U2B = B2.getUtilityB();
		
		double wantedUtilA = U1A*(1-W1) + U2A*W1;
		double wantedUtilB = U1B*(1-W1) + U2B*W1;
		return getNearestBidDetailsFromUtilities(wantedUtilA, wantedUtilB, 0.025);
	}
	
	/**
	 * Finds the best bid point in utility space A
	 * @param A the utility space (can be of our agent or opponent)
	 * @return best bid point
	 */
	public BidPoint getBestBidPointFromUtilitySpace(UtilitySpace A) {
		Bid BB = null;
		try {
			BB = A.getMaxUtilityBid();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Double[] utilities2 = new Double[2];
		try {
			utilities2[0] = ourUtilitySpace.getUtility(BB);
			utilities2[1] = opponentModel.getBidEvaluation(BB);
		} catch (Exception e) {
			e.printStackTrace();
		}
		BidPoint best = new BidPoint(BB, utilities2);
		return best;
	}
	
	/** 
	 * Find the biddetails nearest to these utilities
	 * @param UA utility of our agent
	 * @param UB utility of opponent
	 * @param maxR maximum range
	 * @return bidDetails of the nearest bid
	 */
	public BidDetails getNearestBidDetailsFromUtilities(double UA, double UB, double maxR) {
		double curR = 0.025;
		// find bids in this range
		Range r = new Range(UA-curR, UA+curR);	
		List<BidDetails> bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);

		int maxTries = 4;
		
		while (bidsInRange.size() > 500 && maxTries > 0) { // not too many bids
			curR /= 2;
			r = new Range(UA-curR, UA+curR);	
			bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);
			Log.h("!");
			maxTries--;
		}
		maxTries = 10;
		while (bidsInRange.size() < 1 && maxTries > 0) { // not enough bids
			curR *= 2;
			r = new Range(UA-curR, UA+curR);	
			bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);
			Log.h("i");
			maxTries--;
		}
		if (bidsInRange.size() == 0) { // do bid nearest to this utility because there are none
			return outcomespace.getBidNearUtility(UA);
		} 	
		double minDist = 2.0;
		BidDetails bestBid = null;
		for (BidDetails B : bidsInRange) { // look for bid with smallest euclidean distance
			double myU = B.getMyUndiscountedUtil();
			double theirU = opponentModel.getBidEvaluation(B.getBid());
			double dist = Math.sqrt(Math.pow(UA-myU,2) + Math.pow(UB-theirU,2));
			if (dist < minDist) {
				minDist = dist;
				bestBid = B;
			}
		}
		return bestBid;
	}
	
	// ------------------------- OPPONENT FUNCTIONS -------------------------------------------
	
	/**
	 * Updates the bidspace
	 */
	public void updateBidSpace () {
		UtilitySpace utilitySpaceOpponent = 	opponentModel.getOpponentUtilitySpace();
		// BidSpace build from ours/opponents
		try {
			// Compute the bid space from our and their utility space
			bidSpace = new BidSpace(ourUtilitySpace, utilitySpaceOpponent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ks = getKalaiSmorodisky();
	}
	
	/**
	 * Finds the kalai point using the bidspace
	 * @return
	 */
	public BidPoint getKalaiSmorodisky () {
		// Build bidSpace
		if (bidSpace == null)
			updateBidSpace();
		BidSpace bs = bidSpace;
		BidPoint ks = null;
		try {
			// Calculate Kalai
			ks = bs.getKalaiSmorodinsky();
			Helper.get(negotiationSession).setKalaiPoint(ks);	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ks;
	}

	// --------------------------- DISTANCE FUNCTIONS -------------------------- //
	
	/**
	 * Find regular euclidean distance between two bidpoints
	 * @param B1
	 * @param B2
	 * @return
	 */
	public double getDistanceBetweenBidPoints(BidPoint B1, BidPoint B2) {
		double U1A = B1.getUtilityA();
		double U1B = B2.getUtilityB();
		double U2A = B2.getUtilityA();
		double U2B = B2.getUtilityB();
		return Math.sqrt(Math.pow(U1A-U2A,2) + Math.pow(U1B-U2B, 2));
	}
	
	/**
	 * Find weighted euclidean distance between B1 and B2.
	 * @param B1
	 * @param B2
	 * @param W1 weight for our utility distance
	 * @param W2 weight for opponent utility distance
	 * @return
	 */
	public double getAdjustedDistanceBetweenBidPoints(BidPoint B1, BidPoint B2, double W1, double W2) {
		double U1A = B1.getUtilityA();
		double U1B = B2.getUtilityB();
		double U2A = B2.getUtilityA();
		double U2B = B2.getUtilityB();
		return Math.sqrt(W1*Math.pow(U1A-U2A,2) + W2*Math.pow(U1B-U2B, 2));
	}
	
	// ------------------ CONVERSION FUNCTIONS --------------------------- //
	
	/**
	 * Searches outcomespace for the biddetails of bidpoint B
	 * @param B bidpoint 
	 * @return the biddetails of this bid
	 */
	public BidDetails findBidDetailsFromBidPoint(BidPoint B) {
		double curR = 0.005;
		Range r = new Range(B.getUtilityA()-curR, B.getUtilityA()+curR);	
		List<BidDetails> bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);
		for (BidDetails B2: bidsInRange) {
			if (B2.getMyUndiscountedUtil() == B.getUtilityA())
				if (opponentModel.getBidEvaluation(B2.getBid()) == B.getUtilityB())
					return B2;
		}
		return null;
	}
	
	/**
	 * Find biddetails from bid
	 * @param B bid
	 * @return biddetails
	 */
	public BidDetails getBidDetailsFromBid(Bid B) {
		BidDetails C = null;
		try {
			C = new BidDetails(B, ourUtilitySpace.getUtility(B));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return C;
	}
	
	/**
	 * Finds the bidpoint from the bid A
	 * @param A bid
	 * @return bidpoint
	 */
	public BidPoint getBidPointFromBid(Bid A) {
		Double[] utilities2 = new Double[2];
		try {
			utilities2[0] = ourUtilitySpace.getUtility(A);
			utilities2[1] = opponentModel.getBidEvaluation(A);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new BidPoint(A, utilities2);
	}
	
	/**
	 * Finds the bidpoint from biddetails
	 * @param A biddetails
	 * @return bidpoint
	 */
	public BidPoint getBidPointFromBidDetails(BidDetails A) {
		return getBidPointFromBid(A.getBid());
	}
}
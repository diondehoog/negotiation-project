package negotiator.group7.phases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import misc.Range;
import negotiator.Bid;
import negotiator.analysis.BidPoint;
import negotiator.analysis.BidSpace;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.group7.Convolution;
import negotiator.group7.Log;
import negotiator.group7.Helper;
import negotiator.group7.OpponentBidCompare;
import negotiator.group7.OpponentType;
import negotiator.group7.OpponentTypeEstimator;
import negotiator.utility.UtilitySpace;

public class Phase2 extends Phase{
	private double tft1;
	private double tft2;
	/** k \in [0, 1]. For k = 0 the agent starts with a bid of maximum utility */
	private double k ;
	/** Maximum target utility */
	private double Pmax;
	/** Minimum target utility */
	private double Pmin;
	/** Concession factor */
	private double e;
	
	/** Phase boundaries */
	private double[] phaseBoundary;
	private double   phase2LowerBound;
	private double   phase2range;
	private double   lastWantedUtil = 1;
	private double   lastWantedUtilOpp = 0;
	private double 	 lastDistance2Kalai = 0;
	
	/** Initialize variables */
	private double Ppareto = 0.5; // probability of offering pareto
	private int averageOver = 5; // how many bids to average over to determine concession of opponent
	private double niceFactor = 0.33; // when opponent concedes, their concession is multiplied by this
	private double Pconcede = 0.05; // probability of conceding to make opponent happy
	private double concedeFactor = 0.3; // amount of distance to concede to KS
	
//TODO explain these two
	private int concedeSteps = 10; // concession steps taken after each other
	
	private int concedeStep = -1;
	
	/** Outcome space */
	SortedOutcomeSpace outcomespace;
	UtilitySpace ourUtilitySpace;
	
	/** ArrayLists for saving history */
	private BidSpace bidSpace; 
	
	private ArrayList<Double> distOpponentBidsToKS = new ArrayList<Double>();
	
	private double ourDist = -1.0;
	private double ourMaxDist = -1.0;
	
	public Phase2(NegotiationSession negSession, OpponentModel opponentModel, double phaseStart, double phaseEnd, 
			double Ppareto, int averageOver, double niceFactor, double Pconcede, double concedeFactor, int concedeSteps, 
			double k, double e, double[] phaseBoundary, double phase2LowerBound, double phase2range,
			SortedOutcomeSpace outcomespace) {
		super(negSession, opponentModel, phaseStart, phaseEnd);
		this.tft1 = tft1;
		this.tft2 = tft2;
		this.k = k;
		this.e = e;
		this.phaseBoundary = phaseBoundary;
		this.phase2LowerBound = phase2LowerBound;
		this.phase2range = phase2range;
		this.outcomespace = outcomespace;
		
		// Set our utility space once
		ourUtilitySpace = negotiationSession.getUtilitySpace();
		
		updateBidSpace();
	}
	
	@Override
	public BidDetails determineNextBid() {
		
		// Update bid space every iteration
		updateBidSpace();
		
		// By default return best current bid
		BidDetails nextBid = negotiationSession.getOwnBidHistory().getBestBidDetails();
		
		// Last bid of the opponent
		BidDetails bidB = negotiationSession.getOpponentBidHistory().getLastBidDetails();
		// Calculate our utility and that of the opponent of the last bid
		Double[] utilities = {bidB.getMyUndiscountedUtil(), opponentModel.getBidEvaluation(bidB.getBid())};
		// Create BidPoint using the opponents bid and the two utilities
		BidPoint bidPointB = new BidPoint(bidB.getBid(), utilities);
		
		// Calculate distance between last opponents bid to estimated KS
		double ksDist = getDistanceToKalaiSmorodinsky(bidPointB);
		distOpponentBidsToKS.add(ksDist);
		
		//System.out.println("Opponents distance to KS point = " + ksDist);
		
		double x = getAvgDifferenceKS(averageOver);
		//System.out.println("Average difference to KS over last 5 bids = " + x);
		
		
		UtilitySpace utilitySpaceOpponent = opponentModel.getOpponentUtilitySpace();
		
		BidPoint myBB = getBestBidPointFromUtilitySpace(ourUtilitySpace);
		BidPoint theirBB = getBestBidPointFromUtilitySpace(utilitySpaceOpponent);
		BidPoint ks = getKalaiSmorodisky();
		

		double theirMaxDist = getDistanceToKalaiSmorodinsky(theirBB);
		double theirDist = ksDist;
		
		if (ourDist < -0.05) { // if first time ever
			ourDist = (ourMaxDist/theirMaxDist)*ksDist; // match their concession
			//ourDist = getDistanceToKalaiSmorodinsky(myBB);
			ourMaxDist = getDistanceToKalaiSmorodinsky(myBB);
		}
		
		
		// ourDist = theirDist; // just mirror the opponent bid 
		
		if ((concedeStep == -1)&&(Math.random()<this.Pconcede)) {
			concedeStep = 1;
		}
		
		double xconcede = 0;
		if (concedeStep > 0) {
			Log.vln("Concede! Just because we are nice. :) Step: " + concedeStep);
			xconcede = concedeStep/concedeSteps*concedeFactor;
			concedeStep++;
			if (concedeStep >= this.concedeSteps) {
				concedeStep = -1;
			}
		}
		
		ourDist += (x*niceFactor-xconcede)*(ourMaxDist/theirMaxDist); // add their difference distance to our distance
		
		if (ourDist > ourMaxDist) {
			ourDist = ourMaxDist;
		}
		
		double W = ourDist/ourMaxDist;
		//double W = theirDist/theirMaxDist;
		
		//Log.vln("Percentage: " + String.format("%3.2f",W) + " (1 means bad, 0 means KS)");
		
		nextBid = interpolateBidPoints(ks, myBB, W); // W = 1 means return myBB, W = 0 means return ks
		
		// TODO: this might be bad, since we might concede alot because of this! :(
		if (Math.random()<this.Ppareto) { // 50% of time offer pareto outcome that is closest to our offer
			Log.vln("Offer pareto, just because were nice! :) ");
			List<BidPoint> pareto = null;
			try {
				pareto = bidSpace.getParetoFrontier();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			BidPoint nextBidPoint = getBidPointFromBidDetails(nextBid);
			
			double minDist = 5.0; 
			BidPoint closest = null;
			for (BidPoint B : pareto) { // loop over pareto to find closest bid
				
				// our utility distance is more important than theirs
				double dist = getAdjustedDistanceBetweenBidPoints(B, nextBidPoint, 2.0, 1.0);
				if (dist < minDist) {
					minDist = dist;
					closest = B;
				}
				Log.vln("Finding pareto bid...");
			}
			
			if (closest == null) {
				System.out.println("Closest bid not found");
			}
			// TODO: check if our utility is not TOO high
			
			nextBid = findBidDetailsFromBidPoint(closest);
			
		}
				
		return nextBid;
		
	}
	
	public BidDetails findBidDetailsFromBidPoint(BidPoint B) {
		double curR = 0.005;
		Range r = new Range(B.getUtilityA()-curR, B.getUtilityA()+curR);	
		List<BidDetails> bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);
		for (BidDetails B2: bidsInRange) {
			Log.vln("Finding biddetails...");
			if (B2.getMyUndiscountedUtil() == B.getUtilityA())
				if (opponentModel.getBidEvaluation(B2.getBid()) == B.getUtilityB())
					return B2;
		}
		return null;
	}
	
	public double getDistanceBetweenBidPoints(BidPoint B1, BidPoint B2) {
		double U1A = B1.getUtilityA();
		double U1B = B2.getUtilityB();
		double U2A = B2.getUtilityA();
		double U2B = B2.getUtilityB();
		return Math.sqrt(Math.pow(U1A-U2A,2) + Math.pow(U1B-U2B, 2));
	}
	
	public double getAdjustedDistanceBetweenBidPoints(BidPoint B1, BidPoint B2, double W1, double W2) {
		double U1A = B1.getUtilityA();
		double U1B = B2.getUtilityB();
		double U2A = B2.getUtilityA();
		double U2B = B2.getUtilityB();
		return Math.sqrt(W1*Math.pow(U1A-U2A,2) + W2*Math.pow(U1B-U2B, 2));
	}
	
	public BidDetails getBidDetailsFromBidPoint(BidPoint A) { // this function does not work :(
		Bid B = A.getBid();
		if (B == null) {
			System.out.println("JA DAS NULL HE!"); // jammer genius
		}
		return getBidDetailsFromBid(B);
	}
	
	public BidDetails getBidDetailsFromBid(Bid B) {
		BidDetails C = null;
		try {
			C = new BidDetails(B, ourUtilitySpace.getUtility(B));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return C;
	}
	
	public BidDetails interpolateBidPoints(BidPoint B1, BidPoint B2, double W1) {
		double U1A = B1.getUtilityA();
		double U1B = B2.getUtilityB();
		double U2A = B2.getUtilityA();
		double U2B = B2.getUtilityB();
		double wantedUtilA = U1A*(1-W1) + U2A*W1;
		double wantedUtilB = U1B*(1-W1) + U2B*W1;
//trash		ArrayList<Bid> koe = new ArrayList<Bid>();
		
		return getNearestBidDetailsFromUtilities(wantedUtilA, wantedUtilB, 0.025);
		
		//BidPoint WantedBidPoint = bidSpace.getNearestBidPoint(wantedUtilA, wantedUtilB, 0.75, 0.25, koe);
		//Bid WantedBid = WantedBidPoint.getBid();
		//BidDetails WantedBidDetails = new BidDetails(WantedBid, WantedBidPoint.getUtilityA());
		//return WantedBidDetails;
	}
	
	public BidPoint getBidPointFromBid(Bid A) {
		Double[] utilities2 = new Double[2];
		try {
			utilities2[0] = ourUtilitySpace.getUtility(A);
			utilities2[1] = opponentModel.getBidEvaluation(A);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new BidPoint(A, utilities2);
	}
	
	public BidPoint getBidPointFromBidDetails(BidDetails A) {
		return getBidPointFromBid(A.getBid());
	}
	
	public BidPoint getBestBidPointFromUtilitySpace(UtilitySpace A) {
		
		Bid BB = null;
		try {
			BB = A.getMaxUtilityBid();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Double[] utilities2 = new Double[2];
		try {
			utilities2[0] = ourUtilitySpace.getUtility(BB);
			utilities2[1] = opponentModel.getBidEvaluation(BB);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BidPoint best = new BidPoint(BB, utilities2);
		return best;
	}
	
	public double getMaxDistToKalai() {
		double max = -1.0;
		for (double dist : distOpponentBidsToKS) {
			Log.vln("Runialoop");
			if (dist > max)
				max = dist;
		}
		return max;
	}
	
	public double getDistToKalaiLastNBids (int N) {
		
		int curSize = distOpponentBidsToKS.size();
		
		// Determine lower bound
		int lower = 0;
		if (curSize >= N) lower = curSize-N-1;
		if (lower < 0) lower = 0;
		
		List<Double> sub = distOpponentBidsToKS.subList(lower, curSize-1);
		
		return getListAverage(sub);
	}
	
	//Returns the average difference to the KS between the N last bids
	public double getAvgDifferenceKS (int N) {
		int curSize = distOpponentBidsToKS.size();

		// No difference is less than 2 values
		if (curSize < 2) return 0.0;
		
		// Determine lower bound
		int lower = 0;
		if (curSize >= N) lower = curSize-N-1;
		if (lower < 0) lower = 0;
		
		//Put last distances of the opponent into list sub
		List<Double> sub = distOpponentBidsToKS.subList(lower, curSize-1);
		double[] diffs = new double[sub.size()-1];
		
		double val2 = sub.get(0) - sub.get(sub.size()-1);
		
		// Calculate differences, diffs goes from old [0] to new [sub.size-1] bids
		for (int i = 0; i < sub.size()-1; i++) {
			diffs[i] = sub.get(i+1)-sub.get(i);
		}
		
		double val = 0;
		
		for (int j = 0; j < diffs.length; j++) {
			Log.vln("Some loop");
			val += diffs[j];
		}
		//System.out.println("val1/val2: " + val + ", " + val2);
		return val/(double)diffs.length;
	}
	
	public double getListAverage(List<Double> input) {
		if (input.isEmpty()) return 0.0;
		double val = 0;
		for (Double d : input){	val += d; }
		return val/(double)input.size();
	}
	
	/**
	 * From [1]:
	 * 
	 * A wide range of time dependent functions can be defined by varying the way in
	 * which f(t) is computed. However, functions must ensure that 0 <= f(t) <= 1,
	 * f(0) = k, and f(1) = 1.
	 * 
	 * That is, the offer will always be between the value range, 
	 * at the beginning it will give the initial constant and when the deadline is reached, it
	 * will offer the reservation value.
	 * 
	 * For 0 < e < 1 it will behave as a Hardliner / Hardheader / Boulware
	 * For e = 1 it will behave as a linear agent
	 * For e > 1 it will behave as a conceder (it will give low utilities faster than linear)                 
	 */
	
//TODO Weggooien
//	public double f(double t)
//	{
//		if (e == 0)
//			return k;
//		if (t < this.phaseStart)
//			return 1;
//		if (t > this.phaseEnd)
//			return 1;
		
		// scale t
//		double torig = t;
//		t = (t - this.phaseStart) * (this.phaseEnd -  this.phaseStart);
		//Log.dln("Original t:" + torig + ", t between " + this.phaseBoundary[0] + " and " + this.phaseBoundary[1] + ": " + t);
		
//		double ft = k + (1 - k) * Math.pow(t, 1.0/e);
//		return ft;
//	}

	/**
	 * Makes sure the target utility with in the acceptable range according to the domain
	 * Goes from Pmax to Pmin!
	 * @param t
	 * @return double
	 */
//	public double p(double t) {
		
//		double pt = phase2LowerBound + (Pmax - phase2LowerBound) * (1 - f(t));
//		return pt;
//	}
	
	/**
	 * This method returns the average difference over the last n bids.
	 * Can be used to see the behavior the agent over time.
	 * 
	 * @param n
	 * @return
	 */
	
	public BidDetails getNearestBidDetailsFromUtilities(double UA, double UB, double maxR) {

		double curR = 0.05;
		// find bids in this range
		Range r = new Range(UA-curR, UA+curR);	
		List<BidDetails> bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);
		
		while (bidsInRange.size() > 500) {
			curR /= 2;
			r = new Range(UA-curR, UA+curR);	
			bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);
			Log.vln("Making radius smaller...");
		}
		
		while (bidsInRange.size() < 1) {
			curR *= 2;
			r = new Range(UA-curR, UA+curR);	
			bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);
			Log.vln("Enlarging radius... current radius:" + curR + " found bids: " + bidsInRange.size());
		}
		
		if (bidsInRange.size() == 0) { // do bid nearest to this utility because there are none
			return outcomespace.getBidNearUtility(UA);
		} 
		//System.out.println("Found: " + bidsInRange.size() + " bids in range " + curR);
		double minDist = 2.0;
		BidDetails bestBid = null;
		for (BidDetails B : bidsInRange) { // look for bid with smallest euclidean distance
			Log.vln("Loop over bid too find nearest bid");
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
	
	/* Decide bid closest to optimal frontier */
	public BidDetails close2Pareto(double nextBidUtil){ 							
	
		Range r = new Range(nextBidUtil-phase2range, nextBidUtil+phase2range);
	
		Double temp = new Double(nextBidUtil);
		Double range2 = new Double(phase2range);
		BidDetails nextBid;

		//Log.vln("I want an utility of: " + temp.toString() + " range: " + range2);

		List<BidDetails> bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);

		if (bidsInRange.size() == 0) { // do standard bid because we dont have any choices
	
			nextBid =  outcomespace.getBidNearUtility(nextBidUtil);
		
		} else {
			OpponentBidCompare comparebids = new OpponentBidCompare();
			comparebids.setOpponentModel(opponentModel);
		
			Collections.sort(bidsInRange, comparebids);
		
			//Log.v("Max: " + opponentModel.getBidEvaluation(bidsInRange.get(0).getBid()) + ", ");
			//Log.vln("Min: " + opponentModel.getBidEvaluation(bidsInRange.get(bidsInRange.size()-1).getBid()));
			nextBid = bidsInRange.get(0);
			
		}
		
		return nextBid;
	}
	
	
	public double getAverageDiffLastNBids (int n) {
		
		// Get list of opponent bids sorted on time
		List<BidDetails> h = negotiationSession.getOpponentBidHistory().sortToTime().getHistory();

		if (n > negotiationSession.getOpponentBidHistory().size()) {
			// Not enough bids in history! n is set to the size-1
			n = negotiationSession.getOpponentBidHistory().size()-1;
		}
		if (n <= 1) {
			return 0;
		}
		
		// Save values
		double[] vals = new double[n];
		
		double avg = 0;
		
		// TODO: Smooth the values
		
		for (int i = 0; i < n-1; i++) {
			Log.vln("Another runialoop");
			//BidDetails bd = h.get(i);
			//Log.rln("Bid at time " + bd.getTime() + " has utility " + bd.getMyUndiscountedUtil());
			vals[i] = h.get(i).getMyUndiscountedUtil() - h.get(i+1).getMyUndiscountedUtil();
			avg += vals[i];
		}
		
		avg = avg/((double)n-1.0);
		
		
		Log.sln("Average concede over last " + n + " bids = " + avg);
/*
		double[] smooth = new double[n];
		
		Log.rln("###################################");
		
		// Smoothing kernel 
		double[] kernel = {1.0/6.0, 4.0/6.0, 1.0/6.0};
		
		for (int i = 0; i < n; i++) {
			smooth[i] = Convolution.apply(vals, i, kernel);
			Log.rln("Value at index = " + i + " has value " + vals[i] + " and after smoothing " + smooth[i]);
		}
		
		
		//Log.rln("Average concede over last " + n + " bids = " + avg);
	*/	
				
		return avg;
	}
	
	public BidPoint getKalaiSmorodisky () {
		// Build bidSpace
		//BidSpace bs = getCurrentBidSpace();
		BidSpace bs = bidSpace;
		
		BidPoint ks;
		try {
			ks = bs.getParetoFrontier().get(0); // FALLBACK
		} catch (Exception e1) {
			e1.printStackTrace();
			ks = bs.bidPoints.get(0); // worst fallback ever ?
		}
		
		try {
			// Calculate Kalai-Smorodinsky
			ks = bs.getKalaiSmorodinsky();
			Helper.setKalaiPoint(ks);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ks;
	}
	
	public double getDistanceToKalaiSmorodinsky (BidPoint input) {
		
		// Calculate Kalai-Smorodinsky
		BidPoint ks = getKalaiSmorodisky();
		
		if (ks != null) {
			// Return the distance from the bid point to the KS
			return ks.getDistance(input);
		} else {
			return 0.0;
		}
		
	}
	
	//updates BidSpace: 
	public void updateBidSpace () {
		
		//ourUtilitySpace
		UtilitySpace utilitySpaceOpponent = opponentModel.getOpponentUtilitySpace();
		
		// BidSpace is build from ours and opponents 
		try {
			// Compute the bid space from our and their utility space
			bidSpace = new BidSpace(ourUtilitySpace, utilitySpaceOpponent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//TODO Weggooien
//	public BidSpace getCurrentBidSpace () {
//		return bidSpace;
//	}

	

}

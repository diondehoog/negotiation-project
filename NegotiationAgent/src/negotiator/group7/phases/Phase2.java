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
	
	/** Outcome space */
	SortedOutcomeSpace outcomespace;
	UtilitySpace ourUtilitySpace;
	
	/** ArrayLists for saving history */
	private BidSpace bidSpace; 
	private ArrayList<BidPoint> kalaiPoints;
	
	private ArrayList<Double> distOpponentBidsToKS = new ArrayList<Double>();
	
	private double ourDist = -1.0;
	private double ourMaxDist = -1.0;
	
	public Phase2(NegotiationSession negSession, OpponentModel opponentModel, double phaseStart, double phaseEnd, 
			double tft1, double tft2, double k, double e, 
			double[] phaseBoundary, double phase2LowerBound, double phase2range,
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
		
		// Initializing history lists
		kalaiPoints = 	new ArrayList<BidPoint>();
		
		// Set our utility space once
		ourUtilitySpace = negotiationSession.getUtilitySpace();
		
		updateBidSpace();
		
	}
	
	@Override
	public BidDetails determineNextBid() {
		
		// Update bid space every iteration
		updateBidSpace();
		
		// Second negotiation phase (implemented by Arnold)
		double time = negotiationSession.getTime();
		
		// By default return best current bid
		BidDetails nextBid = negotiationSession.getOwnBidHistory().getBestBidDetails();
		
		// Last bid of the opponent
		BidDetails bidB = negotiationSession.getOpponentBidHistory().getLastBidDetails();
		// Calculate our utility and that of the opponent
		Double[] utilities = {bidB.getMyUndiscountedUtil(), opponentModel.getBidEvaluation(bidB.getBid())};
		// Create BidPoint using the opponents bid and the two utilities
		BidPoint bidPointB = new BidPoint(bidB.getBid(), utilities);
		
		// Calculate distance last opponents bid to estimated KS and save the value
		double ksDist = getDistanceToKalaiSmorodinsky(bidPointB);
		distOpponentBidsToKS.add(ksDist);
		
		//System.out.println("Opponents distance to KS point = " + ksDist);
		
		int av = 5; // how many bids to average over
		double x = getAvgDifferenceKS(av);
		System.out.println("Average difference to KS over last 5 bids = " + x);
		
		try {
			Bid bestbid = ourUtilitySpace.getMaxUtilityBid();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BidPoint ks = getKalaiSmorodisky();
		double ourUtil = ks.getUtilityA();
		double theirUtil = ks.getUtilityB();
		
		UtilitySpace utilitySpaceOpponent = opponentModel.getOpponentUtilitySpace();
		
		BidPoint myBB = getBestBidPointFromUtilitySpace(ourUtilitySpace);
		BidPoint theirBB = getBestBidPointFromUtilitySpace(utilitySpaceOpponent);
		
		if (ourDist < -0.05) {
			ourDist = getDistanceToKalaiSmorodinsky(myBB);
			ourMaxDist = getDistanceToKalaiSmorodinsky(myBB);
		}
		
		double theirMaxDist = getDistanceToKalaiSmorodinsky(theirBB);
		double theirDist = ksDist;
		
		ourDist += x*(ourMaxDist/theirMaxDist)*1/(3.0); // add their difference distance to our distance
		
		if (ourDist > ourMaxDist) {
			ourDist = ourMaxDist;
		}
		
		double W = ourDist/ourMaxDist;
		//double W = theirDist/theirMaxDist;
		
		Double W2 = new Double(W);
		Log.vln("Percentage: " + String.format("%3.2f",W) + " (1 means bad, 0 means KS)");
		
		nextBid = interpolateBidPoints(ks, myBB, W); // W = 1 means return myBB, W = 0 means return ks
		
		if (Math.random()>0.5) { // 50% of time offer pareto outcome that is closest to our offer
			List<BidPoint> pareto = null;
			try {
				pareto = bidSpace.getParetoFrontier();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			BidPoint nextBidPoint = getBidPointFromBidDetails(nextBid);
			
			double minDist = 2.0;
			BidPoint closest = null;
			for (BidPoint B : pareto) {
				double dist = getDistanceBetweenBidPoints(B, nextBidPoint);
				if (dist < minDist) {
					minDist = dist;
					closest = B;
				}
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
		System.out.println("Crash?");
		Range r = new Range(B.getUtilityA()-curR, B.getUtilityA()+curR);	
		System.out.println("Koe");
		List<BidDetails> bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);
		System.out.println("Neus");
		for (BidDetails B2: bidsInRange) {
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
	
	public BidDetails getBidDetailsFromBidPoint(BidPoint A) { // this function does not work :(
		Bid B = A.getBid();
		if (B == null) {
			System.out.println("JA DAS NULL HE!");
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
		ArrayList<Bid> koe = new ArrayList<Bid>();
		
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
		
		// Perform smoothing using convolution
		/*double[] k = {1.0/6.0, 4.0/6.0, 1.0/6.0};
		double[] smooth;
		
		try {
			smooth = Convolution.apply(diffs, k, "valid");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			smooth = null;
			e.printStackTrace();
		}*/
		
		double val = 0;
		
		for (int j = 0; j < diffs.length; j++) {
			val += diffs[j];
		}
		
		return val/(double)diffs.length;
	}
	
	public double getListAverage(List<Double> input) {
		if (input.isEmpty()) return 0.0;
		double val = 0;
		for (Double d : input)	val += d;
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
	public double f(double t)
	{
		if (e == 0)
			return k;
		if (t < this.phaseStart)
			return 1;
		if (t > this.phaseEnd)
			return 1;
		
		// scale t
		double torig = t;
		t = (t - this.phaseStart) / (this.phaseEnd -  this.phaseStart);
		//Log.dln("Original t:" + torig + ", t between " + this.phaseBoundary[0] + " and " + this.phaseBoundary[1] + ": " + t);
		
		double ft = k + (1 - k) * Math.pow(t, 1.0/e);
		return ft;
	}

	/**
	 * Makes sure the target utility with in the acceptable range according to the domain
	 * Goes from Pmax to Pmin!
	 * @param t
	 * @return double
	 */
	public double p(double t) {
		
		double pt = phase2LowerBound + (Pmax - phase2LowerBound) * (1 - f(t));
		//Log.dln("p is: " + pt + " en dat is " + (pt > 1 ? "KUT" : "NICE"));
		return pt;
	}
	
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
		
		while (bidsInRange.size() < 50) {
			curR *= 2;
			r = new Range(UA-curR, UA+curR);	
			bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);
		}
		
		while (bidsInRange.size() > 500) {
			curR /= 2;
			r = new Range(UA-curR, UA+curR);	
			bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);
		}
		
		if (bidsInRange.size() == 0) { // do bid nearest to this utility because there are none
			return outcomespace.getBidNearUtility(UA);
		} 
		System.out.println("Found: " + bidsInRange.size() + " bids in range " + curR);
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
	
	/* Decide bid closest to optimal frontier */
	public BidDetails close2Pareto(double nextBidUtil){ 							
	
		Range r = new Range(nextBidUtil-phase2range, nextBidUtil+phase2range);
	
		Double temp = new Double(nextBidUtil);
		Double range2 = new Double(phase2range);
		BidDetails nextBid;

		Log.vln("I want an utility of: " + temp.toString() + " range: " + range2);

		List<BidDetails> bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);

		if (bidsInRange.size() == 0) { // do standard bid because we dont have any choices
	
			nextBid =  outcomespace.getBidNearUtility(nextBidUtil);
		
		} else {
			OpponentBidCompare comparebids = new OpponentBidCompare();
			comparebids.setOpponentModel(opponentModel);
		
			Collections.sort(bidsInRange, comparebids);
		
			Log.v("Max: " + opponentModel.getBidEvaluation(bidsInRange.get(0).getBid()) + ", ");
			Log.vln("Min: " + opponentModel.getBidEvaluation(bidsInRange.get(bidsInRange.size()-1).getBid()));
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
		
		BidPoint ks = null;
		
		//Log.rln("#############################################");
		//Log.rln("OPPONENT: " + spaceOpponent.toString() );
		//Log.rln("OURS: " + spaceOurs.toString());
		
		// Build bidSpace
		BidSpace bs = getCurrentBidSpace();
		
		try {
			// Calculate Kalai-Smorodinsky
			ks = bs.getKalaiSmorodinsky();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ks;
	}
	
	public double getDistanceToKalaiSmorodinsky (BidPoint input) {
		
		// Calculate Kalai-Smorodinsky
		BidPoint ks = getKalaiSmorodisky();
		
		// Return the distance from the bid point to the KS
		return ks.getDistance(input);
		
	}
	
	public void updateBidSpace () {
		
		//ourUtilitySpace
		UtilitySpace utilitySpaceOpponent = 	opponentModel.getOpponentUtilitySpace();
		
		// BidSpace build from ours/opponents 
		BidSpace bs;
		try {
			// Compute the bid space from our and their utility space
			bidSpace = new BidSpace(ourUtilitySpace, utilitySpaceOpponent);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public BidSpace getCurrentBidSpace () {
		return bidSpace;
	}

	

}

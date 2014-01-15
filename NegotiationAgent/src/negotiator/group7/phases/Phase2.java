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
	private double 	 lastDistance2Kalai = 0;
	
	/** Outcome space */
	SortedOutcomeSpace outcomespace;
	
	/** ArrayList for saving bid spaces */
	private ArrayList<BidSpace> bidSpaces;
	
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
		
		bidSpaces = new ArrayList<BidSpace>();
	}
	
	@Override
	public BidDetails determineNextBid() {
		Log.sln("Phase 2");
		// Second negotiation phase (implemented by Arnold)
		double time = negotiationSession.getTime();
		BidDetails nextBid;
		
		/* Opponent modelling by Bas */
		//OpponentType type = OpponentTypeEstimator.EstimateType(this.negotiationSession, this.opponentModel, 100);
		//Log.dln("EstimatedOpponentType: " + type.toString());
				
		
		//int opponentClass = 1 for Hardheaded, 2 for Conceder, 3 for random
		
		BidDetails lastBid = negotiationSession.getOwnBidHistory().getLastBidDetails();
		getDistToKalaiSmorodinsky(lastBid);
		
//		double bestBid = negotiationSession.getOpponentBidHistory().getBestBidDetails().getMyUndiscountedUtil();

		double difference;
		List<BidDetails> lastOpponentBids = negotiationSession.getOpponentBidHistory().sortToTime().getHistory();
		Double lastOwnUtil = negotiationSession.getOwnBidHistory().getLastBidDetails().getMyUndiscountedUtil();
		//Calculate difference between last bid and before last bid
		if (lastOpponentBids.size() > 0){
			
			difference = getAverageDiffLastNBids(2);
			Log.dln("Difference: " + String.format("%5f", difference) + ", lastWantedUtil: " + String.format("%5f", this.lastWantedUtil));

			double nextBidUtil;
			
			//The opponent is approaching us in utility
			if (difference>0)
				//nextBidUtil = Math.max(lastOwnUtil-(difference*tft1),p(time));
				nextBidUtil = lastWantedUtil-(difference*tft1);
			
			//The opponent is distancing from us in utility
			else
				//nextBidUtil = Math.max(lastOwnUtil-(difference*tft2),p(time));
				nextBidUtil = lastWantedUtil-(difference*tft2);
			
			nextBidUtil = Math.min(nextBidUtil, 1);

			double prevDistance2Kalai = lastDistance2Kalai;
			lastDistance2Kalai = distance2Kalai(negotiationSession.getOpponentBidHistory().getLastBidDetails());
			
			//Calculate the relative distance the opponent went to the Kalai point
			double relDist = 1-(lastDistance2Kalai/prevDistance2Kalai);
			
			//Calculate linear interpolation
			getKalaiPoint
			//If there has been a better bid of the opponent, don't go below
//			nextBidUtil = Math.max(nextBidUtil, bestBid);
			
			//Log.dln("nextBidUtil = " + nextBidUtil);
			
			/* Decide bid closest to optimal frontier */				
			nextBid = close2Pareto(nextBidUtil);
			
			//Set the lastWantedUtil for the Util we want now
			lastWantedUtil = nextBidUtil;
		}
		
		else{
			nextBid = negotiationSession.getOutcomeSpace().getBidNearUtility(1);
		}
//		if (nextBid.getMyUndiscountedUtil()>bestBid)
		
		
			return nextBid;
//		else
//			return negotiationSession.getOutcomeSpace().getBidNearUtility(bestBid);
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
	
	public double[] getDistToKalaiSmorodinsky (BidDetails input) {
		
		double[] distances = {0.0, 0.0};
		
		// Fetch two utility spaces to estimate Kalai-Smorodinsky
		UtilitySpace spaceOurs = 		negotiationSession.getUtilitySpace();
		UtilitySpace spaceOpponent = 	opponentModel.getOpponentUtilitySpace();
		
		Log.rln("#############################################");
		Log.rln("OPPONENT: " + spaceOpponent.toString() );
		Log.rln("OURS: " + spaceOurs.toString());
		
		// Build bidSpace
		BidSpace bs;
		
		try {
			// BidSpace build from ours/opponents 
			bs = new BidSpace(spaceOurs, spaceOpponent);
			
			// Save current bid space in array list
			bidSpaces.add(bs);
			
			// Calculate Kalai-Smorodinsky
			BidPoint ks = bs.getKalaiSmorodinsky();
			
			// Calculate the two distances to the KS
			distances[0] = ks.getUtilityA()-input.getMyUndiscountedUtil();
			distances[1] = ks.getUtilityB()-opponentModel.getBidEvaluation(input.getBid());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// If an exception is caught, distances = [0.0, 0.0] is returned
		return distances;
	}
	

}

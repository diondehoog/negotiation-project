package negotiator.group7.phases;

import java.util.Collections;
import java.util.List;

import misc.Range;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.group7.Convolution;
import negotiator.group7.Log;
import negotiator.group7.OpponentBidCompare;
import negotiator.group7.OpponentType;
import negotiator.group7.OpponentTypeEstimator;

public class Phase2 extends Phase{
	private double tft1;
	private double tft2;
	/** k \in [0, 1]. For k = 0 the agent starts with a bid of maximum utility */
	private double k = 0.0;
	/** Maximum target utility */
	private double Pmax;
	/** Minimum target utility */
	private double Pmin;
	/** Concession factor */
	private double e = 1.0;
	
	/** Phase boundaries */
	private double[] phaseBoundary = {0.4, 1};
	private double   phase2LowerBound = 0.6;
	private double   phase2range = 0.05;
	
	/** Outcome space */
	SortedOutcomeSpace outcomespace;
	
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
	}
	
	@Override
	public BidDetails determineNextBid() {
		// Second negotiation phase (implemented by Arnold)
		double time = negotiationSession.getTime();
		BidDetails nextBid;
		Log.dln("Determining next bid in phase 2");
		
		/* Opponent modelling by Bas */
		OpponentType type = OpponentTypeEstimator.EstimateType(this.negotiationSession, this.opponentModel, 100);
		Log.dln("EstimatedOpponentType: " + type.toString());
				
		
		//int opponentClass = 1 for Hardheaded, 2 for Conceder, 3 for random
		
		
		double bestBid = negotiationSession.getOpponentBidHistory().getBestBidDetails().getMyUndiscountedUtil();

		double difference;
		List<BidDetails> lastOpponentBids = negotiationSession.getOpponentBidHistory().sortToTime().getHistory();
		Double lastOwnUtil = negotiationSession.getOwnBidHistory().getLastBidDetails().getMyUndiscountedUtil();
		//Calculate difference between last bid and before last bid
		if (lastOpponentBids.size() > 0){
			
			difference = getAverageDiffLastNBids(10);

			double nextBidUtil;
			
			//The opponent is approaching us in utility
			if (difference>0)
				nextBidUtil = Math.max(lastOwnUtil-(difference*tft1),p(time));
				
			//The opponent is distancing from us in utility
			else
				nextBidUtil = Math.max(lastOwnUtil-(difference*tft2),p(time));
			
			//If there has been a better bid of the opponent, don't go below
			nextBidUtil = Math.max(nextBidUtil, bestBid);
			
			//Log.dln("nextBidUtil = " + nextBidUtil);
			
			/* Decide bid closest to optimal frontier */				
			Range r = new Range(nextBidUtil-phase2range, nextBidUtil+phase2range);
			
			Double temp = new Double(nextBidUtil);
			Double range2 = new Double(phase2range);

			Log.vln("I want an utility of: " + temp.toString() + " range: " + range2);

			List<BidDetails> bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);

			if (bidsInRange.size() == 0) { // do standard bid because we dont have any choices
				nextBid =  outcomespace.getBidNearUtility(nextBidUtil); 
			} else {
				Double sizeList = new Double(bidsInRange.size());
				Log.vln("Number of bids found that are in range:" + sizeList.toString());
				
				OpponentBidCompare comparebids = new OpponentBidCompare();
				comparebids.setOpponentModel(opponentModel);
				
				Collections.sort(bidsInRange, comparebids);
				
				Log.vln("Max: " + opponentModel.getBidEvaluation(bidsInRange.get(0).getBid()));
				Log.vln("Min: " + opponentModel.getBidEvaluation(bidsInRange.get(bidsInRange.size()-1).getBid()));
				nextBid = bidsInRange.get(0);
			}
		}
		else{
			nextBid = negotiationSession.getOutcomeSpace().getBidNearUtility(p(time));
		}
		if (nextBid.getMyUndiscountedUtil()>bestBid)
			return nextBid;
		else
			return negotiationSession.getOutcomeSpace().getBidNearUtility(bestBid);
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
	public double getAverageDiffLastNBids (int n) {
		
		// Get list of opponent bids sorted on time
		List<BidDetails> h = negotiationSession.getOpponentBidHistory().sortToTime().getHistory();

		if (n > negotiationSession.getOpponentBidHistory().size()) {
			// Not enough bids in history! n is set to the size-1
			n = negotiationSession.getOpponentBidHistory().size()-1;
		}
		
		// Save values
		double[] vals = new double[n];
		
		double avg = 0;
		
		// TODO: Smooth the values
		
		for (int i = 0; i < n-1; i++) {
			//BidDetails bd = h.get(i);
			//Log.rln("Bid at time " + bd.getTime() + " has utility " + bd.getMyUndiscountedUtil());
			vals[i] = h.get(i).getMyUndiscountedUtil()-h.get(i+1).getMyUndiscountedUtil();
			avg += vals[i];
		}
		
		avg = avg/n;
		
		Log.rln("Average concede over last " + n + " bids = " + avg);
		Log.sln("Average concede over last " + n + " bids = " + avg);

		double[] smooth = new double[n];
		
		Log.rln("###################################");
		
		// Smoothing kernel 
		double[] kernel = {1.0/6.0, 4.0/6.0, 1.0/6.0};
		
		for (int i = 0; i < n; i++) {
			smooth[i] = Convolution.apply(vals, i, kernel);
			Log.rln("Value at index = " + i + " has value " + vals[i] + " and after smoothing " + smooth[i]);
		}
		
		
		//Log.rln("Average concede over last " + n + " bids = " + avg);
		
				
		return avg;
	}

}

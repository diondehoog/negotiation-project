package negotiator.group7;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import misc.Range;
import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.analysis.BidSpace;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OMStrategy;
import negotiator.boaframework.OfferingStrategy;
import negotiator.boaframework.OpponentModel;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.boaframework.opponentmodel.tools.UtilitySpaceAdapter;
import negotiator.utility.UtilitySpace;

/**
 * This is an abstract class used to implement a TimeDependentAgent Strategy adapted from [1]
 * 	[1]	S. Shaheen Fatima  Michael Wooldridge  Nicholas R. Jennings
 * 		Optimal Negotiation Strategies for Agents with Incomplete Information
 * 		http://eprints.ecs.soton.ac.uk/6151/1/atal01.pdf
 * 
 * The default strategy was extended to enable the usage of opponent models.
 * 
 * Note that this agent is not fully equivalent to the theoretical model, loading the domain
 * may take some time, which may lead to the agent skipping the first bid. A better implementation
 * is GeniusTimeDependent_Offering. 
 * 
 */
public class Group7_BS extends OfferingStrategy {

	/** k \in [0, 1]. For k = 0 the agent starts with a bid of maximum utility */
	private double k = 0.0;
	/** Maximum target utility */
	private double Pmax;
	/** Minimum target utility */
	private double Pmin;
	/** Concession factor */
	private double e = 1.0;
	
	/** Outcome space */
	SortedOutcomeSpace outcomespace;
	
	/** Phase boundaries */
	private double[] phaseBoundary = {0.4, 1};
	private double   phase1LowerBound = 0.8;
	private double   phase1UpperBound = 1.0;
	private double   phase2LowerBound = 0.6;
	private double   phase2range = 0.05;

	
	/** Keep track of the current phase */
	private int curPhase = 1;

	/** Initialize bid history */
	BidHistory biddingHistory;
	
	/** Tit-for-tat parameters: tft1 is amount of approaching, tft2 is amount of distancing*/
	double tft1 = 0.5;
	double tft2 = 0.75;
	
	/** Pareto frontier needs bidspace and utility space to be computed */
	private BidSpace bidSpace; // (not implemented, does not work :( )
	private UtilitySpace myUtilSpace;
	private List<Bid> par; // parato frontier (not implemented)
	private int bidNum = 0; // current bid number
	private int recomputePar = 500; // after every 500 bids recompute the pareto frontier
	private BidDetails nash; // nash product
	
	
	/**
	 * Method which initializes the agent by setting all parameters.
	 * The parameter "e" is the only parameter which is required (concession factor).
	 */
	public void init(NegotiationSession negoSession, OpponentModel model, OMStrategy oms, HashMap<String, Double> parameters) throws Exception {
		
		if (parameters.get("phase2") != null)
			phaseBoundary[0] = parameters.get("phase2");
		
		if (parameters.get("phase3") != null)
			phaseBoundary[1] = parameters.get("phase3");

		if (parameters.get("phase1lowerbound") != null)
			phase1LowerBound = parameters.get("phase1lowerbound");
		
		if (parameters.get("phase1upperbound") != null)
			phase1UpperBound = parameters.get("phase1upperbound");
		
		if (parameters.get("phase2lowerbound") != null)
			phase2LowerBound = parameters.get("phase2lowerbound");
		
		if (parameters.get("e") != null)
			e = parameters.get("e");
		
		negotiationSession = negoSession;
		
		this.myUtilSpace = negotiationSession.getUtilitySpace();
		
		// TODO: outcomespace is more efficient in finding bids near a known utility for us (not for opponent preferences)
		outcomespace = new SortedOutcomeSpace(negotiationSession.getUtilitySpace());
		
		negotiationSession.setOutcomeSpace(outcomespace);
		
		// If k is given it is set to the given value, else it will have the initial value
		if (parameters.get("k") != null)
			k = parameters.get("k");
		
		if (parameters.get("min") != null)
			Pmin = parameters.get("min");
		else
			Pmin = negoSession.getMinBidinDomain().getMyUndiscountedUtil();
	
		if (parameters.get("max") != null) {
			Pmax= parameters.get("max");
		} else {
			BidDetails maxBid = negoSession.getMaxBidinDomain();
			Pmax = maxBid.getMyUndiscountedUtil();
		}
		
		this.opponentModel = model;
		this.omStrategy = oms;
		
		// Initialize bidding history
		biddingHistory = new BidHistory();
		
	}

	@Override
	public BidDetails determineOpeningBid() {
		// We can do something better here...
		double time = negotiationSession.getTime();
		BidDetails openingBid = negotiationSession.getOutcomeSpace().getBidNearUtility(0.9);
		
		Log.sln("openingBid = " + openingBid.toString());
		return openingBid;
		//return determineNextBid();
	}

	/**
	 * Simple offering strategy which retrieves the target utility
	 * and looks for the nearest bid if no opponent model is specified.
	 * If an opponent model is specified, then the agent return a bid according
	 * to the opponent model strategy.
	 */
	@Override
	public BidDetails determineNextBid() {
		
		bidNum++;
		Log.vln("Bid nummer: " + bidNum);
		
		if (bidNum % recomputePar == 0) {
			Log.vln("Ik ga nash product uitrekenen");
			this.computeNash();
			Log.vln("Ik heb de nash product uitgerekend, het is de volgende bid:");
			Log.vln("Mijn utility in Nash: " + nash.getMyUndiscountedUtil());
			Log.vln("Geschatte utility van tegenstander in Nash: " + opponentModel.getBidEvaluation(nash.getBid()));
		}
		
		/* Tom R (2013-12-28)
		 * Some ideas:
		 * 	- NS.getBidHistory() and NS.getOpponentBidHistory() might be useful
		 *  - Class 'ParetoFrontier' can be used to easily compute the PF
		 *  - We have two bid-sorter classes: 'BidDetailsSorterTime' and 'BidDetailsSorterUtility'
		 *  - The 'BidFilter' class has some useful methods for filtering bids on time/utility
		 */
		
		double time = negotiationSession.getTime(); // Normalized time [0,1]
			
		// Determine current negotiation phase
		curPhase = getNegotiationPhase();
		
		getAverageDiffLastNBids (10);

		if (curPhase == 1) {
			// First negotiation phase (implemented by Tom)
			// During the first phase we select random bids.
			Range randBidRange = getRangeFunctionFirstPhase(time, 0.02);
			
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
			biddingHistory.add(bd);
			
			return bd;
			
			
		} else if (curPhase >= 2) {
			// Second negotiation phase (implemented by Arnold)
			
			/* Opponent modelling by Bas */
					
			
			//int opponentClass = 1 for Hardheaded, 2 for Conceder, 3 for random
			
			
			double bestBid = negotiationSession.getOpponentBidHistory().getBestBidDetails().getMyUndiscountedUtil();
			double difference;
			List<BidDetails> lastOpponentBids = negotiationSession.getOpponentBidHistory().sortToTime().getHistory();
			Double lastOwnUtil = negotiationSession.getOwnBidHistory().getLastBidDetails().getMyUndiscountedUtil();
			//Calculate difference between last bid and before last bid
			if (lastOpponentBids.size() > 0){
				if(lastOpponentBids.size() > 10)
					difference = getAverageDiffLastNBids(10);
				else

				difference = lastOpponentBids.get(0).getMyUndiscountedUtil() - lastOpponentBids.get(1).getMyUndiscountedUtil();
				//Log.dln("Difference: " + difference);
				double nextBidUtil;
				
				//The opponent is approaching us in utility
				if (difference>0)
					nextBidUtil = Math.max(lastOwnUtil-(difference*tft1),p(time));
					
				//The opponent is distancing from us in utility
				else
					nextBidUtil = Math.max(lastOwnUtil-(difference*tft2),p(time));
				
				//If there has been a better bid of the opponent, don't go below
				nextBidUtil = Math.max(nextBidUtil, bestBid);
				
				/* Decide bid closest to optimal frontier */				
				Range r = new Range(nextBidUtil-phase2range, nextBidUtil+phase2range);
				
				Double temp = new Double(nextBidUtil);
				Double range2 = new Double(phase2range);

				Log.vln("I want an utility of: " + temp.toString() + " range: " + range2);

				List<BidDetails> bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);

				if (bidsInRange.size() == 0) { // do standard bid because we dont have any choices
				nextBid = outcomespace.getBidNearUtility(nextBidUtil); 
				} 
				else { // do an intelligent bid since we have choices!
				
					Double sizeList = new Double(bidsInRange.size());
					Log.vln("Number of bids found that are in range:" + sizeList.toString());
					
					OpponentBidCompare comparebids = new OpponentBidCompare();
					comparebids.setOpponentModel(opponentModel);
					
					Collections.sort(bidsInRange, comparebids);
					
					Log.vln("Max: " + opponentModel.getBidEvaluation(bidsInRange.get(0).getBid()));
					Log.vln("Min: " + opponentModel.getBidEvaluation(bidsInRange.get(bidsInRange.size()-1).getBid()));
					nextBid = bidsInRange.get(0);
				}
				
				//nextBid = outcomespace.getBidNearUtility(nextBidUtil); // TODO: find bid that opponent likes using OM
				//nextBid = opponentModel.getBid(outcomespace, nextBidUtil);
				Log.s("("+difference + "," + nextBidUtil+"),");
				// Log.inLine(p(time) +", ");
			}
			else{
				nextBid = negotiationSession.getOutcomeSpace().getBidNearUtility(p(time));
			}
			if (nextBid.getMyUndiscountedUtil()>bestBid)
				return nextBid;
			else
				return negotiationSession.getOutcomeSpace().getBidNearUtility(bestBid);
		}
		/*} else if (curPhase == 3) {
			// Final negotiation phase
			// TODO: implemented this based on Acceptance Strategy
			
			
			
			// For now, we just return a random bid...
			return getRandomBid(0.4, 0.6);
			
		}*/
		
		// Never used :-)
		return getRandomBid(0.9, 1.0);
	}
	
	public void computeNash() {
		
		//if 
		
		List<BidDetails> allOutcomes = negotiationSession.getOutcomeSpace().getAllOutcomes();
		double max = -1;
		BidDetails best = null;
		for (BidDetails koe : allOutcomes)
		{
			double myUtil = koe.getMyUndiscountedUtil();
			double enUtil = opponentModel.getBidEvaluation(koe.getBid());
			double prod = myUtil*enUtil;
			if (prod > max) {
				best = koe;
				max = prod;
			}
		}
		
		this.nash = best;
		
		/* This code does not work, I dont know why :(
		UtilitySpace A = opponentModel.getOpponentUtilitySpace();
		if (A == null) {
			Log.vln("Ja dat werkt dus niet, KUT!");
			try {
				this.wait(1000000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			this.bidSpace = new BidSpace(A, this.myUtilSpace, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.par = this.bidSpace.getParetoFrontierBids();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		*/
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
		if (t < this.phaseBoundary[0])
			return 1;
		if (t > this.phaseBoundary[1])
			return 1;
		
		// scale t
		double torig = t;
		t = (t - this.phaseBoundary[0]) / (this.phaseBoundary[1] -  this.phaseBoundary[0]);
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
	
	public BidDetails getRandomBidFirstPhase () {
		
		Random randgen = new Random();
		// Generate random number between 0.9 and 1.
		double utilGoal = 0.9+(randgen.nextInt(1)/100);
		
		BidDetails bd = negotiationSession.getOutcomeSpace().getBidNearUtility(utilGoal);
		return bd;
		
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
	
	public BidDetails getRandomBid (double lb, double ub) {
		Range r = new Range(lb, ub);
		return getRandomBid(r);
	}
	
	/**
	 * Returns the current phase of the negotiation session
	 * based on the elapsed time. Phases are 1, 2 or 3.
	 * 
	 * @return
	 */
	public int getNegotiationPhase () {
		double time = negotiationSession.getTime(); // Normalized time [0,1]
		int p = 0;
		
		if (time <= phaseBoundary[0]) 
			return 1;
		else if (time > phaseBoundary[0] && time <= phaseBoundary[1])
			return 2;
		else if (time > phaseBoundary[1])
			return 3;
		
		return 0;
	}
	
	public Range getRangeFunctionFirstPhase (double t, double margin) {

		double normTime = t/phaseBoundary[0]; // Normalized time
		
		double val = 1-(normTime/10);
		Range r = new Range(val-margin, val+margin);
		
		// Set upper bound to 1 is exceeds
		if (r.getUpperbound() > 1) r.setUpperbound(1.0);
		
		//Log.rln("Calculated range for t = " + normTime + ", ["+r.getLowerbound()+","+r.getUpperbound()+"]");
		
		return r;
	}
	
	public boolean isAlreadyOffered (BidDetails bd) {
		
		List<BidDetails> historyList = biddingHistory.getHistory();
		if (historyList.contains(bd)) return true;
		
		return false;
	}
	
	/**
	 * This method returns the average difference over the last n bids.
	 * Can be used to see the behavior the agent over time.
	 * 
	 * @param n
	 * @return
	 */
	public double getAverageDiffLastNBids (int n) {
		
		if (n > negotiationSession.getOpponentBidHistory().size()) {
			// Not enough bids in history!
			return 0.0;
		}
		
		// Save values
		double[] vals = new double[n];
		
		double avg = 0;
		
		// Get list of opponent bids sorted on time
		List<BidDetails> h = negotiationSession.getOpponentBidHistory().sortToTime().getHistory();
		
		// TODO: Smooth the values
		
		for (int i = 0; i < n; i++) {
			BidDetails bd = h.get(i);
			//Log.rln("Bid at time " + bd.getTime() + " has utility " + bd.getMyUndiscountedUtil());
			vals[i] = bd.getMyUndiscountedUtil();
			avg += vals[i];
		}
		
		avg = avg/n;
		
		double[] smooth = new double[n];
		
		Log.rln("###################################");
		
		// Smoothing kernel 
		double[] kernel = {1.0/6.0, 4.0/6.0, 1.0/6.0};
		
		for (int i = 0; i < n; i++) {
			smooth[i] = applyConvolution(vals, i, kernel);
			Log.rln("Value at index = " + i + " has value " + vals[i] + " and after smoothing " + smooth[i]);
		}
		
		
		//Log.rln("Average concede over last " + n + " bids = " + avg);
		
		
		
		return avg;
	}
	
	/**
	 * Applies convolution kernel k to input array at position x
	 * 
	 * input = 	Double array containing values to be smoothed
	 * x = 		Location where to apply smooth
	 * k = 		Kernel
	 *
	 * @return
	 */
	public double applyConvolution(double[] input, int x, double[] k) {
		
		// Build double array with end values repeated
		double[] toConvolve = new double[input.length+2];
		toConvolve[0] = input[0];
		for (int j = 0; j < input.length; j++) {
			toConvolve[j+1] = input[j];
		}
		toConvolve[input.length+1] = input[input.length-1];
	
		double output = 0;	

		for (int i = 0; i < k.length; i++) {
			output = output + (toConvolve[x+i]*k[i]);
		}
		
		return output;
	}
	
}

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
import negotiator.group7.phases.Phase;
import negotiator.group7.phases.Phase1;
import negotiator.group7.phases.Phase2;
import negotiator.group7.phases.Phase3;
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
	private double[] phaseBoundary = {0.05, 1};
	private double   phase1LowerBound = 0.8;
	private double   phase1UpperBound = 1.0;
	private double   phase2LowerBound = 0.6;
	private double   phase2range = 0.05;

	
	/** Keep track of the current phase */
	private int curPhase = 0;
	
	/** Tit-for-tat parameters: tft1 is amount of approaching, tft2 is amount of distancing*/
	double tft1 = 0.2;
	double tft2 = 0.75;
	
	/** Pareto frontier needs bidspace and utility space to be computed */
	private BidSpace bidSpace; // (not implemented, does not work :( )
	private UtilitySpace myUtilSpace;
	private List<Bid> par; // parato frontier (not implemented)
	private int bidNum = 0; // current bid number
	private int recomputePar = 500; // after every 500 bids recompute the pareto frontier
	private BidDetails nash; // nash product
	
	private Phase phase;
	
	
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
		int newPhase = getNegotiationPhase();
		if (newPhase != curPhase)
		{
			Log.dln("Switching to phase " + newPhase);
			if (newPhase == 1)
				this.phase = new Phase1(this.negotiationSession, this.opponentModel, 0.0, phaseBoundary[0], this.phase1LowerBound, this.phase1UpperBound);
			if (newPhase == 2)
				this.phase = new Phase2(this.negotiationSession, this.opponentModel, this.phaseBoundary[0], this.phaseBoundary[1], 
						tft1, tft2, this.k, this.e, this.phaseBoundary, this.phase2LowerBound, this.phase2range,
						this.outcomespace);
			if (newPhase == 3)
				this.phase = new Phase3(this.negotiationSession, this.opponentModel, this.phaseBoundary[1], 1.0);
			curPhase = newPhase;
		}
		// Prevent NullPointer exceptions
		if (this.phase == null)
		{
			this.phase = new Phase1(this.negotiationSession, this.opponentModel, 0.0, phaseBoundary[0], 
					this.phase1LowerBound, this.phase1UpperBound);
			Log.newLine("Error: Phase is null. Initialized new phase1...");
		}
		return this.phase.determineNextBid();

		/*} else if (curPhase == 3) {
			// Final negotiation phase
			// TODO: implemented this based on Acceptance Strategy
			
			
			
			// For now, we just return a random bid...
			return getRandomBid(0.4, 0.6);
			
		}*/
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
	
//	public boolean isAlreadyOffered(BidDetails bd) {
//		
//		List<BidDetails> historyList = biddingHistory.getHistory();
//		if (historyList.contains(bd)) return true;
//		
//		return false;
//	}
	

	
}

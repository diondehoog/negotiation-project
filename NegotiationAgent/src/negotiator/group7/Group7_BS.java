package negotiator.group7;

import java.util.HashMap;

import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OMStrategy;
import negotiator.boaframework.OfferingStrategy;
import negotiator.boaframework.OpponentModel;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.boaframework.opponentmodel.NoModel;

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
	private double k;
	/** Maximum target utility */
	private double Pmax;
	/** Minimum target utility */
	private double Pmin;
	/** Concession factor */
	private double e;
	/** Outcome space */
	SortedOutcomeSpace outcomespace;
	
	/** Phase boundaries */
	private double[] phaseBoundary = {0.2, 0.8};
	
	/**
	 * Empty constructor used for reflexion. Note this constructor assumes that init
	 * is called next.
	 */
	public Group7_BS(){}
	
	public Group7_BS(NegotiationSession negoSession, OpponentModel model, OMStrategy oms, double e, double k, double max, double min){
		this.e = e;		// Concession factor
		this.k = k;		
		this.Pmax = max;	// Max target utility
		this.Pmin = min;	// Min target utility
		
		this.negotiationSession = negoSession;
		outcomespace = new SortedOutcomeSpace(negotiationSession.getUtilitySpace());
		negotiationSession.setOutcomeSpace(outcomespace);
		
		this.opponentModel = model;	// Opponent model
		this.omStrategy = oms;		// Opponent strategy
	}
	
	/**
	 * Method which initializes the agent by setting all parameters.
	 * The parameter "e" is the only parameter which is required (concession factor).
	 */
	public void init(NegotiationSession negoSession, OpponentModel model, OMStrategy oms, HashMap<String, Double> parameters) throws Exception {
		
		// All the parameters are given as HashMap<String,Double>
		if (parameters.get("e") != null) {
			this.negotiationSession = negoSession;
			
			outcomespace = new SortedOutcomeSpace(negotiationSession.getUtilitySpace());
			negotiationSession.setOutcomeSpace(outcomespace);
			
			this.e = parameters.get("e");
			
			// Check is k is given, if not, set k=0 which means start with a bid with maximum utility
			if (parameters.get("k") != null)
				this.k = parameters.get("k");
			else
				this.k = 0;
			
			if (parameters.get("min") != null)
				this.Pmin = parameters.get("min");
			else
				this.Pmin = negoSession.getMinBidinDomain().getMyUndiscountedUtil();
		
			if (parameters.get("max") != null) {
				Pmax= parameters.get("max");
			} else {
				BidDetails maxBid = negoSession.getMaxBidinDomain();
				Pmax = maxBid.getMyUndiscountedUtil();
			}
			
			this.opponentModel = model;
			this.omStrategy = oms;
			
		} else {
			throw new Exception("Constant \"e\" for the concession speed was not set.");
		}
	}

	@Override
	public BidDetails determineOpeningBid() {
		// We can do something better here...
		
		
		
		
		return determineNextBid();
	}

	/**
	 * Simple offering strategy which retrieves the target utility
	 * and looks for the nearest bid if no opponent model is specified.
	 * If an opponent model is specified, then the agent return a bid according
	 * to the opponent model strategy.
	 */
	@Override
	public BidDetails determineNextBid() {
		
		/* Tom R (2013-12-28)
		 * Some ideas:
		 * 	- NS.getBidHistory() and NS.getOpponentBidHistory() might be useful
		 *  - Class 'ParetoFrontier' can be used to easily compute the PF
		 *  - We have two bid-sorter classes: 'BidDetailsSorterTime' and 'BidDetailsSorterUtility'
		 *  - The 'BidFilter' class has some useful methods for filtering bids on time/utility
		 */
		
		
		double time = negotiationSession.getTime(); // Normalized time [0,1]
		
		if (time < phaseBoundary[0]) {
			// Negotiation Phase 1
		}
		
		
		// Calculate the utility goal by using p(t)
		double utilityGoal;
		utilityGoal = p(time);
		
		// System.out.println("[e=" + e + ", Pmin = " + BilateralAgent.round2(Pmin) + "] t = " + 
		//					  BilateralAgent.round2(time) + ". Aiming for " + utilityGoal);
		
		// if there is no opponent model available
		if (opponentModel instanceof NoModel) {
			// Opponent model NOT available
			// Use to utilityGoal to get the nearest bid in the outcome space
			nextBid = negotiationSession.getOutcomeSpace().getBidNearUtility(utilityGoal);
		} else {
			// Opponent Model IS available
			// Base the next bid on the OM and outcome space
			nextBid = omStrategy.getBid(outcomespace, utilityGoal);
		}
		return nextBid;
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
	 * For e = 0 (special case), it will behave as a Hardliner.
	 */
	public double f(double t)
	{
		if (e == 0)
			return k;
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
		return Pmin + (Pmax - Pmin) * (1 - f(t));
	}
	
	/**
	 * 	- Opdelen in drie fases op basis van tijd (en later ook van discount)
	 *  - Met hoge biedeingen beginnen in eerste fase (+90%)
	 *  - De reactie van hoge biedingen gebruiken om strategie/preference van opponent te bepalen
	 *  
	 *  - Combineren van twee strategien:
	 *  	- Conceder --> Hard-Headed
	 *  	- Hard-Headed --> Tit-for-That
	 * 
	 * 	- Fase 1: hoge biedingen 90%
	 *  - Fase 2: afhankelijk van strategy HH/TfT
	 *  - Fase 3: acceptance strategy
	 *  
	 *  Nash Point schatten a.d.h. preference profile
	 *  Paar functies (sin/exp) implementeren en kijken hoe je de OS fuckt
	 *  
	 *  Runnen: saven in Eclipse, class file in Genius laden (TomV maakt XML)
	 *  
	 */
}
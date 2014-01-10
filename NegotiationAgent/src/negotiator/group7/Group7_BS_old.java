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
 * @author Alex Dirkzwager, Mark Hendrikx
 */
public class Group7_BS_old extends OfferingStrategy {

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
	
	/**
	 * Method which initializes the agent by setting all parameters.
	 * The parameter "e" is the only parameter which is required.
	 */
	public void init(NegotiationSession negoSession, OpponentModel model, OMStrategy oms, HashMap<String, Double> parameters) throws Exception {
		if (parameters.get("e") != null) {
			this.negotiationSession = negoSession;
			
			outcomespace = new SortedOutcomeSpace(negotiationSession.getUtilitySpace());
			negotiationSession.setOutcomeSpace(outcomespace);
			
			this.e = parameters.get("e");
			
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
		double time = negotiationSession.getTime();
		double utilityGoal;
		utilityGoal = Pmax;
		
		// if there is no opponent model available
		if (opponentModel instanceof NoModel) {
			nextBid = negotiationSession.getOutcomeSpace().getBidNearUtility(utilityGoal);
		} else {
			nextBid = omStrategy.getBid(outcomespace, utilityGoal);
		}
		return nextBid;
	}
}
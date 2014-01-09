package negotiator.group7;

import java.util.ArrayList;
import java.util.HashMap;

import negotiator.boaframework.AcceptanceStrategy;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OfferingStrategy;
import negotiator.boaframework.OutcomeTuple;
import negotiator.boaframework.acceptanceconditions.other.AC_Next;
import negotiator.boaframework.acceptanceconditions.other.Multi_AcceptanceCondition;

/**
 * This Acceptance Condition will accept an opponent bid if the utility is
 * higher than the bid the agent is ready to present
 * 
 * Decoupling Negotiating Agents to Explore the Space of Negotiation Strategies
 * T. Baarslag, K. Hindriks, M. Hendrikx, A. Dirkzwager, C.M. Jonker
 * 
 * @author Alex Dirkzwager, Mark Hendrikx
 * @version 18/12/11
 */
public class Group7_AS extends Multi_AcceptanceCondition {

	//private double a;
	//private double b;

	/**
	 * Empty constructor for the BOA framework.
	 * 
	 * @return
	 */
	public Group7_AS() {
	}

//	public Group7_AS(NegotiationSession negoSession, OfferingStrategy strat,
//			double alpha, double beta) {
//		this.negotiationSession = negoSession;
//		this.offeringStrategy = strat;
//		this.a = alpha;
//		this.b = beta;
//	}

	@Override
	public void init(NegotiationSession negoSession, OfferingStrategy strat,
			HashMap<String, Double> parameters) throws Exception {
		this.negotiationSession = negoSession;
		this.offeringStrategy = strat;

//		if (parameters.get("a") != null || parameters.get("b") != null) {
//			a = parameters.get("a");
//			b = parameters.get("b");
//		} else {
//			a = 1;
//			b = 0;
//		}

		outcomes = new ArrayList<OutcomeTuple>();
		ACList = new ArrayList<AcceptanceStrategy>();
		for (int e = 0; e < 50; e++) {
			ACList.add(new AC_Next(negotiationSession, offeringStrategy, 1,
					e * 0.01));
		}
	}
/*
	@Override
	public String printParameters() {
		String str = "[a: " + a + " b: " + b + "]";
		return str;
	}

	@Override
	public Actions determineAcceptability() {
		for (int e = 0; e < 5; e++) {
			ACList.get(e).determineAcceptability();
		}
		return Actions.Reject;
	}*/
}
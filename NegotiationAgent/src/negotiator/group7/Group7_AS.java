package negotiator.group7;

import java.util.HashMap;

import negotiator.BidHistory;
import negotiator.boaframework.AcceptanceStrategy;
import negotiator.boaframework.Actions;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OfferingStrategy;

/**
 * Checking all different acceptance strategies
 * 
 * @author Olivier Hokke
 * @version 13/01/14
 */
public class Group7_AS extends AcceptanceStrategy {

	/**
	 * Empty constructor for the BOA framework.
	 * 
	 * @return
	 */
	public Group7_AS() {
	}

	public Group7_AS(NegotiationSession negoSession, OfferingStrategy strat) {
		this.negotiationSession = negoSession;
		this.offeringStrategy = strat;
	}

	@Override
	public void init(NegotiationSession negoSession, OfferingStrategy strat,
			HashMap<String, Double> parameters) throws Exception {
		this.negotiationSession = negoSession;
		this.offeringStrategy = strat;

//		outcomes = new ArrayList<OutcomeTuple>();
//		ACList = new ArrayList<AcceptanceStrategy>();
//
//		ACList.add(new AC_ABMP(negotiationSession, offeringStrategy));
//		ACList.add(new AC_AgentFSEGA(negotiationSession, offeringStrategy));
//		ACList.add(new AC_AgentK(negotiationSession, offeringStrategy));
//		ACList.add(new AC_AgentK2(negotiationSession, offeringStrategy));
//		ACList.add(new AC_AgentLG(negotiationSession, offeringStrategy));
//		ACList.add(new AC_AgentMR(negotiationSession, offeringStrategy));
//		ACList.add(new AC_AgentSmith(negotiationSession, offeringStrategy));
//		ACList.add(new AC_BRAMAgent(negotiationSession, offeringStrategy));
//		ACList.add(new AC_BRAMAgent2(negotiationSession, offeringStrategy));
//		ACList.add(new AC_CUHKAgent(negotiationSession, offeringStrategy));
//		ACList.add(new AC_False());
//		ACList.add(new AC_Gahboninho(negotiationSession, offeringStrategy));
//		ACList.add(new AC_HardHeaded(negotiationSession, offeringStrategy));
//		ACList.add(new AC_IAMcrazyHaggler(negotiationSession, offeringStrategy));
//		ACList.add(new AC_IAMHaggler2010(negotiationSession, offeringStrategy));
//		ACList.add(new AC_IAMHaggler2011(negotiationSession, offeringStrategy));
//		//ACList.add(new AC_IAMHaggler2012(negotiationSession, offeringStrategy));
//		ACList.add(new AC_NiceTitForTat(negotiationSession, offeringStrategy));
//		ACList.add(new AC_Nozomi(negotiationSession, offeringStrategy));
//		ACList.add(new AC_OMACagent(negotiationSession, offeringStrategy));
//		ACList.add(new AC_TheNegotiator(negotiationSession, offeringStrategy));
//		//ACList.add(new AC_ValueModelAgent(negotiationSession, offeringStrategy));
//		ACList.add(new AC_Yushu(negotiationSession, offeringStrategy));
// 
//		// See page 112 & 119 in Complex Automated Negotiations - Theories, Models, and Software Competitions
//
//		ACList.add(new AC_Next(negotiationSession, offeringStrategy, 1.02, 0.0));
//		ACList.add(new AC_Next(negotiationSession, offeringStrategy, 1.0, 0.0));
//		ACList.add(new AC_Previous(negotiationSession, 1.02, 0.0));
//		ACList.add(new AC_Previous(negotiationSession, 1.0, 0.0));
//		
//		ACList.add(new AC_Gap(negotiationSession, 0.02));
//		ACList.add(new AC_Gap(negotiationSession, 0.05));
//		ACList.add(new AC_Gap(negotiationSession, 0.1));
//		ACList.add(new AC_Gap(negotiationSession, 0.2));
//
//		ACList.add(new AC_Const(negotiationSession, 0.1));
//		ACList.add(new AC_Const(negotiationSession, 0.2));
//		ACList.add(new AC_Const(negotiationSession, 0.3));
//		ACList.add(new AC_Const(negotiationSession, 0.4));
//		ACList.add(new AC_Const(negotiationSession, 0.5));
//		ACList.add(new AC_Const(negotiationSession, 0.6));
//		ACList.add(new AC_Const(negotiationSession, 0.7));
//		ACList.add(new AC_Const(negotiationSession, 0.8));
//		ACList.add(new AC_Const(negotiationSession, 0.9));
//		ACList.add(new AC_ConstDiscounted(negotiationSession, 0.8));
//		ACList.add(new AC_ConstDiscounted(negotiationSession, 0.9));
//		
//		ACList.add(new AC_Time(negotiationSession, 0.99));
//		
//		ACList.add(new AC_CombiAvg(negotiationSession, offeringStrategy, 0.99));
//		ACList.add(new AC_CombiBestAvg(negotiationSession, offeringStrategy, 0.99));
//		ACList.add(new AC_CombiBestAvgDiscounted(negotiationSession, offeringStrategy, 0.99));
//		ACList.add(new AC_CombiMax(negotiationSession, offeringStrategy,0.99));
//		ACList.add(new AC_CombiMaxInWindow(negotiationSession, offeringStrategy, 0.99));
//		ACList.add(new AC_CombiMaxInWindowDiscounted(negotiationSession, offeringStrategy, 0.99));
//		ACList.add(new AC_CombiProb(negotiationSession, offeringStrategy, 0.99));
//		ACList.add(new AC_CombiProbDiscounted(negotiationSession, offeringStrategy, 0.99));

		//ACList.add(new AC_TheNegotiatorReloaded(negotiationSession, offeringStrategy, a ,b, ad, bd, c, t));
		//ACList.add(new AC_CombiV2(negotiationSession, offeringStrategy, a, b, t, c, d));
		//ACList.add(new AC_CombiV3(negotiationSession, offeringStrategy, a, b, t, c));
		//ACList.add(new AC_CombiV4(negotiationSession, offeringStrategy, a, b, c, d, e));
		//ACList.add(new AC_Combi(negotiationSession, offeringStrategy, a, b, t, c));
		
		//ACList.add(new AC_MAC()); // this is the class that we extend..
	}

	static double averageDeltaTime = 0;
	static double time = 0;
	double previousTime = 0;
	int counter = 0;
	
	public static int GetGuessedBidsLeft() {
		return (int) ((1.0 - time) / (averageDeltaTime + 0.00001)); // avoiding division by zero :P
	}
	
	public Actions determineAcceptability() {
		if (negotiationSession == null) return Actions.Reject;
		if (negotiationSession.getOwnBidHistory() == null) return Actions.Reject;
		if (negotiationSession.getOpponentBidHistory() == null) return Actions.Reject;
		if (negotiationSession.getOwnBidHistory().getWorstBidDetails() == null) return Actions.Reject;
		if (negotiationSession.getOpponentBidHistory().getWorstBidDetails() == null) return Actions.Reject;

		// Averaging execution time
		counter++;
		time = negotiationSession.getTime();
		double dt = time - previousTime;
		if (previousTime != 0) {
			if (averageDeltaTime == 0) {
				averageDeltaTime = dt;
			} else {
				averageDeltaTime = (9 * averageDeltaTime + dt) / 10;
			}
		}
		previousTime = time;
		int bidsLeft = GetGuessedBidsLeft();
		int totalBids = counter + bidsLeft;

//		Actions a = super.determineAcceptability();
		BidHistory bh = negotiationSession.getOpponentBidHistory();
		double hisLast = bh.getHistory().get(0).getMyUndiscountedUtil();
		double hisBest = bh.getBestBidDetails().getMyUndiscountedUtil();
		double ourWorst = negotiationSession.getOwnBidHistory().getWorstBidDetails().getMyUndiscountedUtil();
		double ourWorstFixed = Math.max(ourWorst, (1 - time) * 0.5 + 0.5);
		
		// This curve is build to gradually become lower with increasing speed so that at the end we are more allowing
		double acceptCurve = 0.95 * Math.pow((double)bidsLeft/(double)totalBids, 0.025);
		
		// TODO: let accept curve approach the NASH!!

		//Log.hln("acceptCurve: " + acceptCurve);

		if (hisLast > acceptCurve) {
			Log.newLine("~~~~~~~~~~~ hisLast > acceptCurve ==> hislast: " + hisLast + "; acceptCurve: " + acceptCurve);
			// Here we simply accept anything above the accept curve
			return Actions.Accept;
		} /*else if (bidsLeft < 4 && hisLast == hisBest) {
			Log.newLine("~~~~~~~~~~~ bidsLeft < 4");
			// Here we accept anything, once only 4 bids are left
			return Actions.Accept;
		} else if (hisLast > ourWorstFixed) {
			Log.newLine("~~~~~~~~~~~ hisLast > ourWorstFixed ==> hislast: " + hisLast + "; ourWorstFixed: " + ourWorstFixed);
			// If he bids higher than our worst, we will simply accept right away
			// and since we don't go down with our utility that quickly, this should work fine
			return Actions.Accept;
		}*/
		return Actions.Reject;
	}
}
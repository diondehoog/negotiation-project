package negotiator.group7;

import java.util.ArrayList;
import java.util.HashMap;

import negotiator.boaframework.AcceptanceStrategy;
import negotiator.boaframework.Actions;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OfferingStrategy;
import negotiator.boaframework.OutcomeTuple;
import negotiator.boaframework.acceptanceconditions.anac2010.AC_AgentFSEGA;
import negotiator.boaframework.acceptanceconditions.anac2010.AC_AgentK;
import negotiator.boaframework.acceptanceconditions.anac2010.AC_AgentSmith;
import negotiator.boaframework.acceptanceconditions.anac2010.AC_IAMHaggler2010;
import negotiator.boaframework.acceptanceconditions.anac2010.AC_IAMcrazyHaggler;
import negotiator.boaframework.acceptanceconditions.anac2010.AC_Nozomi;
import negotiator.boaframework.acceptanceconditions.anac2010.AC_Yushu;
import negotiator.boaframework.acceptanceconditions.anac2011.AC_AgentK2;
import negotiator.boaframework.acceptanceconditions.anac2011.AC_BRAMAgent;
import negotiator.boaframework.acceptanceconditions.anac2011.AC_Gahboninho;
import negotiator.boaframework.acceptanceconditions.anac2011.AC_HardHeaded;
import negotiator.boaframework.acceptanceconditions.anac2011.AC_IAMHaggler2011;
import negotiator.boaframework.acceptanceconditions.anac2011.AC_NiceTitForTat;
import negotiator.boaframework.acceptanceconditions.anac2011.AC_TheNegotiator;
import negotiator.boaframework.acceptanceconditions.anac2012.AC_AgentLG;
import negotiator.boaframework.acceptanceconditions.anac2012.AC_AgentMR;
import negotiator.boaframework.acceptanceconditions.anac2012.AC_BRAMAgent2;
import negotiator.boaframework.acceptanceconditions.anac2012.AC_CUHKAgent;
import negotiator.boaframework.acceptanceconditions.anac2012.AC_OMACagent;
import negotiator.boaframework.acceptanceconditions.other.AC_ABMP;
import negotiator.boaframework.acceptanceconditions.other.AC_CombiAvg;
import negotiator.boaframework.acceptanceconditions.other.AC_CombiBestAvg;
import negotiator.boaframework.acceptanceconditions.other.AC_CombiBestAvgDiscounted;
import negotiator.boaframework.acceptanceconditions.other.AC_CombiMax;
import negotiator.boaframework.acceptanceconditions.other.AC_CombiMaxInWindow;
import negotiator.boaframework.acceptanceconditions.other.AC_CombiMaxInWindowDiscounted;
import negotiator.boaframework.acceptanceconditions.other.AC_CombiProb;
import negotiator.boaframework.acceptanceconditions.other.AC_CombiProbDiscounted;
import negotiator.boaframework.acceptanceconditions.other.AC_Const;
import negotiator.boaframework.acceptanceconditions.other.AC_ConstDiscounted;
import negotiator.boaframework.acceptanceconditions.other.AC_False;
import negotiator.boaframework.acceptanceconditions.other.AC_Gap;
import negotiator.boaframework.acceptanceconditions.other.AC_Next;
import negotiator.boaframework.acceptanceconditions.other.AC_Previous;
import negotiator.boaframework.acceptanceconditions.other.AC_Time;
import negotiator.boaframework.acceptanceconditions.other.Multi_AcceptanceCondition;

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

	public Actions determineAcceptability() {
		if (negotiationSession == null) return Actions.Reject;
		if (negotiationSession.getOwnBidHistory() == null) return Actions.Reject;
		if (negotiationSession.getOpponentBidHistory() == null) return Actions.Reject;
		if (negotiationSession.getOwnBidHistory().getWorstBidDetails() == null) return Actions.Reject;
		if (negotiationSession.getOpponentBidHistory().getWorstBidDetails() == null) return Actions.Reject;
		
//		Actions a = super.determineAcceptability();
		double hisLast = negotiationSession.getOpponentBidHistory().getLastBidDetails().getMyUndiscountedUtil();
		double hisBest = negotiationSession.getOpponentBidHistory().getBestBidDetails().getMyUndiscountedUtil();
		double ourWorst = negotiationSession.getOwnBidHistory().getWorstBidDetails().getMyUndiscountedUtil();
		double time = negotiationSession.getTime();
		double ourWorstFixed = Math.max(ourWorst, 1 - time);
		if (hisLast > 0.9) {
			Log.hln("~~~~~~~~~~~ hisLast > 0.9 ==> hislast: " + hisLast);
			return Actions.Accept;
//		} else if (hisBest == hisLast && time > 0.90) {
//			Log.hln("~~~~~~~~~~~ hisBest == hisLast && time > 0.90 ==> hislast: " + hisLast + "; hisBest: " + hisBest + "; time: " + time);
//			return Actions.Accept;
//		} else if (time > 0.995) {
//			Log.hln("~~~~~~~~~~~ time > 0.995");
//			return Actions.Accept;
		} else if (hisLast > ourWorstFixed) {
			Log.hln("~~~~~~~~~~~ hisLast > ourWorstFixed ==> hislast: " + hisLast + "; ourWorstFixed: " + ourWorstFixed);
			return Actions.Accept;
		}
		return Actions.Reject;
	}
}
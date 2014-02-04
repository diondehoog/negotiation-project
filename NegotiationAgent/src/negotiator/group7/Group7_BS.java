package negotiator.group7;

import java.util.HashMap;

import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OMStrategy;
import negotiator.boaframework.OfferingStrategy;
import negotiator.boaframework.OpponentModel;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.group7.phases.Phase;
import negotiator.group7.phases.Phase1;
import negotiator.group7.phases.Phase2;
import negotiator.group7.phases.Phase3;

public class Group7_BS extends OfferingStrategy {
	
	/** Outcome space */
	SortedOutcomeSpace outcomespace;
	
	/** Phase boundaries */
	private double[] phaseBoundary = {0.1, 0.95};
	
	private double Ppareto = 0.5; // probability of offering pareto
	private int averageOver = 5; // how many bids to average over to determine concession of opponent
	private double niceFactor = 0.33; // when opponent concedes, their concession is multiplied by this
	private double Pconcede = 0.05; // probability of conceding to make opponent happy
	private double concedeFactor = 0.3; // amount of distance to concede to KS
	private int concedeSteps = 10; // concession steps taken after eachother
	private double hardcodefix = 1.0;
	
	/** Keep track of the current phase */
	private int curPhase = 0;
	
	private Phase phase;
	
	private Helper ourHelper;
	
	
	public void init(NegotiationSession negoSession, OpponentModel model, OMStrategy oms, HashMap<String, Double> parameters) throws Exception {
		
		double d = negoSession.getDiscountFactor(); 
		
		// rescale boundraries for discounts
		if (parameters.get("discount") != null)
			if (parameters.get("discount") > 0.5)
				if ((d != 1)&&(d != 0)) {
					phaseBoundary[0] = phaseBoundary[0]*d;
					phaseBoundary[1] = phaseBoundary[1]*d;
				}
		
		// make helper class for communication between BOA components
		ourHelper = Helper.get(negotiationSession);
		ourHelper.setBiddingStrategy(this);
		ourHelper.setSession(negoSession);
		
		if (parameters.get("phase2") != null)
			phaseBoundary[0] = parameters.get("phase2"); 
		
		if (parameters.get("phase3") != null)
			phaseBoundary[1] = parameters.get("phase3"); 
		
		negotiationSession = negoSession;
		
		outcomespace = new SortedOutcomeSpace(negotiationSession.getUtilitySpace());
		
		negotiationSession.setOutcomeSpace(outcomespace);
		
		// phase 2 parameters:
		if (parameters.get("Ppareto") != null)
			Ppareto = parameters.get("Ppareto");
		if (parameters.get("averageOver") != null)
			averageOver = parameters.get("averageOver").intValue();
		if (parameters.get("niceFactor") != null)
			niceFactor = parameters.get("niceFactor");
		if (parameters.get("Pconcede") != null)
			Pconcede = parameters.get("Pconcede");
		if (parameters.get("concedeFactor") != null)
			concedeFactor = parameters.get("concedeFactor");
		if (parameters.get("concedeSteps") != null)
			concedeSteps = parameters.get("concedeSteps").intValue();
		if (parameters.get("hardcodefix") != null)
			hardcodefix = parameters.get("hardcodefix");
	
		this.opponentModel = model;
		this.omStrategy = oms;		
	}

	@Override
	public BidDetails determineOpeningBid() {
		// offer best bid:
		BidDetails openingBid = negotiationSession.getOutcomeSpace().getBidNearUtility(1.0);
		Log.sln("openingBid = " + openingBid.toString());
		return openingBid;
	}

	/**
	 * Simple offering strategy which retrieves the target utility
	 * and looks for the nearest bid if no opponent model is specified.
	 * If an opponent model is specified, then the agent return a bid according
	 * to the opponent model strategy.
	 */
	@Override
	public BidDetails determineNextBid() {
		// Determine current negotiation phase
		int newPhase = getNegotiationPhase();
		if (newPhase != curPhase)
		{
			System.out.println("Switching to phase " + newPhase);
			if (newPhase == 1)
				this.phase = new Phase1(this.negotiationSession, this.opponentModel, 0.0, phaseBoundary[0], 0.0, 0.0);
			if (newPhase == 2)
				this.phase = new Phase2(this.negotiationSession, this.opponentModel, this.phaseBoundary[0], this.phaseBoundary[1], 
				this.Ppareto, this.averageOver, this.niceFactor, this.Pconcede, this.concedeFactor, this.concedeSteps, 0, 0, this.phaseBoundary, 0.0, this.hardcodefix,
						this.outcomespace);
			if (newPhase == 3)
				this.phase = new Phase3(this.negotiationSession, this.opponentModel, this.phaseBoundary[1], 1.0);
			curPhase = newPhase;
		}
		// Prevent NullPointer exceptions
		if (this.phase == null)
		{
			this.phase = new Phase1(this.negotiationSession, this.opponentModel, 0.0, phaseBoundary[0], 
					0.0, 0.0);
			Log.newLine("Error: Phase is null. Initialized new phase1...");
		}
		return this.phase.determineNextBid();
	}
	
	/**
	 * Returns the current phase of the negotiation session
	 * based on the elapsed time. Phases are 1, 2 or 3.
	 * 
	 * @return
	 */
	public int getNegotiationPhase () {
		double time = negotiationSession.getTime(); // Normalized time [0,1]
		if (time <= phaseBoundary[0]) 
			return 1;
		else if (time <= phaseBoundary[1])
			return 2;
		else
			return 3;
	}
}

package negotiator.group7.phases;

import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;

public class Phase3 extends Phase{

	public Phase3(NegotiationSession negSession, OpponentModel opponentModel, double phaseStart, double phaseEnd) {
		super(negSession, opponentModel, phaseStart, phaseEnd);
	}

	@Override
	public BidDetails determineNextBid() {
		// Final negotiation phase
		// TODO: implemented this based on Acceptance Strategy
		
		
		
		// For now, we just return a random bid...
		return getRandomBid(0.4, 0.6);
	}

	
	
}

package negotiator.group7.phases;

import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;
import negotiator.group7.Helper;
import negotiator.group7.Log;

public class Phase3 extends Phase {

	public Phase3(NegotiationSession negSession, OpponentModel opponentModel, double phaseStart, double phaseEnd) {
		super(negSession, opponentModel, phaseStart, phaseEnd);
	}

	@Override
	public BidDetails determineNextBid() {
		
		int opponentStrategy = Helper.getOMStrategy().getOpponentModel();
		
		if (opponentStrategy == 1) {
			// Opponent is assumed to be HardHeaded
			Log.rln("Opponent is assumed to be HardHeaded, offering random bid [0.7, 0.8]");
			return getRandomBid(0.7, 0.8);
		} else {
			// Opponent is assumed to be Conceder
			Log.rln("Opponent is assumed to be Conceder, offering KS point");
			return Helper.getKalaiPoint();
		}
		
	}
}
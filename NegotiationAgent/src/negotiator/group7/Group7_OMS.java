package negotiator.group7;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OMStrategy;
import negotiator.boaframework.OpponentModel;

/**
 * This class uses an opponent model to determine the next bid for the opponent, while taking
 * the opponent's preferences into account. The opponent model is used to select the best bid.
 *
 * @author Mark Hendrikx
 */
public class Group7_OMS extends OMStrategy {

	/**  when to stop updating the opponentmodel. Note that this value
	 * 	 is not exactly one as a match sometimes lasts slightly longer. */
	double updateThreshold = 1.1;
	
	/**
	 * Empty constructor for the BOA framework.
	 */
	public Group7_OMS() {
		Helper.setOMStrategy(this);
	}

	/**
	 * Normal constructor used to initialize the BestBid opponent model strategy.
	 * @param negotiationSession symbolizing the negotiation state.
	 * @param model used by the opponent model strategy.
	 */
	public Group7_OMS(NegotiationSession negotiationSession, OpponentModel model) {
		try {
			super.init(negotiationSession, model);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Helper.setOMStrategy(this);
	}
	
	/**
	 * Initializes the opponent model strategy. If a value for the parameter t is given, then
	 * it is set to this value. Otherwise, the default value is used.
	 * 
	 * @param negotiationSession state of the negotiation.
	 * @param model opponent model used in conjunction with this opponent modeling strategy.
	 * @param parameters set of parameters for this opponent model strategy.
	 */
	public void init(NegotiationSession negotiationSession, OpponentModel model, HashMap<String, Double> parameters) throws Exception {
		super.init(negotiationSession, model);
		if (parameters.get("t") != null) {
			updateThreshold = parameters.get("t").doubleValue();
		} else {
			Log.dln("OMStrategy assumed t = 1.1");
		}
		Helper.setOMStrategy(this);
	}
	
	/**
	 * Returns the best bid for the opponent given a set of similarly
	 * preferred bids.
	 * 
	 * @param list of the bids considered for offering.
	 * @return bid to be offered to opponent.
	 */
	@Override
	public BidDetails getBid(List<BidDetails> allBids) {
		
		// 1. If there is only a single bid, return this bid
		if (allBids.size() == 1) {
			return allBids.get(0);
		}
		double bestUtil = -1;
		BidDetails bestBid = allBids.get(0);
		
		// 2. Check that not all bids are assigned at utility of 0
		// to ensure that the opponent model works. If the opponent model
		// does not work, offer a random bid.
		boolean allWereZero = true;
		// 3. Determine the best bid
		for (BidDetails bid : allBids) {
			double evaluation = model.getBidEvaluation(bid.getBid());
			if (evaluation > 0.0001) {
				allWereZero = false;
			}
			if (evaluation > bestUtil) {
				bestBid = bid;
				bestUtil = evaluation;
			}
		}
		// 4. The opponent model did not work, therefore, offer a random bid.
		if (allWereZero) {
			Random r = new Random();
			return allBids.get(r.nextInt(allBids.size()));
		}
		return bestBid;
	}

	/**
	 * The opponent model may be updated, unless the time is higher
	 * than a given constant.
	 * 
	 * @return true if model may be updated.
	 */
	@Override
	public boolean canUpdateOM() {
		return negotiationSession.getTime() < updateThreshold;
	}
	
	/**
	 * Returns 1 if the opponent is HardHeaded
	 * Returns 2 if the opponent is Conceder
	 * 
	 * @return
	 */
	public int getOpponentModel() {

		try {
			// Fetch the bid history of the opponent
			BidHistory opponentHist = negotiationSession.getOpponentBidHistory();
			
			// Load the list of distinct bids from the Helper
			List<Bid> distinctBids = Helper.getDistinctBids(opponentHist);
			
			// Number of total opponent bids
			double numBids = 		 (double)opponentHist.size();
			double numDistinctBids = (double)distinctBids.size();
			
			// Threshold percentage for conceder/hardheaded
			double threshold = 0.03;
			
			double ratio = (double)numDistinctBids/numBids;
			
			// Ratio ~0.05 for IAmHaggler == Conceder
			if (ratio < threshold) {
				// There is a small percentage of distinct bids,
				// therefore we classify the opponent as HardHeaded
				Log.rln("Opponent is classified as HardHeaded [" + ratio + "]");
				return 1;
			} else {
				// The percentage of distinct bids is large,
				// therefore we classify the opponent as Conceder
				Log.rln("Opponent is classified as Conceder [" + ratio + "]");
				return 2;
			}
		} catch (Exception e) {
			Log.newLine("WARNING: unable to determine opponent model...");
			return -1;
		}
	}
	
	public boolean isOpponentHardHeaded () {
		boolean ret = (getOpponentModel() == 1) ? true : false;
		return ret;
	}
	
	public boolean isOpponentConceder () {
		boolean ret = (getOpponentModel() == 2) ? true : false;
		return ret;
	}
}
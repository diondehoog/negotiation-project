package negotiator.group7;

import java.util.HashMap;

import negotiator.Bid;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;
import negotiator.boaframework.opponentmodel.HardHeadedFrequencyModel;
import negotiator.boaframework.opponentmodel.NashFrequencyModel;
import negotiator.boaframework.opponentmodel.ScalableBayesianModel;
import negotiator.issue.Issue;
import negotiator.utility.UtilitySpace;

/**
 * Group 7 oppononent model. Depending on the bidding space size, we choose either frequency modeling (for a large bidding space),
 * or Bayesian modeling if we have a relatively small bidding space. Thi is done for performance reasons. 
 */
public class Group7_OM extends OpponentModel {

	/**
	 * The maximum size of the bidding space for which we use Bayesian modelling
	 */
	private final static long biddingSpaceThreshold = 10000;
	
	private long biddingSpaceSize;
	
	private OpponentModel curOM;
	
	private HashMap<String, Double> parameters;
	
	/**
	 * Initializes the utility space of the opponent such that all value
	 * issue weights are equal.
	 */
	@Override
	public void init(NegotiationSession negotiationSession, HashMap<String, Double> parameters) throws Exception {
		this.negotiationSession = negotiationSession;
		this.parameters = parameters;
		initializeModel();
		initializeOpponentModeller();
	}
	
	@Override
	public void init(NegotiationSession negotiationSession)
	{
			try {
				init(negotiationSession, new HashMap<String, Double>());
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	private void initializeModel(){
		opponentUtilitySpace = new UtilitySpace(negotiationSession.getDomain());
		biddingSpaceSize = opponentUtilitySpace.getDomain().getNumberOfPossibleBids();
	}
	
	private void initializeOpponentModeller()
	{
		if (biddingSpaceSize > biddingSpaceThreshold)
		{
			curOM = new NashFrequencyModel();
		} else {
			curOM = new ScalableBayesianModel();
		}
		try {
			curOM.init(negotiationSession, parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//setOpponentUtilitySpace(opponentUtilitySpace);
	}
	
	@Override
	public void updateModel(Bid opponentBid) {
		curOM.updateModel(opponentBid);
	}
	
	/**
	 * Updates the opponent model given a bid.
	 */
	@Override
	public void updateModel(Bid opponentBid, double time) {		
		curOM.updateModel(opponentBid, time);
	}

	@Override
	public double getBidEvaluation(Bid bid) {
		return curOM.getBidEvaluation(bid);
	}
	
	@Override
	public UtilitySpace getOpponentUtilitySpace() {
		return curOM.getOpponentUtilitySpace();
	}
	
	@Override
	public void setOpponentUtilitySpace(negotiator.protocol.BilateralAtomicNegotiationSession fNegotiation)
	{
		curOM.setOpponentUtilitySpace(fNegotiation);
	}
	
	@Override
	public void setOpponentUtilitySpace(UtilitySpace opponentUtilitySpace) {
		curOM.setOpponentUtilitySpace(opponentUtilitySpace);
	}
	
	@Override
	public double getWeight(Issue issue) {
		return curOM.getWeight(issue);
	}
	
	@Override
	public double[] getIssueWeights() {
		return curOM.getIssueWeights();
	}
	
	@Override
	public void cleanUp() {
		curOM.cleanUp();
	}
	
	@Override
	public boolean isCleared() {
		return curOM.isCleared();
	}
	
	@Override
	public String getName() {
		return "Group 7 Flexible Opponent Model";
	}
}
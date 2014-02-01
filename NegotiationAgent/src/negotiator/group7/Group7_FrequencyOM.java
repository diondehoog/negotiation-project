package negotiator.group7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Objective;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;
import negotiator.utility.UtilitySpace;

public class Group7_FrequencyOM extends OpponentModel {


	// the learning coefficient is the weight that is added each turn to the issue weights
	// which changed. It's a trade-off between concession speed and accuracy.
	private double learnCoef;
	// value which is added to a value if it is found. Determines how fast
	// the value weights converge.
	private int learnValueAddition;
	private int amountOfIssues;
	
	private final double meanBidSkip = 1d;
	private final double rightMargin = 0.05;
	private final double leftMargin = 0.10;
	private final int maxLearnValueAddition = 10;
	
	/**
	 * Initializes the utility space of the opponent such that all value
	 * issue weights are equal.
	 */
	@Override
	public void init(NegotiationSession negotiationSession, HashMap<String, Double> parameters) throws Exception {
		this.negotiationSession = negotiationSession;
		if (parameters != null && parameters.get("l") != null) {
			learnCoef = parameters.get("l");
		} else {
			learnCoef = 0.2;
		}
		learnValueAddition = 1;
		initializeModel();
	}
	
	private void initializeModel(){
		opponentUtilitySpace = new UtilitySpace(negotiationSession.getUtilitySpace());
		amountOfIssues = opponentUtilitySpace.getDomain().getIssues().size();
		double commonWeight = 1D / (double)amountOfIssues;    
		
		// initialize the weights
		for(Entry<Objective, Evaluator> e: opponentUtilitySpace.getEvaluators()){
			// set the issue weights
			opponentUtilitySpace.unlock(e.getKey());
			e.getValue().setWeight(commonWeight);
			try {
				// set all value weights to one (they are normalized when calculating the utility)
				for(ValueDiscrete vd : ((IssueDiscrete)e.getKey()).getValues())
					((EvaluatorDiscrete)e.getValue()).setEvaluation(vd,1);  
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Determines the difference between bids. For each issue, it is determined if the
	 * value changed. If this is the case, a 1 is stored in a hashmap for that issue, else a 0.
	 * 
	 * @param a bid of the opponent
	 * @param another bid
	 * @return
	 */
	private HashMap<Integer, Integer> determineDifference(BidDetails first, BidDetails second){
		
		HashMap<Integer, Integer> diff = new HashMap<Integer, Integer>();
		try{
			for(Issue i : opponentUtilitySpace.getDomain().getIssues()){
				diff.put(i.getNumber(), (
						((ValueDiscrete)first.getBid().getValue(i.getNumber())).equals((ValueDiscrete)second.getBid().getValue(i.getNumber()))
						 				)?0:1);
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
		return diff;
	}
	
	/**
	 * Updates the opponent model given a bid.
	 */
	@Override
	public void updateModel(Bid opponentBid, double time) {		
		if(negotiationSession.getOpponentBidHistory().size() < 2) {
			return;
		}
		int numberOfUnchanged = 0;
		BidDetails oppBid = negotiationSession.getOpponentBidHistory().getLastBidDetails();
		BidDetails prevOppBid = negotiationSession.getOpponentBidHistory().getHistory().get(negotiationSession.getOpponentBidHistory().size() - 2);
		HashMap<Integer, Integer> lastDiffSet = determineDifference(prevOppBid, oppBid);
		
		// count the number of changes in value
		for(Integer i: lastDiffSet.keySet()){
			if(lastDiffSet.get(i) == 0)
				numberOfUnchanged ++;
		}
		
		// This is the value to be added to weights of unchanged issues before normalization. 
		// Also the value that is taken as the minimum possible weight, (therefore defining the maximum possible also). 
		double goldenValue = learnCoef / (double)amountOfIssues;
		// The total sum of weights before normalization.
		double totalSum = 1D + goldenValue * (double)numberOfUnchanged;
		// The maximum possible weight
		double maximumWeight = 1D - learnCoef / totalSum; 
		
		
		
		// re-weighing issues while making sure that the sum remains 1 
		for(Integer i: lastDiffSet.keySet()){
			if (lastDiffSet.get(i) == 0 && opponentUtilitySpace.getWeight(i)< maximumWeight)
				opponentUtilitySpace.setWeight(opponentUtilitySpace.getDomain().getObjective(i), (opponentUtilitySpace.getWeight(i) + goldenValue)/totalSum);
			else
				opponentUtilitySpace.setWeight(opponentUtilitySpace.getDomain().getObjective(i), opponentUtilitySpace.getWeight(i)/totalSum);
		}
		
		try {
			List<BidDetails> distinctBids = getDistinctBids(negotiationSession.getOpponentBidHistory());
			double curUtil = this.getBidEvaluation(oppBid.getBid());
			double expectedUtil = ExpectedNewBidUtil();
			int actualLearnRate = learnValueAddition;
			if (!distinctBids.contains(oppBid) && (curUtil < expectedUtil - rightMargin || curUtil > expectedUtil + leftMargin)) { // We have a new original bid!
				// Algebra to find the new learnValueAddition (where w_i is the weight of issue i, and v_i,j is the value item j from issue i:
				// U(offer) = sum_i(w_i (v_{i,j}/sum_j(v_{i,j}))
				// Frequency modeling will add the following:
				// U(offer) = sum_i(w_i (v_{i,j} + x)/(sum_j(v_{i,j}) + x))
				// However this is a bit hard to solve algebraically, so we just try some values for x and choose the best one.
				double closestUtil = 0;
				// Estimate the utility for all possible learn rates
				for (int i = 1; i <= maxLearnValueAddition; i++) {
					double estimatedUtil = calculateUtilityUsingLearnRate(oppBid, i);
					if (Math.abs(estimatedUtil - expectedUtil) < Math.abs(closestUtil - expectedUtil)) {
						closestUtil = estimatedUtil;
						actualLearnRate = i;
					}
				}
				
			}
			UpdateValues(oppBid, actualLearnRate);
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void UpdateValues(BidDetails oppBid, int learnRate) throws Exception
	{
		// Then for each issue value that has been offered last time, a constant value is added to its corresponding ValueDiscrete.
		for(Entry<Objective, Evaluator> e: opponentUtilitySpace.getEvaluators()){
			( (EvaluatorDiscrete)e.getValue() ).setEvaluation(oppBid.getBid().getValue(((IssueDiscrete)e.getKey()).getNumber()), 
				( learnRate + 
					((EvaluatorDiscrete)e.getValue()).getEvaluationNotNormalized( 
						( (ValueDiscrete)oppBid.getBid().getValue(((IssueDiscrete)e.getKey()).getNumber()) ) 
					)
				)
			);
		}
	}
	
	private double calculateUtilityUsingLearnRate(BidDetails oppBid, int learnRate) throws Exception {
		double utility = 0;
		for (Entry<Objective, Evaluator> e: opponentUtilitySpace.getEvaluators()) { // Iterates over all issues
			EvaluatorDiscrete e2 = ((EvaluatorDiscrete)e.getValue());
			double issueWeight = e2.getWeight();
			int itemValue = e2.getValue((ValueDiscrete)oppBid.getBid().getValue(e.getKey().getNumber()));
			itemValue += learnRate;
			int valueSum = learnRate;
			for (ValueDiscrete v : e2.getValues()) {
				valueSum += e2.getValue(v);
			}
			double normalizedItemValue = (double)itemValue / (double)valueSum;
			utility += issueWeight * normalizedItemValue;
		}
		return utility;
	}
	
	/**
	 * Finds the minimum util we currently expect from the opponent (e.g. what strategy we expect)
	 * @return
	 */
	public double ExpectedNewBidUtil()
	{
		// The expected minimum utility is a function of the number of different offers we have received and the number of different offers possible.
		List<BidDetails> distinctBids = getDistinctBids(negotiationSession.getOpponentBidHistory());
		double meanConcessionPerNewBid = 1d / ((double)opponentUtilitySpace.getDomain().getNumberOfPossibleBids());
		// Assuming each time we receive a new 
		return 1d - (double)distinctBids.size() * meanBidSkip * meanConcessionPerNewBid;
	}
	
	/**
	 * Returns a list (ordered in time where the first item is the oldest bid, and the last new item is the newest bid.
	 * @param hist The BidHistory from which we want to get the list of distinct bids.
	 * @return List of recent bids
	 */
	public static List<BidDetails> getDistinctBids(BidHistory hist)
	{
		List<BidDetails> opponentBids = hist.sortToTime().getHistory();
		// Make sure we ignore the most recent bid. This is necessary to check whether the most recent bid is a new one. 
		// Also the most recent bid should be ignored in the calculation.
		opponentBids.remove(0);
		List<BidDetails> distinctBids = new ArrayList<BidDetails>();
		for (BidDetails bid: opponentBids) {
			if (!distinctBids.contains(bid))
				distinctBids.add(bid);			
		}
		return distinctBids;
	}

	@Override
	public double getBidEvaluation(Bid bid) {
		double result = 0;
		try {
			result = opponentUtilitySpace.getUtility(bid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public String getName() {
		return "HardHeaded Frequency Model";
	}
}

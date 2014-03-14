import java.util.HashMap;
import java.util.Random;

import negotiator.Bid;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OMStrategy;
import negotiator.boaframework.OfferingStrategy;
import negotiator.boaframework.OpponentModel;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;

/**
 * @author 	Tom Runia, Tom Viering, Bas Dado, Olivier Hokke, Arnold Schutter
 * @since	01-04-2014
 * @version	1.0
 */
public class BOAconstructor_BS extends OfferingStrategy {
	
	private Helper helper;
	
	public void init(NegotiationSession negSession, OpponentModel model, OMStrategy oms, HashMap<String, Double> parameters) throws Exception { 
		
		// Initialize helper class
		// WARNING: Only the BS calls the init() method!
		helper = Helper.get(negSession);
		helper.init(negSession, this);

		Log.newLine("Succesfully initialized Bidding Strategy.");
	}
	
	/**
	 * I suggest we keep this simple. Only use two phases:
	 * 
	 * 	1) Select random bids just like we did during the course. During this
	 * 	   phase we also sample the outcome space and find out what are good bids
	 * 	2) Use the knowledge from the first phase to generate 'good' bids
	 * 
	 */
	public BidDetails determineNextBid() {
		Bid bid;
		// Generate new random bid
		try { bid = getRandomBid(helper.getReservationValue()); } 
		catch (Exception e) { 
			Log.newLine("WARNING: unable to generate random bid...");
			return null;
		}
		
		BidDetails bd = new BidDetails(bid, getUtility(bid));
		return bd;
	}

	public BidDetails determineOpeningBid() {
		// TODO: change into something more clever? :-)
		return determineNextBid();
	}
	
	private Bid getRandomBid (double minUtility) throws Exception {
		// From the ExampleAgent
		HashMap<Integer, Value> values = new HashMap<Integer, Value>(); // pairs <issuenumber,chosen value string>
		Random randomnr= new Random();
		
		Bid bid = null;
		
		do {
			for(Issue lIssue:helper.getIssues()) {
				// Assuming only discrete utility spaces
				IssueDiscrete lIssueDiscrete = (IssueDiscrete)lIssue;
				int optionIndex=randomnr.nextInt(lIssueDiscrete.getNumberOfValues());
				values.put(lIssue.getNumber(), lIssueDiscrete.getValue(optionIndex));
			}
			// Create new bid and check if it is smaller than the threshold
			bid = new Bid(helper.getUtilSpace().getDomain(),values);
		} while (getUtility(bid) < minUtility);

		return bid;
	}
	
	private double getUtility (Bid bid) {
		double util = 0.0;
		
		try {
			util = helper.getUtilSpace().getUtility(bid);
		} catch (Exception e) {
			util = 0.0;
			Log.newLine("WARNING: cannot get utility from invalid Bid...");
		}
		
		return util;
	}
	
	// TODO:
	//  - getMaxUtilityBid() --> efficient algorithm
	//	- getUtility() can be used to get (un)discounted own utility for sampling

}

package negotiator.group7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import misc.Range;
import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.analysis.BidPoint;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;

public class Helper {
	
	private static HashMap<NegotiationSession, Helper> osToHelper = new HashMap<NegotiationSession, Helper>();
	public static Helper get(NegotiationSession os) {
		if (!osToHelper.containsKey(os)) {
			osToHelper.put(os, new Helper());
		}
		return osToHelper.get(os);
	}
	
	private BidDetails kalaiPoint;
	private BidDetails nashPoint;
	private NegotiationSession session;
	private Group7_AS as;
	private Group7_BS bs;
	private Group7_FrequencyOM om;
	private Group7_OMS oms;
	private Integer bidsLeft;
	private Integer bidsTotal;
	private Integer bidsMade;
	private boolean opponentModelReliable;

	public boolean isOpponentModelReliable() {
		return opponentModelReliable;
	}

	public void setOpponentModelReliable(boolean opponentModelReliable) {
		this.opponentModelReliable = opponentModelReliable;
	}

	public NegotiationSession getSession() {
		return session;
	}

	public void setSession(NegotiationSession session) {
		this.session = session;
	}

	/**
	 * @return The amount of bids that are possibly left, based on the (running) average duration per bid. (based on the view of the AS! so if the AS is called later than you, this number can be outdated by 1 bid)
	 */
	public Integer getBidsLeft() {
		return bidsLeft;
	}

	/**
	 * @param bidsLeft The amount of bids that are possibly left, based on the (running) average duration per bid. (based on the view of the AS! so if the AS is called later than you, this number can be outdated by 1 bid)
	 */
	public void setBidsLeft(Integer bidsLeft) {
		this.bidsLeft = bidsLeft;
	}

	/**
	 * @return The total amount of bids that we guess will be made in this session. (based on the view of the AS! so if the AS is called later than you, this number can be outdated by 1 bid)
	 */
	public Integer getBidsTotal() {
		return bidsTotal;
	}

	/**
	 * @param bidsTotal The amount of bids that are possibly left, based on the (running) average duration per bid. (based on the view of the AS! so if the AS is called later than you, this number can be outdated by 1 bid)
	 */
	public void setBidsTotal(Integer bidsTotal) {
		this.bidsTotal = bidsTotal;
	}

	/**
	 * @return the amount of bids that have been made until now. (based on the view of the AS! so if the AS is called later than you, this number can be outdated by 1 bid)
	 */
	public Integer getBidsMade() {
		return bidsMade;
	}

	/**
	 * @param bidsMade the amount of bids that have been made until now. (based on the view of the AS! so if the AS is called later than you, this number can be outdated by 1 bid)
	 */
	public void setBidsMade(Integer bidsMade) {
		this.bidsMade = bidsMade;
	}

	public Group7_AS getAcceptanceStrategy() {
		return as;
	}

	public void setAcceptanceStrategy(Group7_AS as) {
		this.as = as;
	}

	public Group7_BS getBiddingStrategy() {
		return bs;
	}

	public void setBiddingStrategy(Group7_BS bs) {
		this.bs = bs;
	}

	public Group7_FrequencyOM getOpponentModel() {
		return om;
	}

	public void setOpponentModel(Group7_FrequencyOM om) {
		this.om = om;
	}

	public Group7_OMS getOMStrategy() {
		return oms;
	}

	public void setOMStrategy(Group7_OMS oms) {
		this.oms = oms;
	}
	
	public void setKalaiPoint (BidPoint ks) {
		kalaiPoint = getBidDetails(ks);
	}
	
	public BidDetails getKalaiPoint () {
		return kalaiPoint;
	}
	
	public void setNashPoint (BidDetails np) {
		nashPoint = np;
	}
	
	public BidDetails getNashPoint () {
		return nashPoint;
	}
	
	/**
	 * Returns a list (ordered in time where the first item is the oldest bid, and the last new item is the newest bid.
	 * @param hist The BidHistory from which we want to get the list of distinct bids.
	 * @return List of recent bids
	 */
	public BidDetails getBidDetails(BidPoint point) {
		if (getOpponentModel() == null) return null;
		if (getSession() == null) return null;
		if (getSession().getOutcomeSpace() == null) return null;
		
		Range r = new Range(point.getUtilityA() - 0.001, point.getUtilityA() + 0.001);
		List<BidDetails> bidsInRange = getSession().getOutcomeSpace().getBidsinRange(r);
		for (BidDetails B2: bidsInRange)
			if (B2.getMyUndiscountedUtil() == point.getUtilityA())
				if (getOpponentModel().getBidEvaluation(B2.getBid()) == point.getUtilityB())
					return B2;
		
		return null;
	}
	
	/**
	 * Returns a list (ordered in time where the first item is the oldest bid, and the last new item is the newest bid.
	 * @param hist The BidHistory from which we want to get the list of distinct bids.
	 * @return List of recent bids
	 */
	public List<Bid> getDistinctBids(BidHistory hist)
	{
		List<BidDetails> opponentBids = hist.sortToTime().getHistory();
		// Make sure we ignore the most recent bid. This is necessary to check whether the most recent bid is a new one. 
		// Also the most recent bid should be ignored in the calculation.
		List<Bid> distinctBids = new ArrayList<Bid>();
		boolean ignoredFirst = false;
		for (BidDetails bidDet: opponentBids) {
			Bid bid = bidDet.getBid();
			if (!ignoredFirst)
				ignoredFirst = true;
			if (!distinctBids.contains(bid))
				distinctBids.add(bid);			
		}
		
		//Log.rln("Number of distinct opponent bids: " + distinctBids.size());
		return distinctBids;
	}
}

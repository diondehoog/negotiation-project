package negotiator.group7;

import java.util.List;

import misc.Range;
import negotiator.analysis.BidPoint;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;

public class Helper {
	
	private static BidDetails kalaiPoint;
	private static BidDetails nashPoint;
	private static NegotiationSession session;
	private static Group7_AS as;
	private static Group7_BS bs;
	private static Group7_FrequencyOM om;
	private static Group7_OMS oms;
	private static Integer bidsLeft;
	private static Integer bidsTotal;
	private static Integer bidsMade;
	private static Integer bidsDistinctOpponent;
	
	public static Integer getCountDistinctOpponentBids() {
		return bidsDistinctOpponent;
	}

	public static void setCountDistinctOpponentBids(Integer bidsDistinctOpponent) {
		Helper.bidsDistinctOpponent = bidsDistinctOpponent;
	}

	public static NegotiationSession getSession() {
		return session;
	}

	public static void setSession(NegotiationSession session) {
		Helper.session = session;
	}

	/**
	 * @return The amount of bids that are possibly left, based on the (running) average duration per bid. (based on the view of the AS! so if the AS is called later than you, this number can be outdated by 1 bid)
	 */
	public static Integer getBidsLeft() {
		return bidsLeft;
	}

	/**
	 * @param bidsLeft The amount of bids that are possibly left, based on the (running) average duration per bid. (based on the view of the AS! so if the AS is called later than you, this number can be outdated by 1 bid)
	 */
	public static void setBidsLeft(Integer bidsLeft) {
		Helper.bidsLeft = bidsLeft;
	}

	/**
	 * @return The total amount of bids that we guess will be made in this session. (based on the view of the AS! so if the AS is called later than you, this number can be outdated by 1 bid)
	 */
	public static Integer getBidsTotal() {
		return bidsTotal;
	}

	/**
	 * @param bidsTotal The amount of bids that are possibly left, based on the (running) average duration per bid. (based on the view of the AS! so if the AS is called later than you, this number can be outdated by 1 bid)
	 */
	public static void setBidsTotal(Integer bidsTotal) {
		Helper.bidsTotal = bidsTotal;
	}

	/**
	 * @return the amount of bids that have been made until now. (based on the view of the AS! so if the AS is called later than you, this number can be outdated by 1 bid)
	 */
	public static Integer getBidsMade() {
		return bidsMade;
	}

	/**
	 * @param bidsMade the amount of bids that have been made until now. (based on the view of the AS! so if the AS is called later than you, this number can be outdated by 1 bid)
	 */
	public static void setBidsMade(Integer bidsMade) {
		Helper.bidsMade = bidsMade;
	}

	public static Group7_AS getAcceptanceStrategy() {
		return as;
	}

	public static void setAcceptanceStrategy(Group7_AS as) {
		Helper.as = as;
	}

	public static Group7_BS getBiddingStrategy() {
		return bs;
	}

	public static void setBiddingStrategy(Group7_BS bs) {
		Helper.bs = bs;
	}

	public static Group7_FrequencyOM getOpponentModel() {
		return om;
	}

	public static void setOpponentModel(Group7_FrequencyOM om) {
		Helper.om = om;
	}

	public static Group7_OMS getOMStrategy() {
		return oms;
	}

	public static void setOMStrategy(Group7_OMS oms) {
		Helper.oms = oms;
	}
	
	public static void setKalaiPoint (BidPoint ks) {
		kalaiPoint = getBidDetails(ks);
	}
	
	public static BidDetails getKalaiPoint () {
		return kalaiPoint;
	}
	
	public static void setNashPoint (BidDetails np) {
		nashPoint = np;
	}
	
	public static BidDetails getNashPoint () {
		return nashPoint;
	}
	
	/**
	 * Depends on the Group7_OM, Frequency Opponent Model and the session
	 */
	public static BidDetails getBidDetails(BidPoint point) {
		if (getOpponentModel() == null) return null;
		if (getSession() == null) return null;
		
		Range r = new Range(point.getUtilityA() - 0.001, point.getUtilityA() + 0.001);	
		List<BidDetails> bidsInRange = getSession().getOutcomeSpace().getBidsinRange(r);
		for (BidDetails B2: bidsInRange) {
			if (B2.getMyUndiscountedUtil() == point.getUtilityA())
				if (getOpponentModel().getBidEvaluation(B2.getBid()) == point.getUtilityB())
					return B2;
		}
		return null;
	}
}

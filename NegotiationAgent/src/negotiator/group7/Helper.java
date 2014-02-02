package negotiator.group7;

import negotiator.analysis.BidPoint;
import negotiator.bidding.BidDetails;

public class Helper {
	
	private static BidPoint kalaiPoint;
	private static BidDetails nashPoint;
	private static Group7_AS as;
	private static Group7_BS bs;
	private static Group7_OM om;
	private static Group7_OMS oms;
	private static Integer bidsLeft;
	private static Integer bidsTotal;
	private static Integer bidsMade;
	
	public static Integer getBidsLeft() {
		return bidsLeft;
	}

	public static void setBidsLeft(Integer bidsLeft) {
		Helper.bidsLeft = bidsLeft;
	}

	public static Integer getBidsTotal() {
		return bidsTotal;
	}

	public static void setBidsTotal(Integer bidsTotal) {
		Helper.bidsTotal = bidsTotal;
	}

	public static Integer getBidsMade() {
		return bidsMade;
	}

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

	public static Group7_OM getOpponentModel() {
		return om;
	}

	public static void setOpponentModel(Group7_OM om) {
		Helper.om = om;
	}

	public static Group7_OMS getOMStrategy() {
		return oms;
	}

	public static void setOMStrategy(Group7_OMS oms) {
		Helper.oms = oms;
	}
	
	public static void setKalaiPoint (BidPoint ks) {
		kalaiPoint = ks;
	}
	
	public static BidPoint getKalaiPoint () {
		return kalaiPoint;
	}
	
	public static void setNashPoint (BidDetails np) {
		nashPoint = np;
	}
	
	public static BidDetails getNashPoint () {
		return nashPoint;
	}
	
}

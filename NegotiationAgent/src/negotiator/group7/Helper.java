package negotiator.group7;

import negotiator.analysis.BidPoint;
import negotiator.bidding.BidDetails;

public class Helper {
	
	private static BidPoint kalaiPoint;
	private static BidDetails nashPoint;
	
	public Helper() {
		// TODO Auto-generated constructor stub
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

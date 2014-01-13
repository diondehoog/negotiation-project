package negotiator.group7;

import java.util.Comparator;

import negotiator.bidding.BidDetails;
import negotiator.boaframework.OpponentModel;

public class OpponentBidCompare implements Comparator<BidDetails> {
	
	private OpponentModel model = null;
	
	public void setOpponentModel(OpponentModel model) {
		this.model = model;
	}
	
	@Override
	public int compare(BidDetails x, BidDetails y) {
		if (model == null) {
			System.out.println("Error: no opponent model! Toms opponentBidCompare.java");
		}
		double Ux = model.getBidEvaluation(x.getBid());
		double Uy = model.getBidEvaluation(y.getBid());
		
		if (Ux > Uy)
			return 1;
		if (Ux == Uy)
			return 0;
		return -1;
	 }
}

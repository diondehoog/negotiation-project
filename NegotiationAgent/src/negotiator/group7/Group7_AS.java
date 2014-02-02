package negotiator.group7;

import java.util.HashMap;

import negotiator.BidHistory;
import negotiator.boaframework.AcceptanceStrategy;
import negotiator.boaframework.Actions;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OfferingStrategy;

/**
 * Checking all different acceptance strategies
 * 
 * @author Olivier Hokke
 * @version 13/01/14
 */
public class Group7_AS extends AcceptanceStrategy {

	/**
	 * Empty constructor for the BOA framework.
	 * 
	 * @return
	 */
	public Group7_AS() {
	}

	public Group7_AS(NegotiationSession negoSession, OfferingStrategy strat) {
		this.negotiationSession = negoSession;
		this.offeringStrategy = strat;
	}

	@Override
	public void init(NegotiationSession negoSession, OfferingStrategy strat,
			HashMap<String, Double> parameters) throws Exception {
		this.negotiationSession = negoSession;
		this.offeringStrategy = strat;
		
		if (parameters != null) {
			if (parameters.containsKey("timeWindow"))
				this.timeWindow = parameters.get("timeWindow");
			if (parameters.containsKey("capWorstSlope"))
				this.capWorstSlope = parameters.get("capWorstSlope");
			if (parameters.containsKey("capWorstMinimal"))
				this.capWorstMinimal = parameters.get("capWorstMinimal");
			if (parameters.containsKey("acceptCurveStart"))
				this.acceptCurveStart = parameters.get("acceptCurveStart");
			if (parameters.containsKey("acceptCurveExponent"))
				this.acceptCurveExponent = parameters.get("acceptCurveExponent");
			if (parameters.containsKey("percentDurationWeight"))
				this.percentDurationWeight = parameters.get("percentDurationWeight");
			if (parameters.containsKey("panicConcede"))
				this.panicConcede = parameters.get("panicConcede");
			if (parameters.containsKey("panicWhenBidsLeft"))
				this.panicWhenBidsLeft = parameters.get("panicWhenBidsLeft").intValue();
		}
	}

	/** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/** ~~~~~~~~~~~~~~~~~~ ACCEPTANCE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

	private double timeWindow = 0.2; // how big is the time window in which to search for our/his best/worst bids?
	private double capWorstSlope = -0.3; // the slope from t=0 to t=1 with which to cap our worst bid utility
	private double capWorstMinimal = 0.6; // the minimal value that our worst utility should be, otherwise capped
	private double acceptCurveStart = 0.9; // the starting value of the accept curve
	private double acceptCurveExponent = 0.04; // the exponent with which the curve falls. determines how hard and fast. Calculate final value curve: start * 0.005 ^ exponent. For start=0.9, exponent=0.04, we get: 0.73
	private double percentDurationWeight = 0.25; // how much the duration of the last bid weighs in with the current running average
	private double panicConcede = 0.05; // amount of utility we allow ourself to concede from opponents best bid during panic mode (from time window)
	private int panicWhenBidsLeft = 3; // amount of bids left after which we should panic and accept ASAP
	
	/**
	 * Determine whether we should accept or reject.
	 */
	public Actions determineAcceptability() {
		if (negotiationSession == null 
			|| negotiationSession.getOwnBidHistory() == null
			|| negotiationSession.getOpponentBidHistory() == null
			|| negotiationSession.getOwnBidHistory().getWorstBidDetails() == null 
			|| negotiationSession.getOpponentBidHistory().getWorstBidDetails() == null)
		{
			return Actions.Reject;
		}
		
		// guess how many bids are left
		guessBidsLeft();

		// get history and time
		double time = negotiationSession.getTime();
		double window = Math.max(0.0, time - timeWindow);
		BidHistory bhOpp = negotiationSession.getOpponentBidHistory();
		BidHistory bhOwn = negotiationSession.getOwnBidHistory();
		
		// only look within certain time window
		if (time < 1.0) {
			bhOpp = bhOpp.filterBetweenTime(window, time);
			bhOwn = bhOwn.filterBetweenTime(window, time);
		}

		// get some historical facts
		double hisLast = bhOpp.getHistory().get(0).getMyUndiscountedUtil();
		double hisBest = bhOpp.getBestBidDetails().getMyUndiscountedUtil();
		double ourWorst = negotiationSession.getOwnBidHistory().getWorstBidDetails().getMyUndiscountedUtil();
		double ourNext = offeringStrategy.getNextBid().getMyUndiscountedUtil();
		
		//Group7_BS bs = ((Group7_BS) offeringStrategy);
		//bs.getKalaiSmorodisky();
		
		// adjust ourWorst so that it can never go lower than the line from 0.9 at t=0 to 0.6 at t=1
		double ourWorstCapped = Math.max(ourWorst, (1 - time) * capWorstSlope + capWorstMinimal);
		
		// This curve is build to gradually become lower with increasing speed so that at the end we are more allowing
		//double acceptCurve = acceptCurveStart * Math.pow((double)bidsLeft/(double)bidsTotal, acceptCurveExponent);
		double acceptCurve = acceptCurveStart * Math.min(1.0, Math.pow((1.005) - Math.pow(time, 2), acceptCurveExponent));

		// TODO: let accept curve approach the nash!!
		// TODO: accept when hisLast == nash/kalai??

		/** --------------------- AC_curve ----------------------------- 
		  * AC_const, but time dependent
		  * Here we simply accept anything above the accept curve
		  * -------------------------------------------------------------- */
		if (hisLast > acceptCurve) 
		{
			Log.newLine(" @ hisLast > acceptCurve: " + hisLast + "; " + acceptCurve);
			return Actions.Accept;
		}
		/** --------------------- AC_panic ----------------------------- 
		  * AC_time, but only opponent's better offers
		  * Here we accept the opponents best with a conceding factor, only when 
		  * less than 'panicWhenBidsLeft' bids are left
		  * -------------------------------------------------------------- */
		else if (bidsLeft < panicWhenBidsLeft && hisLast >= hisBest - panicConcede) 
		{
			Log.newLine(" @ bidsLeft < " + panicWhenBidsLeft);
			return Actions.Accept;
		} 
		/** --------------------- AC_worst ----------------------------- 
		  * AC_next, but we don't look at our next bid, but our recent worst.
		  * If he bids higher than our worst, we will simply accept right away
		  * and since we don't go down with our utility that quickly, this should work fine
		  * Also, this value is capped above some line (see comment for 'ourWorstCapped')
		  * -------------------------------------------------------------- */
		else if (hisLast > ourWorstCapped) 
		{
			Log.newLine(" @ hisLast > ourWorstFixed: " + hisLast + "; " + ourWorstCapped);
			return Actions.Accept;
		}
		/** --------------------- AC_next ----------------------------- 
		  * Here we simply apply the AC_next acceptance strategy
		  * If he bids higher than our worst, we will simply accept right away
		  * and since we don't go down with our utility that quickly, this should work fine
		  * -------------------------------------------------------------- */
		else if (hisLast > ourNext)
		{
			Log.newLine(" @ hisLast > ourNext: " + hisLast + "; " + ourNext);
			return Actions.Accept;
		}
		else
		{
			return Actions.Reject;
		}
	}

	/** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/** ~~~~~~~~~~~~~~~~~~ BIDS LEFT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

	double averageDeltaTime = 0;
	double previousTime = 0;
	static int bidsMade = 0;
	static int bidsLeft = 0;
	static int bidsTotal = 0;
	
	/**
	 * This method guesses the amount of bids that are left and counts how many bids have passed.
	 */
	private void guessBidsLeft() {
		double time = negotiationSession.getTime();
		double dt = time - previousTime;
		if (previousTime != 0) {
			if (averageDeltaTime == 0) {
				averageDeltaTime = dt;
			} else {
				averageDeltaTime = (1 - percentDurationWeight) * averageDeltaTime + percentDurationWeight * dt;
			}
		}
		previousTime = time;
		
		bidsMade++;
		bidsLeft = (int) ((1.0 - time) / (averageDeltaTime + 0.00001)); // avoiding division by zero :P
		bidsTotal = bidsMade + bidsLeft;
	}

	/**
	 * @return amount of bids that are probably left (from AS point of view)
	 */
	public static int GetGuessedBidsLeft() {
		return bidsLeft;
	}
	/**
	 * @return amount of bids that are made until now (from AS point of view)
	 */
	public static int GetBidsMade() {
		return bidsMade;
	}
	/**
	 * @return guessed total amount of bids of session (from AS point of view)
	 */
	public static int GetBidsTotal() {
		return bidsMade;
	}
}
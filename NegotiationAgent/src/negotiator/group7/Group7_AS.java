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
	
	private Helper ourHelper;
	
	public Group7_AS() { }

	public Group7_AS(NegotiationSession negoSession, OfferingStrategy strat) {
		this.negotiationSession = negoSession;
		this.offeringStrategy = strat;
		ourHelper = Helper.get(negotiationSession);
		ourHelper.setBidsLeft(0);
		ourHelper.setBidsMade(0);
		ourHelper.setBidsTotal(0);
		ourHelper.setAcceptanceStrategy(this);
		//Helper.setSession(negoSession);
	}

	@Override
	public void init(NegotiationSession negoSession, OfferingStrategy strat,
			HashMap<String, Double> parameters) throws Exception {
		this.negotiationSession = negoSession;
		this.offeringStrategy = strat;
		ourHelper = Helper.get(negotiationSession);
		ourHelper.setBidsLeft(0);
		ourHelper.setBidsMade(0);
		ourHelper.setBidsTotal(0);
		ourHelper.setAcceptanceStrategy(this);
		ourHelper.setSession(negoSession);
		
		if (parameters != null) {
			if (parameters.containsKey("timeWindow"))
				this.timeWindow = parameters.get("timeWindow");
			if (parameters.containsKey("capWorstSlope"))
				this.capWorstSlope = parameters.get("capWorstSlope");
			if (parameters.containsKey("capWorstMinimal"))
				this.capWorstMinimal = parameters.get("capWorstMinimal");
			if (parameters.containsKey("acceptCurveStart"))
				this.acceptCurveStart = parameters.get("acceptCurveStart");
			if (parameters.containsKey("acceptCurveApproach"))
				this.acceptCurveApproach = parameters.get("acceptCurveApproach");
			if (parameters.containsKey("acceptCurveType"))
				this.acceptCurveType = parameters.get("acceptCurveType").intValue();
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
	private double acceptCurveStart = 1.0; // the starting value of the accept curve if type == 1. Otherwise, this is the percentage between 1.0 and the value the curve has to approach that the curves starts at. If 0.8 and type is 3 (kalai), then start is 0.8 + 0.2 * kalai
	private double acceptCurveApproach = 0.6; // the value that the accept curve will approach if type == 1
	private int acceptCurveType = 2; // 1 = static, surve simply reaches the 'acceptCurveApproach' value and starts at 'acceptCurveStart'. 2 = curve approach avg of 1.0 and first bid opponent. 3 = curve approach kalai. 4 = curve approach nash.
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
			|| negotiationSession.getOpponentBidHistory().getWorstBidDetails() == null
			|| negotiationSession.getOpponentBidHistory().getHistory() == null
			|| negotiationSession.getOpponentBidHistory().getHistory().size() < 1)
		{
			return Actions.Reject;
		}
		
		// Just for testing !
		//if (Helper.getOpponentModel() != null) {
		//	Helper.getOMStrategy().getOpponentModel();
		//}
		
		// guess how many bids are left
		guessBidsLeft();

		// get history and time
		double time = negotiationSession.getTime();
		double window = Math.max(0.0, time - timeWindow);
		BidHistory bhOpp = negotiationSession.getOpponentBidHistory();
		BidHistory bhOwn = negotiationSession.getOwnBidHistory();
		
		// only look within certain time window
		if (window < 1.0) {
			bhOpp = bhOpp.filterBetweenTime(window, time);
			bhOwn = bhOwn.filterBetweenTime(window, time);
		}

		// get some historical facts
		double hisLast = 0.0;
		double hisBest = 0.0;
		if (bhOpp.getHistory().size() > 0) hisLast = bhOpp.getHistory().get(0).getMyUndiscountedUtil();
		if (bhOpp.getHistory().size() > 0) hisBest = bhOpp.getBestBidDetails().getMyUndiscountedUtil();
		double ourWorst = negotiationSession.getOwnBidHistory().getWorstBidDetails().getMyUndiscountedUtil();
		double ourNext = offeringStrategy.getNextBid().getMyUndiscountedUtil();
		
		// checks
		boolean isHardHeaded = ourHelper.getOMStrategy() == null ? false : ourHelper.getOMStrategy().isOpponentHardHeaded();
		double kalaiUtility = ourHelper.getKalaiPoint() == null ? 1.0 : ourHelper.getKalaiPoint().getMyUndiscountedUtil();
		double nashUtility = ourHelper.getNashPoint() == null ? 1.0 : ourHelper.getNashPoint().getMyUndiscountedUtil();
		double range = 0.005;
		
		// adjust ourWorst so that it can never go lower than the line from 0.9 at t=0 to 0.6 at t=1
		double ourWorstCapped = Math.max(ourWorst, (1 - time) * -capWorstSlope + capWorstMinimal) - (0.05 * time);
		//Log.hln(isHardHeaded + " ; " + Log.format(ourWorst) + " ; " +Log.format ((1 - time) * -capWorstSlope + capWorstMinimal));
		
		// This curve is build to gradually become lower with increasing speed so that at the end we are more allowing
		double acceptCurve = getAcceptCurveValue(time);

		/** --------------------- AC_curve ----------------------------- 
		  * AC_const, but time dependent
		  * Here we simply accept anything above the accept curve
		  * -------------------------------------------------------------- */
		if (hisLast > acceptCurve) 
		{
			Log.newLine("\n\n ACCEPT! @ hisLast > acceptCurve: " + hisLast + "; " + acceptCurve + "\n\n");
			return Actions.Accept;
		}
		
		/** --------------------- AC_panic ----------------------------- 
		  * AC_time, but only opponent's better offers (AC_max)
		  * Here we accept the opponents best with a conceding factor, only when 
		  * less than 'panicWhenBidsLeft' bids are left.
		  * But(!) never do this when opponent is hardheaded
		  * -------------------------------------------------------------- */
		else if (ourHelper.getBidsLeft() < panicWhenBidsLeft && hisLast >= hisBest - panicConcede && !isHardHeaded) 
		{
			Log.newLine("\n\n ACCEPT! @ bidsLeft < " + panicWhenBidsLeft + " && hisLast >= hisBest - panicConcede: " + ourHelper.getBidsLeft() + "; " + hisLast + "; " + (hisBest - panicConcede) + "\n\n");
			return Actions.Accept;
		} 
		
		/** --------------------- AC_horror ----------------------------- 
		  * last bid... just accept man... just accept... :(
		  * -------------------------------------------------------------- */
		else if (ourHelper.getBidsLeft() == 1) 
		{
			Log.newLine("\n\n ACCEPT! @ Last bid.... just accept man... just accept :( \n\n");
			return Actions.Accept;
		} 
		
		/** --------------------- AC_kalai ----------------------------- 
		  * automatically a reliable kalai
		  * -------------------------------------------------------------- */
		else if (kalaiUtility - range <= hisLast && hisLast <= kalaiUtility + range
				&& ourHelper.isOpponentModelReliable()) 
		{
			Log.newLine("\n\n ACCEPT! @ hisLast == Kalai!!! :D \n\n");
			return Actions.Accept;
		}
		
		/** --------------------- AC_nash ----------------------------- 
		  * automatically a reliable nash
		  * -------------------------------------------------------------- */
		else if (nashUtility - range <= hisLast && hisLast <= nashUtility + range
				&& ourHelper.isOpponentModelReliable()) 
		{
			Log.newLine("\n\n ACCEPT! @ hisLast == Nash!!! :9 \n\n");
			return Actions.Accept;
		} 
		
		/** --------------------- AC_worst ----------------------------- 
		  * AC_next, but we don't look at our next bid, but our recent worst.
		  * If he bids higher than our worst, we will simply accept right away
		  * and since we don't go down with our utility that quickly, this should work fine
		  * Also, this value is capped above some line (see comment for 'ourWorstCapped')
		  * Also, this capped value has a time dependent conceding factor
		  * -------------------------------------------------------------- */
		else if (hisLast > ourWorstCapped) 
		{
			Log.newLine("\n\n ACCEPT! @ hisLast > ourWorstFixed: " + hisLast + "; " + ourWorstCapped + "\n\n");
			return Actions.Accept;
		}
		
		/** --------------------- AC_next ----------------------------- 
		  * Here we simply apply the AC_next acceptance strategy
		  * If he bids higher than our worst, we will simply accept right away
		  * and since we don't go down with our utility that quickly, this should work fine
		  * -------------------------------------------------------------- */
		else if (hisLast > ourNext)
		{
			Log.newLine("\n\n ACCEPT! @ hisLast > ourNext: " + hisLast + "; " + ourNext + "\n\n");
			return Actions.Accept;
		}
		else
		{
			return Actions.Reject;
		}
	}

	/** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/** ~~~~~~~~~~~~~~~~~~ ACCEPT CURVE ~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

	private boolean acceptCurveReady = false;
	private double acceptCurveExponent = 0;
	private double curveFix = 0.003; // the value to offset the curve. This defines also the curve of the curve.
	
	private double getAcceptCurveValue(double time) 
	{
		setupAcceptCurve(time);
		if (!acceptCurveReady) return 1.0;
		return acceptCurveStart * min(1.0, pow((1 + curveFix) - pow(time, 2), acceptCurveExponent));
	}
	
	private void setupAcceptCurve(double time) 
	{
		// no need to adjust static curve or curve based on first opponent bid, after setup is complete
		if (acceptCurveReady && acceptCurveType < 3) return; 
		
		// Initialise base values
		double first = 0;
		double kalai = 0;
		double nash = 0;
		double approach = 1.0;
		double start = acceptCurveStart;

		// Retrieve base values
		try 
		{
			if (negotiationSession.getOpponentBidHistory().getFirstBidDetails() != null) 
				first = negotiationSession.getOpponentBidHistory().getFirstBidDetails().getMyUndiscountedUtil();
			if (ourHelper.getKalaiPoint() != null)
				kalai = ourHelper.getKalaiPoint().getMyUndiscountedUtil();
			if (ourHelper.getNashPoint() != null)
				nash = ourHelper.getNashPoint().getMyUndiscountedUtil();
		}
		catch (Exception e) 
		{ 
			e.printStackTrace(); 
		}

		// Handle type of curve
		switch (acceptCurveType) 
		{
			case 1: // approach static value
				approach = acceptCurveApproach;
				break;
				
			case 2: // approach avg 1.0 and first opponent bid
				approach = 0.5 + 0.5 * first;
				break;
				
			case 3: // approach kalai
				if (kalai == 0 || time < 0.1) return;
				// only if kalai available and time is > 0.1;
				approach = kalai;
				break;
				
			case 4: // approach nash
				if (nash == 0 || time < 0.1) return;
				// only if nash available and time is > 0.1;
				approach = nash;
				break;
		}
		
		if (acceptCurveType > 1)
			start = start + (1 - start) * approach;

		// Finally set up curve!
		acceptCurveExponent = log(curveFix, approach);
		acceptCurveStart = start;
		
		//Log.hln("## Accept curve approaches: " + Log.format(approach) + " and starts at: " + Log.format(start));
		
		acceptCurveReady = true;
	}
	
	/** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/** ~~~~~~~~~~~~~~~~~~ BIDS LEFT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

	double averageDeltaTime = 0;
	double previousTime = 0;
	
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

		ourHelper.setBidsMade(ourHelper.getBidsMade() + 1);
		ourHelper.setBidsLeft((int) ((1.0 - time) / (averageDeltaTime + 0.00001)));
		ourHelper.setBidsTotal(ourHelper.getBidsMade() + ourHelper.getBidsLeft());
	}
	
	/** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/** ~~~~~~~~~~~~~~~~~~ READIBILITY WRAPPERS ~~~~~~~~~~~~~~~~~~~ */
	/** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

	private double log(double base, double value) {
		return Math.log(value) / Math.log(base);
	}
	private double min(double v1, double v2) {
		return Math.min(v1, v2);
	}
	private double pow(double base, double exponent) {
		return Math.pow(base, exponent);
	}
}
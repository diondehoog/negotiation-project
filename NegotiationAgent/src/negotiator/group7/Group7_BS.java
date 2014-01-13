package negotiator.group7;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import misc.Range;
import negotiator.BidHistory;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OMStrategy;
import negotiator.boaframework.OfferingStrategy;
import negotiator.boaframework.OpponentModel;
import negotiator.boaframework.SortedOutcomeSpace;

/**
 * This is an abstract class used to implement a TimeDependentAgent Strategy adapted from [1]
 * 	[1]	S. Shaheen Fatima  Michael Wooldridge  Nicholas R. Jennings
 * 		Optimal Negotiation Strategies for Agents with Incomplete Information
 * 		http://eprints.ecs.soton.ac.uk/6151/1/atal01.pdf
 * 
 * The default strategy was extended to enable the usage of opponent models.
 * 
 * Note that this agent is not fully equivalent to the theoretical model, loading the domain
 * may take some time, which may lead to the agent skipping the first bid. A better implementation
 * is GeniusTimeDependent_Offering. 
 * 
 */
public class Group7_BS extends OfferingStrategy {

	/** k \in [0, 1]. For k = 0 the agent starts with a bid of maximum utility */
	private double k = 0.0;
	/** Maximum target utility */
	private double Pmax;
	/** Minimum target utility */
	private double Pmin;
	/** Concession factor */
	private double e = 1.0;
	
	/** Outcome space */
	SortedOutcomeSpace outcomespace;
	
	/** Phase boundaries */
	private double[] phaseBoundary = {0.2, 0.8};
	private double   phase1LowerBound = 0.8;
	private double   phase1UpperBound = 1.0;
	private double   phase2LowerBound = 0.6;
	private double   phase2range = 0.05;
	
	/** Keep track of the current phase */
	private int curPhase = 1;

	/** Initialize bid history */
	BidHistory biddingHistory;
	
	/** Tit-for-tat parameters: 1/tft1 is amount of approaching, 1/tft2 is amount of distancing*/
	double tft1 = 2;
	double tft2 = 4/3;
	
	/**
	 * Method which initializes the agent by setting all parameters.
	 * The parameter "e" is the only parameter which is required (concession factor).
	 */
	public void init(NegotiationSession negoSession, OpponentModel model, OMStrategy oms, HashMap<String, Double> parameters) throws Exception {
		
		if (parameters.get("phase2") != null)
			phaseBoundary[0] = parameters.get("phase2");
		
		if (parameters.get("phase3") != null)
			phaseBoundary[1] = parameters.get("phase3");

		if (parameters.get("phase1lowerbound") != null)
			phase1LowerBound = parameters.get("phase1lowerbound");
		
		if (parameters.get("phase1upperbound") != null)
			phase1UpperBound = parameters.get("phase1upperbound");
		
		if (parameters.get("phase2lowerbound") != null)
			phase2LowerBound = parameters.get("phase2lowerbound");
		
		if (parameters.get("e") != null)
			e = parameters.get("e");
		
		negotiationSession = negoSession;
		
		outcomespace = new SortedOutcomeSpace(negotiationSession.getUtilitySpace());
		negotiationSession.setOutcomeSpace(outcomespace);
		
		// If k is given it is set to the given value, else it will have the initial value
		if (parameters.get("k") != null)
			k = parameters.get("k");
		
		if (parameters.get("min") != null)
			Pmin = parameters.get("min");
		else
			Pmin = negoSession.getMinBidinDomain().getMyUndiscountedUtil();
	
		if (parameters.get("max") != null) {
			Pmax= parameters.get("max");
		} else {
			BidDetails maxBid = negoSession.getMaxBidinDomain();
			Pmax = maxBid.getMyUndiscountedUtil();
		}
		
		this.opponentModel = model;
		this.omStrategy = oms;
		
		// Initialize bidding history
		biddingHistory = new BidHistory();
		
		// For display purspose, clear the console
		if(System.getProperty("os.name").equals("Mac OS X"))
			Runtime.getRuntime().exec("clear"); // Awesome!
		else
			Runtime.getRuntime().exec("cls");
			
	}

	@Override
	public BidDetails determineOpeningBid() {
		// We can do something better here...
		double time = negotiationSession.getTime();
		BidDetails openingBid = negotiationSession.getOutcomeSpace().getBidNearUtility(0.9*p(time));
		
		System.out.println("openingBid = " + openingBid.toString());
		return openingBid;
		//return determineNextBid();
	}

	/**
	 * Simple offering strategy which retrieves the target utility
	 * and looks for the nearest bid if no opponent model is specified.
	 * If an opponent model is specified, then the agent return a bid according
	 * to the opponent model strategy.
	 */
	@Override
	public BidDetails determineNextBid() {
		
		/* Tom R (2013-12-28)
		 * Some ideas:
		 * 	- NS.getBidHistory() and NS.getOpponentBidHistory() might be useful
		 *  - Class 'ParetoFrontier' can be used to easily compute the PF
		 *  - We have two bid-sorter classes: 'BidDetailsSorterTime' and 'BidDetailsSorterUtility'
		 *  - The 'BidFilter' class has some useful methods for filtering bids on time/utility
		 */
		
		double time = negotiationSession.getTime(); // Normalized time [0,1]
		
		// Determine current negotiation phase
		curPhase = getNegotiationPhase();

		if (curPhase == 1) {
			// First negotiation phase (implemented by Tom)
			// During the first phase we select random bids.
			Range randBidRange = getRangeFunctionFirstPhase(time, 0.02);
			
			/*while (true) {
				BidDetails bd = getRandomBid(randBidRange);
				System.out.println("Generated random bid within range: " + bd.getMyUndiscountedUtil());
				
				if (!isAlreadyOffered(bd)) {
					// Add selected bid to history
					biddingHistory.add(bd);
					
					// Return the random bid
					return bd; 
				} else {
					// Bid was already offered, generate new one...
					System.out.println("Generating NEW random bid since current was already offered!");
				}
			}*/
			
			BidDetails bd = getRandomBid(randBidRange);
			return bd;
			
			
		} else if (curPhase == 2) {
			// Second negotiation phase (implemented by Arnold)
			
			List<BidDetails> lastOpponentBids = negotiationSession.getOpponentBidHistory().sortToTime().getHistory();
			Double lastOwnUtil = negotiationSession.getOwnBidHistory().getLastBidDetails().getMyUndiscountedUtil();
			//Calculate difference between last bid and before last bid
			if (lastOpponentBids.size() > 0){
				double difference = lastOpponentBids.get(0).getMyUndiscountedUtil() - lastOpponentBids.get(1).getMyUndiscountedUtil();
				double nextBidUtil;
				
				//The opponent is approaching us in utility
				if (difference>0)
					nextBidUtil = Math.max(lastOwnUtil-(difference/tft1),p(time));
				
				//The opponent is going away from us in utility
				else
					nextBidUtil = Math.max(lastOwnUtil-(difference/tft2),p(time));
				
				Range r = new Range(nextBidUtil-phase2range, nextBidUtil+phase2range);
				
				Double temp = new Double(nextBidUtil);
				Double range2 = new Double(phase2range);
				System.out.println("I want an utility of: " + temp.toString() + " range: " + range2);
				List<BidDetails> bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);

				if (bidsInRange.size() == 0) { // do standard bid because we dont have any choices
				nextBid = outcomespace.getBidNearUtility(nextBidUtil); 
				} else { // do an intelligent bid since we have choiches!
				
					Double sizeList = new Double(bidsInRange.size());
					System.out.println("Number of bids found that are in range:" + sizeList.toString());
					
					OpponentBidCompare comparebids = new OpponentBidCompare();
					comparebids.setOpponentModel(opponentModel);
					
					Collections.sort(bidsInRange, comparebids);
					
					nextBid = bidsInRange.get(0);
				}
				
				
				//nextBid = outcomespace.getBidNearUtility(nextBidUtil); // TODO: find bid that opponenet likes using OM
				//nextBid = opponentModel.getBid(outcomespace, nextBidUtil);
				System.out.print("("+difference + "," + nextBidUtil+"),");
				// System.out.print(p(time) +", ");
			}
			else{
				nextBid = negotiationSession.getOutcomeSpace().getBidNearUtility(p(time));
			}
			return nextBid;
			
		} else if (curPhase == 3) {
			// Final negotiation phase
			// TODO: implemented this based on Acceptance Strategy
			
			
			// For now, we just return a random bid...
			return getRandomBid(0.4, 0.6);
			
		}
		
		// Never used :-)
		return getRandomBid(0.0, 1.0);
	}
	
	/**
	 * From [1]:
	 * 
	 * A wide range of time dependent functions can be defined by varying the way in
	 * which f(t) is computed. However, functions must ensure that 0 <= f(t) <= 1,
	 * f(0) = k, and f(1) = 1.
	 * 
	 * That is, the offer will always be between the value range, 
	 * at the beginning it will give the initial constant and when the deadline is reached, it
	 * will offer the reservation value.
	 * 
	 * For 0 < e < 1 it will behave as a Hardliner / Hardheader / Boulware
	 * For e = 1 it will behave as a lineair agent
	 * For e > 1 it will behave as a conceder (it will give low utilities faster than lineair)                 
	 */
	public double f(double t)
	{
		if (e == 0)
			return k;
		double ft = k + (1 - k) * Math.pow(t, 1.0/e);
		return ft;
	}

	/**
	 * Makes sure the target utility with in the acceptable range according to the domain
	 * Goes from Pmax to Pmin!
	 * @param t
	 * @return double
	 */
	public double p(double t) {
		return phase2LowerBound + (Pmax - phase2LowerBound) * (1 - f(t));
	}
	
	public BidDetails getRandomBidFirstPhase () {
		
		Random randgen = new Random();
		// Generate random number between 0.9 and 1.
		double utilGoal = 0.9+(randgen.nextInt(1)/100);
		
		BidDetails bd = negotiationSession.getOutcomeSpace().getBidNearUtility(utilGoal);
		return bd;
		
	}
	
	/**
	 * Returns a random bid within the range [lb, ub]
	 * 
	 * @param lb
	 * @param ub
	 */
	public BidDetails getRandomBid (Range r) {
		
//		System.out.println("################################################");
//		System.out.println("Generating random bids within range [" + lb + ", " + ub + "]");

		List<BidDetails> bidsInRange = negotiationSession.getOutcomeSpace().getBidsinRange(r);
		
		// Just for testing, print all bids in range
		//for (BidDetails b : bidsInRange) {
		//	System.out.println("Found bid: " + b.getMyUndiscountedUtil());
		//}
		
		int numBids = bidsInRange.size(); // Number of found bids
		BidDetails randBid;
		
//		System.out.println("Found " + numBids + " within range.");
		
		if (numBids > 0) {
			// One or more bids within range are found.
			// Select a random bid and return it.
			Random randgen = new Random();
			randBid = bidsInRange.get(randgen.nextInt(numBids));
			
			//System.out.println("Selected random bid with utility " + randBid.getMyUndiscountedUtil());
			
		} else {
			// No bids within range are found, now we selected the bid that is closest 
			// to the UPPER bound of the given range.
			randBid = negotiationSession.getOutcomeSpace().getBidNearUtility(r.getUpperbound());
			
			//System.out.println("No bids found, selecting bid closest to upper bound: " + randBid.getMyUndiscountedUtil());
		}
		
		//		System.out.println("################################################");
		return randBid;
		
	}
	
	public BidDetails getRandomBid (double lb, double ub) {
		Range r = new Range(lb, ub);
		return getRandomBid(r);
	}
	
	/**
	 * Returns the current phase of the negotiation session
	 * based on the elapsed time. Phases are 1, 2 or 3.
	 * 
	 * @return
	 */
	public int getNegotiationPhase () {
		double time = negotiationSession.getTime(); // Normalized time [0,1]
		int p = 0;
		
		if (time <= phaseBoundary[0]) 
			return 1;
		else if (time > phaseBoundary[0] && time <= phaseBoundary[1])
			return 2;
		else if (time > phaseBoundary[1])
			return 3;
		
		return 0;
	}
	
	public Range getRangeFunctionFirstPhase (double t, double margin) {

		double normTime = t/phaseBoundary[0]; // Normalized time
		
		double val = 1-(normTime/10);
		Range r = new Range(val-margin, val+margin);
		
		// Set upper bound to 1 is exceeds
		if (r.getUpperbound() > 1) r.setUpperbound(1.0);
		
		System.out.println("Calculated range for t = " + normTime + ", ["+r.getLowerbound()+","+r.getUpperbound()+"]");
		
		return r;
	}
	
	public boolean isAlreadyOffered (BidDetails bd) {
		
		List<BidDetails> historyList = biddingHistory.getHistory();
		if (historyList.contains(bd)) return true;
		
		return false;
	}
	
	
	/**
	 * 	- Opdelen in drie fases op basis van tijd (en later ook van discount)
	 *  - Met hoge biedeingen beginnen in eerste fase (+90%)
	 *  - De reactie van hoge biedingen gebruiken om strategie/preference van opponent te bepalen
	 *  
	 *  - Combineren van twee strategien:
	 *  	- Conceder --> Hard-Headed
	 *  	- Hard-Headed --> Tit-for-That
	 * 
	 * 	- Fase 1: hoge biedingen 90%
	 *  - Fase 2: afhankelijk van strategy HH/TfT
	 *  - Fase 3: acceptance strategy
	 *  
	 *  Nash Point schatten a.d.h. preference profile
	 *  Paar functies (sin/exp) implementeren en kijken hoe je de OS fuckt
	 *  
	 *  Runnen: saven in Eclipse, class file in Genius laden (TomV maakt XML)
	 *  
	 */
}

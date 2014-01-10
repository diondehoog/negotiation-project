package negotiator.group7;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;
import negotiator.boaframework.opponentmodel.NashFrequencyModel;
import negotiator.issue.Issue;
import negotiator.utility.UtilitySpace;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class BidSaverFrequencyModel extends OpponentModel {
	private OpponentModel curOM;
	private List<Double> oppBids;
	private boolean inits = false;
	private String date;
	
	@Override
	public void init(NegotiationSession negotiationSession) {
		System.out.println("Initializing...");
		this.negotiationSession = negotiationSession;
		this.curOM = new NashFrequencyModel();
		this.curOM.init(negotiationSession);
		this.oppBids = new ArrayList<Double>();
		Calendar cal = Calendar.getInstance();
		date = new SimpleDateFormat("yyyyMMddHHmmss").format(cal.getTime());
		inits = true;
	}
	
	@Override
	public void init(NegotiationSession negotiationSession,
            java.util.HashMap<java.lang.String,java.lang.Double> parameters) throws Exception{
		System.out.println("Initializing...");
		this.negotiationSession = negotiationSession;
		this.curOM = new NashFrequencyModel();
		this.curOM.init(negotiationSession, parameters);
		this.oppBids = new ArrayList<Double>();
		Calendar cal = Calendar.getInstance();
		date = new SimpleDateFormat("yyyyMMddHHmmss").format(cal.getTime());
		inits = true;
	}
	
	@Override
	public void updateModel(Bid bid, double time) {
		if (inits)
		{
			writeOwnBids();
			curOM.updateModel(bid, time);
			oppBids.add(curOM.getBidEvaluation(bid));		
			writeEstimatedOpponentBids();
		} else
		{
			System.out.println("Initialization not complete :'(");
		}
	}
	
	private void writeOwnBids()
	{
		if (this.negotiationSession == null)
			return;
		BidHistory bh = this.negotiationSession.getOwnBidHistory();
		bh.sortToTime();
		List<BidDetails> all = bh.getHistory();
		try
		{
			File file = new File("/home/immortaly007/workspace/Log/OwnHistory" + date + ".txt");
			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			System.out.println("File created/opened or whatever");
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			Iterator<BidDetails> bidIter = all.iterator();
			while(bidIter.hasNext()) {
				bw.write(bidIter.next().getMyUndiscountedUtil() + ";");
			}
			bw.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getStackTrace());
		}
	}
	
	
	public void writeEstimatedOpponentBids()
	{
		try
		{
			File file = new File("/home/immortaly007/workspace/Log/OtherHistory" + date + ".txt");
			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			Iterator<Double> bidIter = oppBids.iterator();
			while(bidIter.hasNext())
			{
				bw.write(bidIter.next() + ";");
			}
			bw.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getStackTrace());
		}
	}
	
	@Override
	public double getBidEvaluation(Bid bid) {
		return curOM.getBidEvaluation(bid);
	}
	
	@Override
	public UtilitySpace getOpponentUtilitySpace() {
		return curOM.getOpponentUtilitySpace();
	}
	
	@Override
	public void setOpponentUtilitySpace(negotiator.protocol.BilateralAtomicNegotiationSession fNegotiation)
	{
		curOM.setOpponentUtilitySpace(fNegotiation);
	}
	
	@Override
	public void setOpponentUtilitySpace(UtilitySpace opponentUtilitySpace) {
		curOM.setOpponentUtilitySpace(opponentUtilitySpace);
	}
	
	@Override
	public double getWeight(Issue issue) {
		return curOM.getWeight(issue);
	}
	
	@Override
	public double[] getIssueWeights() {
		return curOM.getIssueWeights();
	}
	
	@Override
	public void cleanUp() {
		curOM.cleanUp();
	}
	
	@Override
	public boolean isCleared() {
		return curOM.isCleared();
	}
	
	@Override
	public String getName()
	{
		return "BidSaverFrequencyModel";
	}

}

package firms;

import java.util.ArrayList;
import java.util.HashMap;

import firmState.FirmState;
import firmState.Offer;

public class FirmHistory extends ArrayList<FirmState> {

	public static final int HISTORY_SIZE = 10;
	private static final long serialVersionUID = 1L;

	// It has the last profit obtained when offering offer
	private HashMap<Offer, Double> profitMap;

	public FirmHistory(int historySize) {
		super(historySize);
		profitMap = new HashMap<Offer, Double>();
	}

	public boolean addCurrentState(FirmState firmState) {
		// List is full remove first element to enter
		if (size() == HISTORY_SIZE)
			remove(0);

		return add(firmState);

	}

	public double getHistoricalProfit(Offer offer) {
		return profitMap.get(offer);
	}

	public boolean containsOffer(Offer o) {
		for (FirmState st : this)
			if (st.getOffer() == o)
				return true;

		return false;
	}

	private FirmState getCurrentState() {
		if (isEmpty())
			return null;
		else
			return get(size() - 1);
	}

	public double getCurrentQualityStep() {
		FirmState prevST = getPrevState();

		if (prevST != null) {
			double curQ = getCurrentQuality();
			double prevQ = prevST.getQuality();
			return curQ - prevQ;
		} else
			return 0;

	}

	public double getCurrentPriceStep() {
		FirmState prevST = getPrevState();
		
		if (prevST != null) {
			double curP = getCurrentPrice();
			double prevP = getPrevState().getPrice();
			return curP - prevP;
		} else
			return 0;
	}

	public FirmState getPrevState() {
		if (size() > 1)
			return get(size() - 2);
		else
			return null;
	}

	public Offer getMaxProfitOffer() {
		if (isEmpty())
			return null;
		else {
			Offer maxOffer = get(0).getOffer();

			for (FirmState st : this.subList(1, size())) {
				if (profitMap.get(maxOffer) < profitMap.get(st.getOffer()))
					maxOffer = st.getOffer();
			}
			return maxOffer;
		}
	}

	public double getCurrentPrice() {
		return getCurrentState().getPrice();
	}

	public double getCurrentQuality() {
		return getCurrentState().getQuality();
	}

	public double getCurrentProfit() {
		return getCurrentState().getProfit();
	}

	public void setCurrentProfit(double profit) {
		FirmState st = getCurrentState();
		profitMap.put(st.getOffer(), profit);
		st.setProfit(profit);
	}

	public void setCurrentDemand(int i) {
		getCurrentState().setDemand(i);
	}

	public int getCurrentDemand() {
		return getCurrentState().getDemand();
	}

	public Offer getCurrentOffer() {
		return getCurrentState().getOffer();
	}

}
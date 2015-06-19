package firmState;

public class FirmState {
	
	public class MarketResponse {
		private int demand = 0;
		private double profit = 0.0;
		
	}

	private Offer offer;
	private MarketResponse mktResponse;
	
	public FirmState() {
		offer = new Offer();
		mktResponse = new MarketResponse();
	}

	public FirmState(Offer offer) {
		this.offer = offer;
		mktResponse = new MarketResponse();
	}

	public double getPrice() {
		return offer.getPrice();
	}

	public void setPrice(double price) {
		offer.setPrice(price);
	}

	public double getQuality() {
		return offer.getQuality();
	}

	public void setQuality(double quality) {
		offer.setQuality(quality);
	}

	public double getProfit() {
		return mktResponse.profit;
	}

	public void setProfit(double profit) {
		this.mktResponse.profit = profit;
	}

	public Offer getOffer() {
		return offer;
	}

	public void setOffer(Offer product) {
		this.offer = product;
	}

	public int getDemand() {
		return mktResponse.demand;
	}

	public void setDemand(int demand) {
		this.mktResponse.demand = demand;
	}

	public String toString() {
		return offer.toString() + " Prof: " + getProfit() + " D: " + getDemand();
	}

}

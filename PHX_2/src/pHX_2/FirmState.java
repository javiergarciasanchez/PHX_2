package pHX_2;

import offer.Offer;
import offer.OfferType;


public class FirmState {
	
	private Offer offer;
	private double profit = 0.;
	private int demand = 0;

	public FirmState() {
		offer = new Offer();
	}

	public FirmState(Offer offer) {
		this.offer = offer;
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
		return profit;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}

	public Offer getOffer() {
		return offer;
	}

	public void setOffer(Offer product) {
		this.offer = product;
	}

	public int getDemand() {
		return demand;
	}

	public void setDemand(int demand) {
		this.demand = demand;
	}

	public OfferType getOfferType() {
		return offer.getOfferType();
	}

	public void setOfferType(OfferType offerType) {
		offer.setOfferType(offerType);
	}

	public String toString() {
		return offer.toString() + " Prof: " + profit + " D: " + demand;
	}

}

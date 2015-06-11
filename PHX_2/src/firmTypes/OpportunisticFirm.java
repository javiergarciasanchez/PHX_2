package firmTypes;

import java.awt.Color;

import firmState.Offer;
import firmState.OfferType;
import firms.Firm;
import repast.simphony.random.RandomHelper;

// This firm offers high price and low quality
public class OpportunisticFirm extends Firm {

	public OpportunisticFirm() {
		super();
	}
	
	@Override
	protected Offer getInitialOffer() {
		
		double q = getRandomInitialQuality();
		Offer offer = new Offer(q, getRandomInitialPrice(q));
		return offer;

	}
	
	private double getRandomInitialQuality() {
		double lowerQ = Offer.getMinQuality();
		double higherQ = (Offer.getMinQuality() + Offer.getMaxQuality()) / 2.0;
		return Firm.getRandomInitialQuality(lowerQ, higherQ);
	}

	private double getRandomInitialPrice(double q) {

		// Chooses a high price to take advantage of consumer ignorance
		
		// it uses default Uniform distribution
		double higherPrice = Offer.getMaxPrice();
		double lowerPrice = Math.max(unitCost(q), (Offer.getMinPrice() + Offer.getMaxPrice()) / 2.0);

		return RandomHelper.nextDoubleFromTo(lowerPrice, higherPrice);

	}

	@Override
	public Color getColor() {
		return Color.RED;
	}

	@Override
	protected void fillOfferTypePreference() {
		
		offerTypePreference[0] = OfferType.INCREASE_PRICE;
		offerTypePreference[1] = OfferType.DECREASE_QUALITY;
		offerTypePreference[2]= OfferType.DECREASE_PRICE;
		offerTypePreference[3] = OfferType.INCREASE_QUALITY;
		
	}
}

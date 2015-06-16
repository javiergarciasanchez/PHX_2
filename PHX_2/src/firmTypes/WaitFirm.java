package firmTypes;

import java.awt.Color;

import firmState.Offer;
import firmState.OfferType;
import firms.Firm;
import firms.Utils;
import repast.simphony.random.RandomHelper;

// This firm firmState low price and high quality
public class WaitFirm extends Firm {

	public WaitFirm() {
		super();
	}

	@Override
	protected Offer getInitialOffer() {
		
		double q = getRandomInitialQuality();
		Offer offer = new Offer(q, getRandomInitialPrice(q));
		return offer;

	}
	
	private double getRandomInitialQuality() {
		double lowerQ = (Offer.getMinQuality() + Offer.getMaxQuality()) / 2.0;;
		double higherQ = Offer.getMaxQuality();

		return Utils.getRandomInitialQuality(lowerQ, higherQ);
	}

	private double getRandomInitialPrice(double q) {
		double lowerPrice, higherPrice;

		// Chooses a LOW price to take advantage of big base of the pyramid and
		// to attract consumers during a crisis
		// it uses the default Uniform Distribution 
		higherPrice = (Offer.getMinPrice() + Offer.getMaxPrice()) / 2.0;
		lowerPrice = unitCost(q);

		return RandomHelper.nextDoubleFromTo(lowerPrice, higherPrice);

	}
	
	@Override
	public Color getColor() {
		return Color.GREEN;
	}

	@Override
	protected void fillOfferTypePreference() {
		offerTypePreference[0]= OfferType.DECREASE_PRICE;
		offerTypePreference[3] = OfferType.INCREASE_QUALITY;		
		offerTypePreference[2] = OfferType.INCREASE_PRICE;
		offerTypePreference[1] = OfferType.DECREASE_QUALITY;
	}

}

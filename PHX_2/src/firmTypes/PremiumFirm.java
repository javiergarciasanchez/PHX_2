package firmTypes;

import offer.Offer;
import pHX_2.Firm;
import pHX_2.Market;
import repast.simphony.random.RandomHelper;

// This firm offer high price and high quality
public class PremiumFirm extends Firm {

	public PremiumFirm() {
		super();
	}

	protected double getRandomInitialQuality() {
		double lowerQ = (Offer.getMinQuality() + Offer.getMaxQuality()) / 2.0;
		double higherQ = Offer.getMaxQuality();
		return getRandomInitialQuality(lowerQ, higherQ);
	}

	protected double getRandomInitialPrice(double q) {
		Firm lowerComp, higherComp;
		double lowerPrice, higherPrice;

		// Chooses a random price in the range delimited by lower and higher
		// competitors
		// If any of the competitors does not exists, absolute price limits are
		// used
		lowerComp = Market.firms.getPrevQFirm(q);
		higherComp = Market.firms.getNextQFirm(q);
		double midPrice = (Offer.getMinPrice() + Offer.getMaxPrice()) / 2.0;

		// Set lower price limit
		lowerPrice = Math.max(unitCost(q), midPrice);

		if (lowerComp != null)
			lowerPrice = Math.max(lowerComp.getPrice(), lowerPrice);

		// Set higher price limit		
		if (higherComp != null && higherComp.getPrice() > lowerPrice )
			higherPrice = higherComp.getPrice();
		else
			higherPrice = Offer.getMaxPrice();
			

		// Uses default Uniform distribution
		return RandomHelper.nextDoubleFromTo(lowerPrice, higherPrice);

	}
}

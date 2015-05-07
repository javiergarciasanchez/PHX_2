package firmTypes;

import offer.Offer;
import pHX_2.Firm;
import pHX_2.Market;
import repast.simphony.random.RandomHelper;

// This firm offers low price and low quality
public class BasePyramidFirm extends Firm {

	public BasePyramidFirm() {
		super();
	}

	protected double getRandomInitialQuality() {
		double lowerQ = Offer.getMinQuality();
		double higherQ = (Offer.getMinQuality() + Offer.getMaxQuality()) / 2.0;
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
		lowerPrice = unitCost(q);

		if (lowerComp != null)
			lowerPrice = Math.max(lowerComp.getPrice(), lowerPrice);

		
		// Set higher price limit
		higherPrice = Math.max(lowerPrice, midPrice);
		
		if (higherComp != null && higherComp.getPrice() > lowerPrice )
			higherPrice = Math.min(higherPrice, higherComp.getPrice());

		
		// Uses default Uniform distribution
		return RandomHelper.nextDoubleFromTo(lowerPrice, higherPrice);

	}

}

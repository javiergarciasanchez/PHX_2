package pHX_2;

// This firm offers high price and low quality
public class OpportunisticFirm extends Firm {

	public OpportunisticFirm() {
		super();
	}

	protected double getRandomInitialQuality() {
		// Uses BetaSub distribution between 0 and 1

		double tmpQ;
		double lowerQ = Offer.getMinQuality();
		double higherQ = Offer.getMaxQuality();

		// Quality should be checked for existence
		// because two different firms cannot have the same quality
		do {

			tmpQ = Firms.getLowQualityDistrib().nextDouble()
					* (higherQ - lowerQ) + lowerQ;

		} while (Firms.containsQ(tmpQ));

		return tmpQ;

	}

	protected double getRandomInitialPrice(double q) {
		double lowerPrice, higherPrice;

		// Chooses a high price to take advantage of consumer high expectations
		// it uses the high initial price distribution, with limits: max price
		// and unit cost
		higherPrice = Offer.getMaxPrice();
		lowerPrice = unitCost(q);

		// Uses BetaSub distributions between 0 and 1, and is rescaled
		return Firms.getHighInitialPriceDistrib().nextDouble()
				* (higherPrice - lowerPrice) + lowerPrice;

	}

}

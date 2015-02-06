package pHX_2;

// This firm offer low price and high quality
public class WaitFirm extends Firm {

	public WaitFirm() {
		super();
	}

	protected double getRandomInitialQuality() {
		// HIGH QUALITY

		// Uses high quality BetaSub distribution between 0 and 1

		double tmpQ;
		double lowerQ = Offer.getMinQuality();
		double higherQ = Offer.getMaxQuality();

		// Quality should be checked for existence
		// because two different firms cannot have the same quality
		do {

			tmpQ = Firms.getHighQualityDistrib().nextDouble()
					* (higherQ - lowerQ) + lowerQ;

		} while (Firms.containsQ(tmpQ));

		return tmpQ;

	}

	protected double getRandomInitialPrice(double q) {
		double lowerPrice, higherPrice;

		// Chooses a LOW price to take advantage of big base of the pyramid and
		// to attract consumers during a crisis
		// it uses the low initial price distribution, with limits: max price
		// and unit cost
		higherPrice = Offer.getMaxPrice();
		lowerPrice = unitCost(q);

		// Uses BetaSub distributions between 0 and 1, and is rescaled
		return Firms.getLowInitialPriceDistrib().nextDouble()
				* (higherPrice - lowerPrice) + lowerPrice;

	}

}

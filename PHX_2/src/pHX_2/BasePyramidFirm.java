package pHX_2;

// This firm offers low price and low quality
public class BasePyramidFirm extends Firm {

	public BasePyramidFirm() {
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

		} while (Firms.sortQFirms.containsKey(tmpQ));

		return tmpQ;

	}

	protected double getRandomInitialPrice(double q) {
		Firm lowerComp, higherComp;
		double lowerPrice, higherPrice;

		// Chooses a random price in the range delimited by lower and higher
		// competitors
		// If any of the competitors does not exists, absolute price limits are
		// used
		lowerComp = getLowerCompetitor(q);
		higherComp = getHigherCompetitor(q);

		// Set lower price limit
		if (lowerComp != null)
			lowerPrice = lowerComp.getPrice();
		else
			lowerPrice = Offer.getMinPrice();

		// checks that lower price is above variable unit cost
		lowerPrice = Math.max(unitCost(q), lowerPrice);

		// Set higher price limit
		if (higherComp != null)
			higherPrice = higherComp.getPrice();
		else
			higherPrice = Offer.getMaxPrice();

		// Uses BetaSub distribution for LOW INITIAL PRICE, between 0 and 1, and
		// is rescaled
		return Firms.getLowInitialPriceDistrib().nextDouble()
				* (higherPrice - lowerPrice) + lowerPrice;

	}
	
}

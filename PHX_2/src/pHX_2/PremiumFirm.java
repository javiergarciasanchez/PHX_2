package pHX_2;

public class PremiumFirm extends Firm {

	public PremiumFirm(Market market) {
		super(market);
	}

	@Override
	public void makeInitialOffer() {
		// This firm offer high price and high quality
		firmState.product.setPrice(market.referenceProductForFirms.getPrice()
				* (1 + market.priceStepDistrib.nextDouble()));

		firmState.product.setQuality(market.referenceProductForFirms
				.getQuality() * (1 + market.qualityStepDistrib.nextDouble()));

	}

	@Override
	public void makeOffer() {
		// This firm offers high price and hig quality
		// so it tries to increase price
		offer(1);

	}

}

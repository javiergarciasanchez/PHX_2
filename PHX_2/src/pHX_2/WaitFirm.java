package pHX_2;

public class WaitFirm extends Firm {

	public WaitFirm(Market market) {
		super(market);
	}

	@Override
	public void makeInitialOffer() {
		// This firm offer low price and high quality
		firmState.product.setPrice(market.referenceProductForFirms.getPrice()
				* (1 - market.priceStepDistrib.nextDouble()));

		firmState.product.setQuality(market.referenceProductForFirms
				.getQuality() * (1 + market.qualityStepDistrib.nextDouble()));

	}

	@Override
	public void makeOffer() {
		// This firm offers low price and high quality
		// so it tries to decrease price
		offer(-1);

	}

}

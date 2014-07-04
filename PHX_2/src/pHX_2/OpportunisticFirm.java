package pHX_2;

public class OpportunisticFirm extends Firm {

	public OpportunisticFirm(Market market) {
		super(market);
	}

	@Override
	public void makeInitialOffer() {

		// This firm offers high price and low quality
		firmState.product.setPrice(market.referenceProductForFirms.getPrice()
				* (1 + market.priceStepDistrib.nextDouble()));

		firmState.product.setQuality(market.referenceProductForFirms
				.getQuality() * (1 - market.qualityStepDistrib.nextDouble()));

	}

	@Override
	public void makeOffer() {
		// This firm offer high price and low quality
		// so it tries to increase price
		offer(1);

	}

}

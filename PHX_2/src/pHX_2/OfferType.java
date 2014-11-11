package pHX_2;

import repast.simphony.random.RandomHelper;
import cern.jet.random.Uniform;

public enum OfferType {
	INCREASE_PRICE, DECREASE_PRICE, INCREASE_QUALITY, DECREASE_QUALITY;

	// Create distribution for Random change to be added
	private static final Uniform changeTypeDistrib = RandomHelper
			.createUniform(1, 4);

	public static OfferType getRandomOfferType() {

		switch (changeTypeDistrib.nextInt()) {
		case 1:
			return OfferType.INCREASE_PRICE;
		case 2:
			return OfferType.DECREASE_PRICE;
		case 3:
			return OfferType.INCREASE_QUALITY;
		case 4:
			return OfferType.DECREASE_QUALITY;
		default:
			return null;
		}

	}
	
}

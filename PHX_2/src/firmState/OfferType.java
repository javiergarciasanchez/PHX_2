package firmState;

import repast.simphony.random.RandomHelper;

public enum OfferType {
	INCREASE_PRICE, DECREASE_PRICE, INCREASE_QUALITY, DECREASE_QUALITY;

	public static OfferType getRandomOfferType() {

		switch (RandomHelper.nextIntFromTo(1,4)) {
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

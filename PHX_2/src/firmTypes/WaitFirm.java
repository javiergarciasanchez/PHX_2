package firmTypes;

import java.awt.Color;

import offer.Offer;
import pHX_2.Firm;
import repast.simphony.random.RandomHelper;

// This firm offer low price and high quality
public class WaitFirm extends Firm {

	public WaitFirm() {
		super();
	}

	protected double getRandomInitialQuality() {
		double lowerQ = (Offer.getMinQuality() + Offer.getMaxQuality()) / 2.0;;
		double higherQ = Offer.getMaxQuality();

		return getRandomInitialQuality(lowerQ, higherQ);
	}

	protected double getRandomInitialPrice(double q) {
		double lowerPrice, higherPrice;

		// Chooses a LOW price to take advantage of big base of the pyramid and
		// to attract consumers during a crisis
		// it uses the default Uniform Distribution 
		higherPrice = (Offer.getMinPrice() + Offer.getMaxPrice()) / 2.0;
		lowerPrice = unitCost(q);

		return RandomHelper.nextDoubleFromTo(lowerPrice, higherPrice);

	}
	
	@Override
	public Color getColor() {
		return Color.GREEN;
	}

}

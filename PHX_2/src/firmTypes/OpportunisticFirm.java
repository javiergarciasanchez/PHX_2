package firmTypes;

import java.awt.Color;

import firmState.Offer;
import firms.Firm;
import firms.Utils;
import repast.simphony.random.RandomHelper;

// This firm offers high price and low quality
public class OpportunisticFirm extends Firm {

	public OpportunisticFirm() {
		super();
	}
	
	protected double getInitialQuality() {
		double lowerQ = Offer.getMinQuality();
		double higherQ = (Offer.getMinQuality() + Offer.getMaxQuality()) / 2.0;
		return Utils.getRandomInitialQuality(lowerQ, higherQ);
	}

	protected double getInitialPrice(double q) {

		// Chooses a high price to take advantage of consumer ignorance
		
		// it uses default Uniform distribution
		double higherPrice = Offer.getMaxPrice();
		double lowerPrice = Math.max(unitCost(q), (Offer.getMinPrice() + Offer.getMaxPrice()) / 2.0);

		return RandomHelper.nextDoubleFromTo(lowerPrice, higherPrice);

	}

	@Override
	public Color getColor() {
		return Color.RED;
	}

}

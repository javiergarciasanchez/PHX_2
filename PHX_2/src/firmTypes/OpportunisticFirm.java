package firmTypes;

import java.awt.Color;

import pHX_2.Market;
import firmState.FirmState;
import firmState.Offer;
import firms.Firm;
import firms.Utils;
import repast.simphony.random.RandomHelper;

// This firm takes advantage of consumer ignorance
public class OpportunisticFirm extends Firm {

	public OpportunisticFirm() {
		super();
	}

	protected double getInitialQuality() {
		// Gets a low quality
		double lowerQ = Offer.getMinQuality();
		double higherQ = (Offer.getMinQuality() + Offer.getMaxQuality()) / 2.0;
		return Utils.getRandomInitialQuality(lowerQ, higherQ);
	}

	@Override
	protected double getInitialPrice(double q) {
		// Chooses a high price to take advantage of consumer ignorance

		// it uses default Uniform distribution
		double higherPrice = Offer.getMaxPrice();
		double lowerPrice = Math.max(unitCost(q),
				(Offer.getMinPrice() + Offer.getMaxPrice()) / 2.0);

		return RandomHelper.nextDoubleFromTo(lowerPrice, higherPrice);
	}

	/*
	 * The new offer is identical to the current one.
	 * 
	 * it removes the firm from FirmsByQ and adds the firm back to it with the
	 * new offer
	 */
	@Override
	protected void setNewOffer() {

		Market.firms.removeFromFirmsByQ(this);

		Offer o = new Offer(getCurrentOffer());

		getHistory().addCurrentState(new FirmState(o));

		Market.firms.addToFirmsByQ(this);
	}

	@Override
	public Color getColor() {
		return Color.RED;
	}

}

package firmTypes;

import java.awt.Color;

import firmHistory.Offer;
import firms.Firm;
import firms.Utils;

// This firm maximizes assuming consumers with full knowledge
public class RationalFirm extends Firm {

	public RationalFirm() {
		super();
	}

	@Override
	protected double getInitialPrice(double q) {
		double p;

		try {
			p = Utils.getRationalPrice(unitCost(q), q);
		} catch (NoPrice e) {
			// Put any price because there is no meaningful price
			p = (Offer.getMaxPrice() + Offer.getMinPrice()) / 2.0;
		}

		return p;

	}

	/*
	 * it adds the new offer to history
	 * 
	 * it removes the firm from FirmsByQ and adds the firm back to it with the
	 * new offer
	 */
	@Override
	protected void setNextOffer() {
		Utils.setNewRationalOffer(this);
	}

	@Override
	public Color getColor() {
		return Color.BLUE;
	}

}

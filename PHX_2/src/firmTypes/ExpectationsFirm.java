package firmTypes;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.awt.Color;

import pHX_2.Market;
import firmHistory.FirmState;
import firmHistory.Offer;
import firms.Firm;
import firms.Utils;

// This firm takes into account consumer ignorance
public class ExpectationsFirm extends Firm {

	public ExpectationsFirm() {
		super();
	}

	@Override
	protected double getInitialPrice(double q) {
		double p;

		double expQ = expectedQ(q);

		try {
			p = Utils.getRationalPrice(unitCost(expQ), expQ);
		} catch (NoPrice e) {
			// Put any price because there is no meaningful price
			p = (Offer.getMaxPrice() + Offer.getMinPrice()) / 2.0;
		}

		return p;
	}

	@Override
	protected void setNewOffer() {
		/*
		 * it adds the new offer to history
		 * 
		 * it removes the firm from FirmsByQ and adds the firm back to it with
		 * the new offer
		 */
		if (recognized())

			Utils.setNewRationalOffer(this);

		else {
			// Assuming market does not recognize quality
			// Estimating rational price for mkt discounted quality
			Offer o;
			double q = getQuality();

			Market.firms.removeFromFirmsByQ(this);

			double p = getInitialPrice(expectedQ(q));
			o = new Offer(q, p);

			getHistory().addCurrentState(new FirmState(o));

			Market.firms.addToFirmsByQ(this);

		}

	}

	private boolean recognized() {

		double recognitionThreshold = (Double) GetParameter("recognitionThreshold");

		double theoDemand = Utils
				.getTheoreticalDemand(getQuality(), getPrice());

		return (getDemand() >= recognitionThreshold * theoDemand);

	}

	private double expectedQ(double q) {

		return (Double) GetParameter("qualityDiscountMean") * q;

	}

	@Override
	public Color getColor() {
		return Color.GREEN;
	}
}

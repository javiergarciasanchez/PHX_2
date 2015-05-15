package graphs;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import consumers.Consumer;
import consumers.Consumers;
import consumers.Pareto;
import firms.Firm;
import offer.Offer;
import pHX_2.Market;
import repast.simphony.context.space.continuous.ContextSpace;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.continuous.StickyBorders;

public class ConsumptionProjection {

	private static final double MAX_X = 100, MAX_Y = 100, MAX_Z = 100;
	private static final double MIN_X = 0, MIN_Y = 0, MIN_Z = 0;

	private ContinuousSpace<Consumer> space;

	public ConsumptionProjection(Consumers consumers) {

		double[] dims = new double[3];
		dims[0] = MAX_X + 0.1;
		dims[1] = MAX_Y + 0.1;
		dims[2] = MAX_Z + 0.1;

		space = new ContextSpace<Consumer>("ConsumptionProjection",
				new SimpleCartesianAdder<Consumer>(), new StickyBorders(), dims);

		consumers.addProjection(space);

	}

	public void update(Consumer c) {
		Firm f;
		if (c.getChosenFirm() == null) {
			space.moveTo(c, 0.0, margUtilToCoord(c.getMargUtilOfQuality()), 0.0);
		} else {

			f = c.getChosenFirm();
			space.moveTo(c, priceToCoord(f.getPrice()),
					margUtilToCoord(c.getMargUtilOfQuality()),
					qualityToCoord(f.getQuality()));
		}
	}

	private int margUtilToCoord(double margUtilOfQuality) {

		return (int) Math.min(
				Math.round(margUtilOfQuality / getMaxUtilToDraw()
						* (MAX_Y - MIN_Y))
						+ MIN_Y, MAX_Y);
	}

	private double getMaxUtilToDraw() {
		// Assign minimum Marginal Utility of Quality for the segment
		double acumProb = (double) GetParameter("margUtilPercentToDraw");

		double gini = (double) GetParameter("gini");
		double lambda = (1.0 + gini) / (2.0 * gini);

		double minimum = Market.consumers.getMinMargUtilOfQuality();

		return Pareto.inversePareto(acumProb, minimum, lambda);
	}

	private double priceToCoord(double price) {
		return (price - Offer.getMinPrice())
				/ (Offer.getMaxPrice() - Offer.getMinPrice()) * (MAX_X - MIN_X)
				+ MIN_X;
	}

	private double qualityToCoord(double quality) {
		return MAX_Z
				- ((quality - Offer.getMinQuality())
						/ (Offer.getMaxQuality() - Offer.getMinQuality())
						* (MAX_Z - MIN_Z) + MIN_Z);
	}

}

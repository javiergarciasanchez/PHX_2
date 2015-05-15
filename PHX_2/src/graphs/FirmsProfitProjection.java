package graphs;

import firms.Firm;
import firms.Firms;
import offer.Offer;
import repast.simphony.context.space.continuous.ContextSpace;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.continuous.StickyBorders;

public class FirmsProfitProjection {

	private static final double MAX_X = 100, MAX_Y = 100, MAX_Z = 100;
	private static final double MIN_X = 0, MIN_Y = 0, MIN_Z = 0;
	private static final double MIN_PROFIT_TO_DRAW = -100.0,
			MAX_PROFIT_TO_DRAW = 1000.0;

	private ContinuousSpace<Firm> space;

	public FirmsProfitProjection(Firms firms) {

		double[] dims = new double[3];
		dims[0] = MAX_X + 0.1;
		dims[1] = MAX_Y + 0.1;
		dims[2] = MAX_Z + 0.1;

		space = new ContextSpace<Firm>("FirmsProfitProjection",
				new SimpleCartesianAdder<Firm>(), new StickyBorders(), dims);

		firms.addProjection(space);

	}

	public void update(Firm firm) {
		space.moveTo(firm, priceToCoord(firm.getPrice()),
				profitToCoord(firm.getProfit()),
				qualityToCoord(firm.getQuality()));
	}

	private double priceToCoord(double price) {
		return (price - Offer.getMinPrice())
				/ (Offer.getMaxPrice() - Offer.getMinPrice()) * (MAX_X - MIN_X)
				+ MIN_X;
	}

	private double profitToCoord(double profit) {
		double profitCoord = Math.min(MAX_PROFIT_TO_DRAW,
				Math.max(profit, MIN_PROFIT_TO_DRAW)) + ( -MIN_PROFIT_TO_DRAW );
		return profitCoord / (MAX_PROFIT_TO_DRAW - MIN_PROFIT_TO_DRAW)
				 * (MAX_Y - MIN_Y) + MIN_Y;
	}

	private double qualityToCoord(double quality) {
		return MAX_Z
				- ((quality - Offer.getMinQuality())
						/ (Offer.getMaxQuality() - Offer.getMinQuality())
						* (MAX_Z - MIN_Z) + MIN_Z);
	}

}

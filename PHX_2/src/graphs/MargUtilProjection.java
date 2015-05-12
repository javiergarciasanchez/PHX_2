package graphs;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import pHX_2.Firm;
import pHX_2.Firms;
import pHX_2.Market;
import demand.Consumer;
import demand.Pareto;
import repast.simphony.context.Context;
import repast.simphony.context.space.grid.ContextGrid;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.SingleOccupancyCellAccessor;
import repast.simphony.space.grid.StickyBorders;

public class MargUtilProjection {

	private static final int MAX_X = 100, MAX_Y = 30;
	private static final int MIN_X = 0;
	private static final int FIRMS_HEIGHT = 4;

	private ContextGrid<Object> space;

	public MargUtilProjection(Context<Object> context) {

		int[] dims = new int[2];
		dims[0] = MAX_X + 1;
		dims[1] = MAX_Y + FIRMS_HEIGHT + 1;

		space = new ContextGrid<Object>("MargUtilProjection",
				new SimpleGridAdder<Object>(), new StickyBorders(),
				new SingleOccupancyCellAccessor<Object>(), dims);

		context.addProjection(space);

	}

	public void add(Consumer c) {
		double mUtil = c.getMargUtilOfQuality();
		space.moveTo(c, margUtilToCoord(mUtil), getY(mUtil));

	}

	private int margUtilToCoord(double margUtilOfQuality) {

		return (int) Math.min(
				Math.round(margUtilOfQuality / getMaxUtilToDraw()
						* (MAX_X - MIN_X))
						+ MIN_X, MAX_X);
	}

	private int getY(double mUtil) {

		int[] dims = new int[2];

		dims[0] = margUtilToCoord(mUtil);
		dims[1] = FIRMS_HEIGHT;

		while (space.getObjectAt(dims) != null) {
			dims[1] += 1;
		}

		return dims[1];
	}

	public void update(Firm f) {
		space.moveTo(f, margUtilToCoord(Firms.getPoorestConsumerMargUtil(
				f.getQuality(), f.getPrice())), FIRMS_HEIGHT / 2 - 1);
	}

	private double getMaxUtilToDraw() {
		// Assign minimum Marginal Utility of Quality for the segment
		double acumProb = (double) GetParameter("margUtilPercentToDraw");

		double gini = (double) GetParameter("gini");
		double lambda = (1.0 + gini) / (2.0 * gini);

		double minimum = Market.consumers.getMinMargUtilOfQuality();

		return Pareto.inversePareto(acumProb, minimum, lambda);
	}

}

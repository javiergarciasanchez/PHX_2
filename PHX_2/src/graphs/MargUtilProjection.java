package graphs;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import consumers.Consumer;
import consumers.Consumers;
import consumers.Pareto;
import firms.Firm;
import firms.Utils;
import repast.simphony.context.Context;
import repast.simphony.context.space.grid.ContextGrid;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.SingleOccupancyCellAccessor;
import repast.simphony.space.grid.StickyBorders;

public class MargUtilProjection {

	private static final int MAX_X = 100, MAX_Y = 100;
	private static final int MIN_X = 0;
	private static final int FIRMS_HEIGHT = 16;
	private static final int SEGMENT_LIMITS_HEIGHT = 10;

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

	public void update(Consumer c) {
		int x = margUtilToCoord(c.getMargUtilOfQuality());
		space.moveTo(c, x, getFreeY(x, FIRMS_HEIGHT + SEGMENT_LIMITS_HEIGHT, 1));

	}

	public void update(Firm f) {

		int x = margUtilToCoord(Utils.getPoorestConsumerMargUtil(
				f.getQuality(), f.getPrice()));

		space.moveTo(f, x, getFreeY(x, 0, 2));

	}

	public void add(SegmentLimit sL) {
		double val = Math.min(sL.getValue(),
				Consumers.getMaxMargUtilOfQuality());
		int x = margUtilToCoord(val);

		space.moveTo(sL, x, getFreeY(x, FIRMS_HEIGHT, 2));

	}

	private int margUtilToCoord(double margUtilOfQuality) {

		return (int) Math.min(
				Math.round(margUtilOfQuality / getMaxUtilToDraw()
						* (MAX_X - MIN_X))
						+ MIN_X, MAX_X);
	}

	private double getMaxUtilToDraw() {
		// Assign minimum Marginal Utility of Quality for the segment
		double acumProb = (double) GetParameter("margUtilPercentToDraw");

		double gini = (double) GetParameter("gini");
		double lambda = (1.0 + gini) / (2.0 * gini);

		double minimum = Consumers.getMinMargUtilOfQuality();

		return Pareto.inversePareto(acumProb, minimum, lambda);
	}

	private int getFreeY(int x, int firstY, int step) {
		int[] dims = new int[2];

		dims[0] = x;
		dims[1] = firstY;

		while (space.getObjectAt(dims) != null) {
			if (dims[1] < MAX_Y)
				dims[1] += step;
			else
				return MAX_Y;
		}

		return dims[1];
	}

}

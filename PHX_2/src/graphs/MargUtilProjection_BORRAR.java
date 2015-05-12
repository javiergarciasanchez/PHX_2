package graphs;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import pHX_2.Firm;
import pHX_2.Firms;
import pHX_2.Market;
import demand.Consumer;
import demand.Consumers;
import demand.Pareto;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContextSpace;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.continuous.StickyBorders;

public class MargUtilProjection_BORRAR {

	private static final double MAX_X = 100, MAX_Y = 100;
	private static final double MIN_X = 0, MIN_Y = 0;
	private static final double FIRMS_HEIGHT = 3;

	private ContinuousSpace<Object> space;

	public MargUtilProjection_BORRAR(Context<Object> context) {

		double[] dims = new double[2];
		dims[0] = MAX_X + 0.1;
		dims[1] = MAX_Y + FIRMS_HEIGHT + 0.1;

		space = new ContextSpace<Object>("MargUtilProjection",
				new SimpleCartesianAdder<Object>(), new StickyBorders(), dims);

		context.addProjection(space);

	}

	public void add(Consumer c) {
		double mUtil = c.getMargUtilOfQuality();
		space.moveTo(c, margUtilToCoord(mUtil), getY(mUtil));

	}

	private double getY(double mUtil) {
		double stepSize = (double) Consumers.getMaxConsumers()
				/ (MAX_Y - (MIN_Y + FIRMS_HEIGHT));
		double[] dims = new double[2];

		dims[0] = margUtilToCoord(mUtil);
		dims[1] = MIN_Y + FIRMS_HEIGHT;

		while (space.getObjectsAt(dims).iterator().hasNext()) {
			dims[1] += stepSize;
		}

		return dims[1];
	}

	public void update(Firm f) {
		space.moveTo(f, margUtilToCoord(Firms.getPoorestConsumerMargUtil(
				f.getQuality(), f.getPrice())), FIRMS_HEIGHT / 2.0 + MIN_Y);
	}

	private double getMaxUtilToDraw() {
		// Assign minimum Marginal Utility of Quality for the segment
		double acumProb = (double) GetParameter("margUtilPercentToDraw");

		double gini = (double) GetParameter("gini");
		double lambda = (1.0 + gini) / (2.0 * gini);

		double minimum = Market.consumers.getMinMargUtilOfQuality();

		return Pareto.inversePareto(acumProb, minimum, lambda);
	}

	private double margUtilToCoord(double margUtilOfQuality) {
		return Math.min((double) margUtilOfQuality / getMaxUtilToDraw()
				* (MAX_X - MIN_X) + MIN_X, MAX_X);
	}

}

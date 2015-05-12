package graphs;

import pHX_2.Market;
import repast.simphony.context.space.continuous.ContextSpace;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.continuous.StickyBorders;
import demand.Consumer;
import demand.Consumers;

public class ConsumersProjection {

	private static final double MAX_X = 100, MAX_Y = 10;
	private static final double MIN_X = 0;

	private ContinuousSpace<Consumer> space;

	public ConsumersProjection(Consumers consumers) {
		double[] dims = new double[2];
		dims[0] = MAX_X + 0.1;
		dims[1] = MAX_Y + 0.1;

		space = new ContextSpace<Consumer>("ConsumersProjection",
				new SimpleCartesianAdder<Consumer>(), new StickyBorders(), dims);

		consumers.addProjection(space);
	}

	public void add(Consumer c) {
		space.moveTo(c, margUtilToCoord(c.getMargUtilOfQuality()), MAX_Y / 2.0);
	}

	private double margUtilToCoord(double margUtilOfQuality) {
		double minMUQ = Market.consumers.getMinMargUtilOfQuality();
		double maxMUQ = Market.consumers.getMaxMargUtilOfQuality();

		return (margUtilOfQuality - minMUQ) / (maxMUQ - minMUQ)
				* (MAX_X - MIN_X) + MIN_X;
	}

}

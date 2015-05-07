package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.util.ArrayList;

import offer.Offer;
import demand.Consumer;
import demand.Consumers;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.continuous.StickyBorders;

public class Market extends DefaultContext<Object> implements
		ContextBuilder<Object> {


	// Defining market components: consumers and firms
	public static Consumers consumers;
	public static ContinuousSpace<Consumer> consumersProyection;

	public static Firms firms;
	public static ContinuousSpace<Firm> firmsProyection;

	public static ArrayList<Firm> toBeKilled;

	@Override
	public Context<Object> build(Context<Object> context) {

		// Reset seed
		RandomHelper.setSeed((Integer) GetParameter("randomSeed"));
		
		// Reset static variables
		Consumer.resetStaticVars();
		Firm.resetStaticVars();
		Offer.resetStaticVars();

		// Initialize ToBeKilled
		toBeKilled = new ArrayList<Firm>();

		context.setId("Market");

		// Create Consumers
		consumers = new Consumers();
		context.addSubContext(consumers);		
		consumers.addConsumers();

		// Create Projections
		// Consumers Space represents Marginal Utility of Quality
		consumersProyection = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null).createContinuousSpace(
						"Consumers_Proyection", consumers,
						new SimpleCartesianAdder<Consumer>(),
						new StickyBorders(), Consumers.getMaxX() + 0.1,
						Consumers.getMaxY() + 0.1);

		// Create firms
		firms = new Firms();
		context.addSubContext(firms);

		// Firms Space represents price and quality
		firmsProyection = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null).createContinuousSpace(
						"Firms_Proyection", firms,
						new SimpleCartesianAdder<Firm>(), new StickyBorders(),
						Offer.getMaxX() + 0.1, Offer.getMaxY() + 0.1);

		return context;

	}

}

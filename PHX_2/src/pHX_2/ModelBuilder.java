package pHX_2;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.continuous.StickyBorders;
import static repast.simphony.essentials.RepastEssentials.*;

public class ModelBuilder extends DefaultContext<Object> implements
		ContextBuilder<Object> {

	@Override
	public Context<Object> build(Context<Object> context) {


		if (RunEnvironment.getInstance().isBatch()) {
			
			// Collect data
			new SQLDataCollector(context);
			
		} else {
			
			// Create Projections
			ContinuousSpaceFactory factory = ContinuousSpaceFactoryFinder
					.createContinuousSpaceFactory(null);

			// Consumers Space represents Marginal Utility of Quality
			factory.createContinuousSpace("Consumers", context,
					new SimpleCartesianAdder<Object>(), new StickyBorders(),
					Consumer.MaxX + 0.1, Consumer.MaxY + 0.1);

			// Firms Space represents price and quality
			factory.createContinuousSpace("Firms", context,
					new SimpleCartesianAdder<Object>(), new StickyBorders(),
					Product.MaxX + 0.1, Product.MaxY + 0.1);
			
		}

		RunEnvironment.getInstance().endAt((Double) GetParameter("stopAt"));

		new Market(context);

		return context;
	}
}
package pHX_2;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.continuous.StickyBorders;
import static repast.simphony.essentials.RepastEssentials.*;

public class CreateMarket extends DefaultContext<Object> implements
		ContextBuilder<Object> {

	public static Context<Object> market;

	// Defining market components: consumers and firms
	public static Consumers consumersContext;
	public static ContinuousSpace<Consumer> consumersProyection;

	public static Firms firmsContext;
	public static ContinuousSpace<Firm> firmsProyection;

	public static ArrayList<Firm> toBeKilled;

	@Override
	public Context<Object> build(Context<Object> context) {

		// Initialize ToBeKilled
		toBeKilled = new ArrayList<Firm>();
		
		// Reset static counters
		Firm.setFirmIDCounter(1);
		Consumer.setConsumerIDCounter(1);
		
		// Check parameters
		String msg = checkParameters();
		if (msg != null) {
			JOptionPane.showMessageDialog(null, msg, "Parameters Error",
					JOptionPane.ERROR_MESSAGE);
			RunEnvironment.getInstance().endRun();
		}

		context.setId("Market");
		market = context;
		
		if (RunEnvironment.getInstance().isBatch()) {

			// Collect data
			new SQLDataCollector(context);

		}

		// Create Consumers
		consumersContext = new Consumers();
		market.addSubContext(consumersContext);

		// Create Projections
		// Consumers Space represents Marginal Utility of Quality
		consumersProyection = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null).createContinuousSpace(
						"Consumers_Proyection", consumersContext,
						new SimpleCartesianAdder<Consumer>(),
						new StickyBorders(), Consumers.getMaxX() + 0.1,
						Consumers.getMaxY() + 0.1);

		// Create firms
		firmsContext = new Firms();
		market.addSubContext(firmsContext);

		// Firms Space represents price and quality
		firmsProyection = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null).createContinuousSpace(
						"Firms_Proyection", firmsContext,
						new SimpleCartesianAdder<Firm>(), new StickyBorders(),
						Offer.getMaxX() + 0.1, Offer.getMaxY() + 0.1);

		addConsumers();
		return market;

	}

	private String checkParameters() {
		return BetaSubj.checkParameters();
	}

	// Default interval means only once
	private void addConsumers() {

		for (int i = 1; i <= (Integer) GetParameter("numberOfConsumers"); i++) {
			new Consumer();
		}

	}

}

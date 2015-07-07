package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import firms.Firm;
import firms.Firms;
import graphs.ConsumersProjection;
import graphs.ConsumptionProjection;
import graphs.Firms2DProjection;
import graphs.FirmsDemandProjection;
import graphs.FirmsProfitProjection;
import graphs.MargUtilProjection;
import graphs.SegmentLimit;
import firmHistory.Offer;
import firmTypes.FirmType;

import java.util.ArrayList;

import consumers.Consumer;
import consumers.Consumers;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;

public class Market extends DefaultContext<Object> implements
		ContextBuilder<Object> {

	// Defining market components: consumers and firms
	public static Consumers consumers;
	public static ConsumersProjection consumersProjection;

	public static Firms firms;
	public static Firms2DProjection firms2DProjection;
	public static FirmsDemandProjection firmsDemandProjection;
	public static FirmsProfitProjection firmsProfitProjection;

	public static ConsumptionProjection consumptionProjection;
	public static MargUtilProjection margUtilProjection;

	public static DefaultContext<SegmentLimit> segments;
	public static ArrayList<Firm> toBeKilled;

	@Override
	public Context<Object> build(Context<Object> context) {

		// Reset seed
		RandomHelper.setSeed((Integer) GetParameter("randomSeed"));

		// Set end of run
		RunEnvironment.getInstance().endAt((Double) GetParameter("stopAt"));

		// Reset static variables
		Consumer.resetStaticVars();
		Consumers.resetStaticVars();
		Firm.resetStaticVars();
		Offer.resetStaticVars();
		FirmType.resetStaticVars();

		// Initialize ToBeKilled
		toBeKilled = new ArrayList<Firm>();

		context.setId("Market");

		// Create RecessionsHandler Handler
		new RecessionsHandler(context);

		// Create Consumers
		consumers = new Consumers();
		context.addSubContext(consumers);
		Consumers.createConsumers();

		// Consumers Projection
		// Dimension is Marginal Utility of Quality
		consumersProjection = new ConsumersProjection(consumers);
		consumptionProjection = new ConsumptionProjection(consumers);
		// Create Marginal utility projection
		margUtilProjection = new MargUtilProjection(context);

		// AddConsumers to projections
		consumers.addConsumersToProjections();

		// Create firms
		firms = new Firms();
		context.addSubContext(firms);
		context.add(firms); // We add firms as an agent to get Market Variation

		// Firms Projections
		// Dimensions are price, quality and consumers
		firms2DProjection = new Firms2DProjection(firms);
		firmsDemandProjection = new FirmsDemandProjection(firms);
		firmsProfitProjection = new FirmsProfitProjection(firms);

		// Create Market Segments defined by firms prices and qualities offered
		segments = new DefaultContext<SegmentLimit>("Segments_Context");
		context.addSubContext(segments);

		return context;

	}

}

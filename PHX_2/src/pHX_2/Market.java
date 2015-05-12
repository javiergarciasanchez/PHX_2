package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import graphs.ConsumersProjection;
import graphs.Firms2DProjection;
import graphs.FirmsDemandProjection;
import graphs.MargUtilProjection;

import java.util.ArrayList;

import offer.Offer;
import demand.Consumer;
import demand.Consumers;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.random.RandomHelper;

public class Market extends DefaultContext<Object> implements
		ContextBuilder<Object> {

	// Defining market components: consumers and firms
	public static Consumers consumers;
	public static ConsumersProjection consumersProjection;

	public static Firms firms;
	public static Firms2DProjection firms2DProjection;
	public static FirmsDemandProjection firmsDemandProjection;
	
	public static MargUtilProjection margUtilProjection; 

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

		// Create Marginal utility projection
		margUtilProjection = new MargUtilProjection(context);
		
		// Create Consumers
		consumers = new Consumers();
		context.addSubContext(consumers);
		consumers.createConsumers();

		// Consumers Projection
		// Dimension is Marginal Utility of Quality
		consumersProjection = new ConsumersProjection(consumers);

		// AddConsumers to projections
		consumers.addConsumersToProjections();

		// Create firms
		firms = new Firms();
		context.addSubContext(firms);

		// Firms Projections
		// Dimensions are price, quality and demand
		firms2DProjection = new Firms2DProjection(firms);
		firmsDemandProjection = new FirmsDemandProjection(firms);


		
		return context;

	}

}

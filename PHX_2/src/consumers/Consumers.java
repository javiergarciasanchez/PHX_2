package consumers;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import repast.simphony.context.DefaultContext;

public class Consumers extends DefaultContext<Consumer> {

	// Distribution of consumer's utility parameters
	// This variables are static to access them easily
	// Are initialized in constructor without problem because
	// only one instance is always created
	private static double minMargUtilOfQuality;
	private static double maxMargUtilOfQuality;

	private static Pareto margUtilOfQualityDistrib;

	public static void resetStaticVars() {
		minMargUtilOfQuality = 0.0;
		maxMargUtilOfQuality = 0.0;
		margUtilOfQualityDistrib = null;		
	}

	public Consumers() {
		super("Consumers_Context");
		
		minMargUtilOfQuality = (double) GetParameter("minMargUtilOfQuality");
		
		// Max marginal utility is updated every time a consumer is created
		maxMargUtilOfQuality = 0.;

		createProbabilityDistrib();
		
	}

	private static void createProbabilityDistrib() {
	
		// Marginal Utility of Quality 
		double gini = (double) GetParameter("gini");
		double lambda = (1.0 + gini) / (2.0 * gini);
		margUtilOfQualityDistrib = Pareto.getPareto(lambda,
				getMinMargUtilOfQuality());		
	
	}

	public static Pareto getMargUtilOfQualityDistrib() {
		return margUtilOfQualityDistrib;
	}

	public static double getMinMargUtilOfQuality() {
		return minMargUtilOfQuality;
	}

	public static double getMaxMargUtilOfQuality() {
		return maxMargUtilOfQuality;
	}

	public static int getNumberOfConsumers() {
		return (Integer) GetParameter("numberOfConsumers");
	}

	public static void setMaxMargUtilOfQuality(double maxMargUtilOfQuality) {
		Consumers.maxMargUtilOfQuality = maxMargUtilOfQuality;
	}

	public static void createConsumers() {

		for (int i = 1; i <= (Integer) GetParameter("numberOfConsumers"); i++) {
			new Consumer();
		}

	}
	
	public void addConsumersToProjections() {

		for (Consumer c : getObjects(Consumer.class)) {
			c.updateProjections();
		}

	}

}
package consumers;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import repast.simphony.context.DefaultContext;

public class Consumers extends DefaultContext<Consumer> {

	// Distribution of consumer's utility parameters
	// This variables are static to access them easily
	// Are initialized in constructor without problem because
	// only one instance is always created
	private double minMargUtilOfQuality;
	private double maxMargUtilOfQuality;

	private Pareto margUtilOfQualityDistrib;

	public Consumers() {
		super("Consumers_Context");
		
		minMargUtilOfQuality = (double) GetParameter("minMargUtilOfQuality");
		
		// Max marginal utility is updated every time a consumer is created
		maxMargUtilOfQuality = 0.;

		createProbabilityDistrib();
		
	}

	private void createProbabilityDistrib() {
	
		// Marginal Utility of Quality 
		double gini = (double) GetParameter("gini");
		double lambda = (1.0 + gini) / (2.0 * gini);
		margUtilOfQualityDistrib = Pareto.getPareto(lambda,
				getMinMargUtilOfQuality());		
	
	}

	public Pareto getMargUtilOfQualityDistrib() {
		return margUtilOfQualityDistrib;
	}

	public double getMinMargUtilOfQuality() {
		return minMargUtilOfQuality;
	}

	public double getMaxMargUtilOfQuality() {
		return maxMargUtilOfQuality;
	}

	public static int getMaxConsumers() {
		return (Integer) GetParameter("numberOfConsumers");
	}

	public void setMaxMargUtilOfQuality(double maxMargUtilOfQuality) {
		this.maxMargUtilOfQuality = maxMargUtilOfQuality;
	}

	public void createConsumers() {

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
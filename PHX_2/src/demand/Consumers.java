package demand;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import cern.jet.random.Normal;
import repast.simphony.context.DefaultContext;
import repast.simphony.random.RandomHelper;

public class Consumers extends DefaultContext<Consumer> {

	private static final int MAX_X = 100, MAX_Y = 10, MIN_X = 0, MIN_Y = 0;

	// Distribution of consumer's utility parameters
	// This variables are static to access them easily
	// Are initialized in constructor without problem because
	// only one instance is always created
	private double minMargUtilOfQuality;
	private double maxMargUtilOfQuality;

	private Pareto margUtilOfQualityDistrib;
	private Normal explorationPrefDistrib;

	public Consumers() {
		super("Consumers_Context");
		
		minMargUtilOfQuality = (double) GetParameter("minMargUtilOfQuality");
		maxMargUtilOfQuality = (double) GetParameter("maxMargUtilOfQuality");

		createProbabilityDistrib();
		
	}

	private void createProbabilityDistrib() {
	
		// Exploration preference
		// Determines the probability of exploring when choosing a firm.
		// If consumer decides to explore it chooses randomly a firm from the
		// known ones but one never tried
		// Otherwise he chooses among the tried firms the one that maximizes
		// utility
		double mean = (double) GetParameter("explorationPrefMean");
		explorationPrefDistrib = RandomHelper.createNormal(mean,
				(double) GetParameter("explorationPrefStdDevPerc") * mean);
	
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

	public Normal getExplorationPrefDistrib() {
		return explorationPrefDistrib;
	}

	public static double getMaxX() {
		return MAX_X;
	}

	public static double getMinX() {
		return MIN_X;
	}

	public static double getMaxY() {
		return MAX_Y;
	}

	public static double getMinY() {
		return MIN_Y;
	}

	public void addConsumers() {

		for (int i = 1; i <= (Integer) GetParameter("numberOfConsumers"); i++) {
			new Consumer();
		}

	}

}
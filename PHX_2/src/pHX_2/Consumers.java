package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import cern.jet.random.Binomial;
import cern.jet.random.Normal;
import cern.jet.random.Uniform;
import repast.simphony.context.DefaultContext;
import repast.simphony.random.RandomHelper;

public class Consumers extends DefaultContext<Consumer> {

	private static final int MAX_X = 100, MAX_Y = 10, MIN_X = 0, MIN_Y = 0;

	// Distribution of consumer's utility parameters
	// This variables are static to access them easily
	// Are initialized in constructor without problem because
	// only one instance is always created
	private static double minMargUtilOfQuality = 0.0;
	private static double maxMargUtilOfQuality = 0.0;

	private static Uniform margUtilOfQualityDistrib = null;
	private static Normal explorationPrefDistrib = null;
	public static Binomial explorationDistrib = RandomHelper.createBinomial(1,
			0.5);

	public Consumers() {
		super("Consumers_Context");
	}

	public static Uniform getMargUtilOfQualityDistrib() {
		if (margUtilOfQualityDistrib == null)
			margUtilOfQualityDistrib = RandomHelper.createUniform(
					getMinMargUtilOfQuality(), getMaxMargUtilOfQuality());

		return margUtilOfQualityDistrib;
	}

	public static double getMinMargUtilOfQuality() {
		if (minMargUtilOfQuality == 0.0)
			minMargUtilOfQuality = (double) GetParameter("minMargUtilOfQuality");

		return minMargUtilOfQuality;
	}

	public static double getMaxMargUtilOfQuality() {
		if (maxMargUtilOfQuality == 0.0)
			maxMargUtilOfQuality = (double) GetParameter("maxMargUtilOfQuality");

		return maxMargUtilOfQuality;
	}

	public static Normal getExplorationPrefDistrib() {
		// Exploration preference determines the probability of exploring when
		// choosing a firm.
		// If consumer decides to explore it chooses randomly a firm from the
		// known ones but one never tried
		// Otherwise he chooses among the tried firms the one that maximizes
		// utility

		if (explorationPrefDistrib == null) {
			double mean = (double) GetParameter("explorationPrefMean");

			explorationPrefDistrib = RandomHelper.createNormal(mean,
					(double) GetParameter("explorationPrefStdDevPerc") * mean);

		}
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

}
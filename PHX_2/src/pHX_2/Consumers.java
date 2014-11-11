package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
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
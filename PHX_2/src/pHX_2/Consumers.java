package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import cern.jet.random.Uniform;
import repast.simphony.context.DefaultContext;
import repast.simphony.random.RandomHelper;

public class Consumers extends DefaultContext<Consumer> {

	// Distribution of consumer's utility parameters
	// This variables are static to access them easily
	// Are initialized in constructor without problem because
	// only one instance is always created
	private static double minMargUtilOfQuality;
	private static double maxMargUtilOfQuality;

	private static Uniform margUtilOfQualityDistrib = null;

	public Consumers() {
		super("Consumers_Context");
	}

	public static Uniform getMargUtilOfQualityDistrib() {
		if (margUtilOfQualityDistrib == null)
			margUtilOfQualityDistrib = RandomHelper.createUniform(
					minMargUtilOfQuality, maxMargUtilOfQuality);
	
		return margUtilOfQualityDistrib;
	}

	public static double getMinMargUtilOfQuality() {
		if (minMargUtilOfQuality == 0)
			minMargUtilOfQuality = (double) GetParameter("minMargUtilOfQuality");

		return minMargUtilOfQuality;
	}

	public static double getMaxMargUtilOfQuality() {
		if (maxMargUtilOfQuality == 0)
			maxMargUtilOfQuality = (double) GetParameter("maxMargUtilOfQuality");

		return maxMargUtilOfQuality;
	}

	public static double getMaxX() {
		return getMaxMargUtilOfQuality() - getMinMargUtilOfQuality();
	}

	public static double getMaxY() {
		return getMaxX() / 10.0;
	}

}
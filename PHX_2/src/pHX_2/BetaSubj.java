package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import repast.simphony.random.RandomHelper;
import cern.jet.random.Beta;

public class BetaSubj {
	// It is defined using mode (mostLikely) and mean. Max and min are 1.0 and 0.0

	private Beta implicitBeta;

	public BetaSubj(double mostLikely, double mean) {

		double alfa = mean * ( 2.0 * mostLikely - 1.0)
				/ (mostLikely - mean);
		
		double beta = alfa * (1 - mean) / mean;

		implicitBeta = RandomHelper.createBeta(alfa, beta);

		return;

	}

	public static String checkParameters(double min, double max,
			double mostLikely, double mean) {

		// Check Parameters
		double mid = (min + max) / 2.0;
		String msg = new String();

		if (min >= max)
			msg = "Minimum should be lower than Maximum";
		else if ((mostLikely >= max) || (mostLikely <= min))
			msg = "Most Likely should be between min and max";
		else if ((mean >= max) || (mean <= min))
			msg = "Mean should be between min and max";
		else if ((mostLikely > mean) && (mean <= mid))
			msg = "If most likey is higher than mean, mean should be higher than middle point";
		else if ((mostLikely < mean) && (mean >= mid))
			msg = "If mostlikey is lower than mean, mean should be lower than middle point";
		else if ((mostLikely == mean) && (mean != mid))
			msg = "If mostlikey is equal to mean, mean should be equal to middle point";
		else
			msg = null;

		return msg;
	}

	public double nextDouble() {
		return implicitBeta.nextDouble();
	}

	public static String checkParameters() {
		String qualityErr, priceErr;

		qualityErr = checkParameters(0.0, 1.0,
				(Double) GetParameter("lowQualityMostLikely"),
				(Double) GetParameter("lowQualityMean"));

		if (qualityErr != null)
			return "Error in the parameters of quality distribution : "
					+ qualityErr;

		priceErr = checkParameters(0.0, 1.0,
				(Double) GetParameter("lowInitialPriceMostLikely"),
				(Double) GetParameter("lowInitialPriceMean"));

		if (priceErr != null)
			return "Error in the parameters of price distribution : "
					+ priceErr;
		else
			return null;

	}

}

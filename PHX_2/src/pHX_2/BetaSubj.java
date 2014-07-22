package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import repast.simphony.random.RandomHelper;
import cern.jet.random.Beta;

public class BetaSubj {

	private Beta implicitBeta;
	private double max, min;

	public BetaSubj(double min, double max, double mostLikely, double mean) {
		
		this.min = min;
		this.max = max;

		double alfa = 2.0 * (mean - min) * ((min + max) / 2.0 - mostLikely)
				/ ((mean - mostLikely) * (max - min));
		double beta = alfa * (max - min) / (mean - min);

		implicitBeta = RandomHelper.createBeta(alfa, beta);

		return;

	}

	public static String checkParameters(double min, double max,
			double mostLikely, double mean) {

		// Check Parameters
		double mid = (min + max) / 2.0;
		String msg = new String(); 

		if (min >= max)
			msg = "MinQuality should be lower than MaxQuality";
		else if ((mostLikely >= max) || (mostLikely <= min))
			msg = "Most Likely Quality should be between min and max quality";
		else if ((mean >= max) || (mean <= min))
			msg = "Mean Quality should be between min and max quality";
		else if ((mostLikely > mean) && (mean <= mid))
			msg = "If mostlikey is higher than mean, mean should be higher than middle point";
		else if ((mostLikely < mean) && (mean >= mid))
			msg = "If mostlikey is lower than mean, mean should be lower than middle point";
		else if ((mostLikely == mean) && (mean != mid))
			msg = "If mostlikey is equal to mean, mean should be equal to middle point";
		else
			msg = null;
		
		if (msg != null) msg = "Error in the parameters of quality distribution : " + msg;
		
		return msg;
	}

	public double nextDouble() {
		return implicitBeta.nextDouble() * (max - min) + min;
	}

	public static String checkParameters() {
		
		return checkParameters((double) GetParameter("minQuality"),
				(double) GetParameter("maxQuality"),
				(Double) GetParameter("lowQualityMostLikely"),
				(Double) GetParameter("lowQualityMean"));

	}

}

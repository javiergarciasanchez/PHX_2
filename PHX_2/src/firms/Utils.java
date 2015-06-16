package firms;

import firmState.Offer;
import pHX_2.Market;
import repast.simphony.random.RandomHelper;

public class Utils {

	public static double getMaxCompetitivePriceToEntry(double quality) {

		Firm loF = Market.firms.getLowerLimitFirm(quality, false);
		Firm hiF = Market.firms.getHigherLimitFirm(quality, false);

		return getMaxCompetitivePriceToEntry(quality, loF, hiF);

	}

	private static double getMaxCompetitivePriceToEntry(double quality,
			Firm lowerComp, Firm higherComp) {

		if (higherComp == null) {
			return Offer.getMaxPrice();
			
		} else if (lowerComp == null) {
			double pH = higherComp.getPrice();
			double qH = higherComp.getQuality();
			
			return pH / qH * quality;
			
		} else {
			double pH = higherComp.getPrice();
			double qH = higherComp.getQuality();
			double pL = lowerComp.getPrice();
			double qL = lowerComp.getQuality();

			return (pH * (quality - qL) + pL * (qH - quality)) / (qH - qL);
			
		}

	}

	public static double getPoorestConsumerMargUtil(double quality, double price) {
		return price / quality;
	}

	public static double getPoorestConsumerMinPrice(double minMargUtil,
			double quality) {
		return minMargUtil * quality;
	}

	public static double getRandomInitialQuality(double lowerQ, double higherQ) {
		// Uses default uniform distribution between lower and high quality

		double tmpQ;

		// Quality should be checked for existence
		// because two different firms cannot have the same quality
		do {

			tmpQ = RandomHelper.nextDoubleFromTo(lowerQ, higherQ);

		} while (Market.firms.containsQ(tmpQ));

		return tmpQ;
	}

}

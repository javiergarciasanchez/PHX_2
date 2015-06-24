package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import consumers.Consumers;
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

	public static double calcLimit(Offer loOffer, Offer hiOffer) {

		if (loOffer == null && hiOffer != null) {
			double poorest = getPoorestConsumerMargUtil(hiOffer.getQuality(),
					hiOffer.getPrice());
			// The limit should be higher than the absolute minimum
			double minMargUtil = Consumers.getMinMargUtilOfQuality();
			return Math.max(poorest, minMargUtil);
		} else if (loOffer != null && hiOffer == null)
			return Consumers.getMaxMargUtilOfQuality();

		else if (loOffer != null && hiOffer != null) {
			double limit = (hiOffer.getPrice() - loOffer.getPrice())
					/ (hiOffer.getQuality() - loOffer.getQuality());

			// The limit should be higher than the poorest of the hiOffer
			double poorest = getPoorestConsumerMargUtil(hiOffer.getQuality(),
					hiOffer.getPrice());
			limit = Math.max(poorest, limit);

			// The limit should be higher than the absolute minimum
			double minMargUtil = Consumers.getMinMargUtilOfQuality();
			limit = Math.max(limit, minMargUtil);

			return limit;

		} else
			// Both are null. It shouldn't come here
			return 0.0;

	}

	public static double calcLimit(Firm loF, Firm hiF) {
		// it is assumed that loF and hiF are not null
		double limit = (hiF.getPrice() - loF.getPrice())
				/ (hiF.getQuality() - loF.getQuality());

		return Math.max(hiF.getPoorestConsumerMargUtil(), limit);
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

	public static double getMarginalProfitOfQuality(Firm firm) {
		// It is assumed firm is in the market

		double p = firm.getPrice();
		double q = firm.getQuality();
		double demand = firm.getDemand();
		double cost = firm.unitCost(q);
		double margCost = firm.getMarginalCostOfQuality();
		double derivQ = demandDerivRespToQuality(firm);

		return (p - cost) * derivQ - margCost * demand;
	}

	private static double demandDerivRespToQuality(Firm firm) {
		// It is assumed firm is in the market

		double gini = (double) GetParameter("gini");
		double lambda = (1.0 + gini) / (2.0 * gini);
		double minMargUtil = Consumers.getMinMargUtilOfQuality();
		double mktSize = Consumers.getNumberOfConsumers();

		double q = firm.getQuality();
		double p = firm.getPrice();
		double poorest = firm.getPoorestConsumerMargUtil();

		Firm loF = Market.firms.getLowerLimitFirm(q, false);
		Firm hiF = Market.firms.getHigherLimitFirm(q, false);

		if (loF == null && hiF == null && minMargUtil >= poorest) {
			// It has all the market
			return 0.0;

		} else if (loF == null && hiF == null && poorest > minMargUtil) {
			return mktSize * lambda * Math.pow(minMargUtil, lambda)
					* Math.pow(q, lambda - 1.0) / Math.pow(p, lambda);

		} else if (loF == null && hiF != null && minMargUtil >= poorest) {
			double pH = hiF.getPrice();
			double qH = hiF.getQuality();

			if (pH == p)
				throw new Error(
						"Prices cannot be the same if both firms are in the market");

			return mktSize * lambda * Math.pow(minMargUtil, lambda)
					* Math.pow(qH - q, lambda - 1.0) / Math.pow(pH - p, lambda);

		} else if (loF == null && hiF != null && poorest > minMargUtil) {
			double pH = hiF.getPrice();
			double qH = hiF.getQuality();

			if (pH == p)
				throw new Error(
						"Prices cannot be the same if both firms are in the market");

			return mktSize
					* lambda
					* Math.pow(minMargUtil, lambda)
					* (Math.pow(q, lambda - 1.0) / Math.pow(p, lambda) + Math
							.pow(qH - q, lambda - 1.0)
							/ Math.pow(pH - p, lambda));

		} else if (loF != null && hiF == null) {
			double pL = loF.getPrice();
			double qL = loF.getQuality();

			return mktSize * lambda * Math.pow(minMargUtil, lambda)
					* Math.pow(q - qL, lambda - 1.0) / Math.pow(p - pL, lambda);

		} else {
			// loF != null && hiF != null
			double pH = hiF.getPrice();
			double qH = hiF.getQuality();
			double pL = loF.getPrice();
			double qL = loF.getQuality();

			if (pH == p || pL == p)
				throw new Error(
						"Prices cannot be the same if both firms are in the market");

			return mktSize
					* lambda
					* Math.pow(minMargUtil, lambda)
					* (Math.pow(q - qL, lambda - 1.0)
							/ Math.pow(p - pL, lambda) + Math.pow(qH - q,
							lambda - 1.0) / Math.pow(pH - p, lambda));

		}
	}

	public static double getMarginalProfitOfPrice(Firm firm) {
		// It is assumed firm is in the market

		double p = firm.getPrice();
		double q = firm.getQuality();
		double demand = firm.getDemand();
		double cost = firm.unitCost(q);
		double derivP = demandDerivRespToPrice(firm);

		return (p - cost) * derivP + demand;
	}

	private static double demandDerivRespToPrice(Firm firm) {
		// It is assumed firm is in the market

		double gini = (double) GetParameter("gini");
		double lambda = (1.0 + gini) / (2.0 * gini);
		double minMargUtil = Consumers.getMinMargUtilOfQuality();
		double mktSize = Consumers.getNumberOfConsumers();

		double q = firm.getQuality();
		double p = firm.getPrice();
		double poorest = firm.getPoorestConsumerMargUtil();

		Firm loF = Market.firms.getLowerLimitFirm(q, false);
		Firm hiF = Market.firms.getHigherLimitFirm(q, false);

		if (loF == null && hiF == null && minMargUtil > poorest) {
			// It has it all the market
			return 0.0;

		} else if (loF == null && hiF == null && poorest > minMargUtil) {
			return -mktSize * lambda * Math.pow(minMargUtil, lambda)
					* Math.pow(q, lambda) / Math.pow(p, lambda + 1.0);

		} else if (loF == null && hiF != null && minMargUtil > poorest) {
			double pH = hiF.getPrice();
			double qH = hiF.getQuality();

			return -mktSize * lambda * Math.pow(minMargUtil, lambda)
					* Math.pow(qH - q, lambda) / Math.pow(pH - p, lambda + 1.0);

		} else if (loF == null && hiF != null && poorest > minMargUtil) {
			double pH = hiF.getPrice();
			double qH = hiF.getQuality();

			return -mktSize
					* lambda
					* Math.pow(minMargUtil, lambda)
					* (Math.pow(q, lambda) / Math.pow(p, lambda + 1.0) + Math
							.pow(qH - q, lambda)
							/ Math.pow(pH - p, lambda + 1.0));

		} else if (loF != null && hiF == null) {
			double pL = loF.getPrice();
			double qL = loF.getQuality();

			return -mktSize * lambda * Math.pow(minMargUtil, lambda)
					* Math.pow(q - qL, lambda) / Math.pow(p - pL, lambda + 1.0);

		} else if (loF != null && hiF != null) {
			double pH = hiF.getPrice();
			double qH = hiF.getQuality();
			double pL = loF.getPrice();
			double qL = loF.getQuality();

			return -mktSize
					* lambda
					* Math.pow(minMargUtil, lambda)
					* (Math.pow(q - qL, lambda)
							/ Math.pow(p - pL, lambda + 1.0) + Math.pow(qH - q,
							lambda) / Math.pow(pH - p, lambda + 1.0));

		} else
			// It shouldn't come here
			return 0.0;
	}

}

package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import consumers.Consumers;
import firmHistory.FirmState;
import firmHistory.Offer;
import firmTypes.NoPrice;
import pHX_2.Market;
import repast.simphony.random.RandomHelper;

public class Utils {

	public static Offer getMaximizingOffer(Firm f) {
		// It is assumed it is in the market

		double qStep = f.getHistory().getCurrentQualityStep();
		double pStep = f.getHistory().getCurrentPriceStep();

		double nextQStep = (qStep == 0 ? Offer.getDefaultQualityStep() : Math
				.abs(qStep));
		double nextPStep = (pStep == 0 ? Offer.getDefaultPriceStep() : Math
				.abs(pStep));

		double margProfQ = Utils.getMarginalProfitOfQuality(f) * nextQStep;
		double margProfP = Utils.getMarginalProfitOfPrice(f) * nextPStep;

		Offer o = new Offer(f.getCurrentOffer());

		if (Math.abs(margProfP) > Math.abs(margProfQ)) {
			// Modify price
			if (margProfP > 0)
				o.modifyPrice(pStep, +1);
			else
				o.modifyPrice(pStep, -1);
		} else {
			// Modify quality
			if (margProfQ > 0)
				o.modifyQuality(qStep, +1);
			else
				o.modifyQuality(qStep, -1);
		}

		return o;

	}

	public static Offer getReenteringOffer(Firm f) {
		// It is assumed it was removed from FirmsByQ

		// Delete history to start again but keep current offer
		Offer o = f.getCurrentOffer();
		f.getHistory().clear();

		try {
			double q = o.getQuality();
			double unitCost = f.unitCost(q);
			o.setPrice(Utils.getRationalPrice(unitCost, q));
		} catch (NoPrice e) {
			// There is no available price to compete
			// Keep current Offer
		}

		return o;

	}

	public static double getRationalPrice(double unitCost, double q)
			throws NoPrice {

		// Chooses a price that is one step below maximum competitive price
		double price = Utils.getMaxCompetitivePriceToEntry(q);

		if (price > unitCost) {
			double priceStep = Market.firms.getPriceStepDistrib().nextDouble();
			price = price - priceStep;

			// Price cannot be lower than cost
			price = Math.max(price, unitCost);

			return price;

		} else
			throw new NoPrice();

	}

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

	/*
	 * Calculates the minimum marginal utility that a consumer may have to
	 * afford a product with quality and price given
	 */
	public static double getPoorestConsumerMargUtil(double quality, double price) {
		return price / quality;
	}

	/*
	 * Calculates the price that the poorest consumer may afford
	 */
	public static double getPoorestConsumerMaxPrice(double quality) {
		return Consumers.getMinMargUtilOfQuality() * quality;
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
		double margCost = firm.getMarginalCostOfQuality(q);
		double derivQ = demandDerivRespToQuality(q, p);

		return (p - cost) * derivQ - margCost * demand;
	}

	private static double demandDerivRespToQuality(double q, double p) {
		// It is assumed firm is in the market

		double gini = (double) GetParameter("gini");
		double lambda = (1.0 + gini) / (2.0 * gini);
		double minMargUtil = Consumers.getMinMargUtilOfQuality();
		double mktSize = Consumers.getNumberOfConsumers();

		Firm loF = Market.firms.getLowerLimitFirm(q, false);
		Firm hiF = Market.firms.getHigherLimitFirm(q, false);

		if (loF == null && hiF == null) {
			return mktSize * lambda * Math.pow(minMargUtil, lambda)
					* Math.pow(q, lambda - 1.0) / Math.pow(p, lambda);

		} else if (loF == null && hiF != null) {
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

			if (pL == p)
				throw new Error(
						"Prices cannot be the same if both firms are in the market");

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
		double derivP = demandDerivRespToPrice(q, p);

		return (p - cost) * derivP + demand;
	}

	private static double demandDerivRespToPrice(double q, double p) {
		// It is assumed firm is in the market

		double gini = (double) GetParameter("gini");
		double lambda = (1.0 + gini) / (2.0 * gini);
		double minMargUtil = Consumers.getMinMargUtilOfQuality();
		double mktSize = Consumers.getNumberOfConsumers();

		double poorest = Utils.getPoorestConsumerMargUtil(q, p);

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

			if (pL == p)
				throw new Error(
						"Prices cannot be the same if both firms are in the market");

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

	/*
	 * it adds the new offer to history it removes the firm from and adds it
	 * back to FirmsByQ
	 */
	public static void setNewRationalOffer(Firm f) {
		Offer o;

		boolean wasInTheMarket = f.isInTheMarket();

		Market.firms.removeFromFirmsByQ(f);

		if (wasInTheMarket)
			o = Utils.getMaximizingOffer(f);
		else
			o = Utils.getReenteringOffer(f);

		f.getHistory().addCurrentState(new FirmState(o));

		Market.firms.addToFirmsByQ(f);

	}

	public static double getTheoreticalDemand(double q, double p) {
		/*
		 * Price cannot be equal to theoretical loF & hiF price. If that were
		 * the case, theoretical demand is calculated for an intermediate price
		 */
		Firm loF = Market.firms.getLowerLimitFirm(q, false);
		Firm hiF = Market.firms.getHigherLimitFirm(q, false);

		double gini = (double) GetParameter("gini");
		double lambda = (1.0 + gini) / (2.0 * gini);
		double minMargUtil = Consumers.getMinMargUtilOfQuality();
		double mktSize = Consumers.getNumberOfConsumers();

		if (loF == null && hiF == null) {
			return mktSize * Math.pow(minMargUtil, lambda)
					* Math.pow(q / p, lambda);

		} else if (loF == null && hiF != null) {
			double pH = hiF.getPrice();
			double qH = hiF.getQuality();

			if (pH == p) {
				// If it has the same price than a competitor with higher q
				// the theoretical demand is 0
				return 0.0;
			} else
				return mktSize
						* Math.pow(minMargUtil, lambda)
						* (Math.pow(q / p, lambda) - Math.pow((qH - q)
								/ (pH - p), lambda));

		} else if (loF != null && hiF == null) {
			double pL = loF.getPrice();
			double qL = loF.getQuality();

			if (pL == p) {
				double priceStep = Market.firms.getPriceStepDistrib()
						.nextDouble();
				p = pL + priceStep;
			}

			return mktSize * Math.pow(minMargUtil, lambda)
					* Math.pow((q - qL) / (p - pL), lambda);

		} else {
			// loF != null && hiF != null
			double pH = hiF.getPrice();
			double qH = hiF.getQuality();
			double pL = loF.getPrice();
			double qL = loF.getQuality();

			if (pH == p) {
				// If it has the same price than a competitor with higher q
				// the theoretical demand is 0
				return 0.0;

			} else if (pL == p) {
				double priceStep = Market.firms.getPriceStepDistrib()
						.nextDouble();
				p = pL + priceStep;
			}

			return mktSize
					* Math.pow(minMargUtil, lambda)
					* (Math.pow((q - qL) / (p - pL), lambda) - Math.pow(
							(qH - q) / (pH - p), lambda));

		}

	}
}

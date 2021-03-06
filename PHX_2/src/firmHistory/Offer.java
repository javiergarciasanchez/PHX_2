package firmHistory;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import firms.Utils;
import pHX_2.Market;

public class Offer {

	private double quality;
	private double price;

	// Read boundaries of parameters
	private static double minPrice;
	private static double maxPrice;
	private static double minQuality;
	private static double maxQuality;

	public static void resetStaticVars() {
		// resets static variables
		minPrice = (double) GetParameter("minPrice");

		/*
		 * Max price is expanded so price is enough to cover the cost of the
		 * most expensive firm
		 */
		maxPrice = (double) GetParameter("initialMaxPrice");

		minQuality = (double) GetParameter("minQuality");
		maxQuality = (double) GetParameter("maxQuality");
	}

	public Offer() {
	}

	public Offer(double q, double p) {
		setQuality(q);
		setPrice(p);
	}

	public Offer(Offer offer) {
		setQuality(offer.getQuality());
		setPrice(offer.getPrice());
	}

	public static double getDefaultQualityStep() {
		return Market.firms.getQualityStepDistrib().nextDouble();
	}

	public static double getDefaultPriceStep() {
		return Market.firms.getPriceStepDistrib().nextDouble();
	}

	public static double getAvailableQ(double tentativeQ) {

		double retQ;

		// Check limits
		if (tentativeQ < minQuality)
			retQ = minQuality;

		else if (tentativeQ > maxQuality)
			retQ = maxQuality;

		else
			retQ = tentativeQ;

		// Check if another firm has chosen this value
		while (Market.firms.containsQ(retQ)) {
			retQ = retQ + Math.ulp(retQ);
		}

		if (retQ > maxQuality) {
			// There is no quality available to choose by increasing quality
			retQ = maxQuality;
			while (Market.firms.containsQ(retQ)) {
				retQ = retQ - Math.ulp(retQ);
			}
			if (retQ < minQuality) {
				throw new Error(
						"No Quality available. All possible values of Quality are taken. Choose a lower amount of firms");
			}
		}

		return retQ;

	}

	public void modifyQuality(double prevQStep, int sign) {

		if (Math.signum(prevQStep) == (-sign)) {
			// It wants to change the direction of a previous quality change
			// Reduce it by half to avoid going back to the same offer
			double q = getAvailableQ(getQuality() + Math.abs(prevQStep) / 2.0
					* sign);

			setQuality(q);

		} else {
			// Either price didn't change before or it wants to change it in
			// same direction
			if (prevQStep == 0)
				prevQStep = getDefaultQualityStep();
			double q = getAvailableQ(getQuality() + Math.abs(prevQStep) * sign);
			setQuality(q);
		}
	}

	public void modifyPrice(double prevPStep, int sign) {
		if (Math.signum(prevPStep) == (-sign))
			// It wants to change the direction of a previous price change
			// Reduced it by half to avoid going back to the same offer
			setPrice(getPrice() + Math.abs(prevPStep) / 2.0 * sign);
		else {
			// Either price didn't change before or it wants to change it in
			// same
			// direction
			if (prevPStep == 0)
				prevPStep = getDefaultPriceStep();
			setPrice(getPrice() + Math.abs(prevPStep) * sign);
		}
	}

	public double getQuality() {
		return quality;
	}

	public void setQuality(double q) {
		quality = q;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {

		if (quality == 0.0)
			throw new Error("quality should be set before price");

		// Price shouldn't be lower than the maximum price that the poorest
		// consumer may afford
		price = Math.max(price, Utils.getPoorestConsumerMaxPrice(quality));

		// Price should be between limits
		price = Math.max(price, minPrice);
		price = Math.min(price, maxPrice);
		
		this.price = price;

	}

	public static double getMinPrice() {
		return minPrice;
	}

	public static double getMaxPrice() {
		return maxPrice;
	}

	public static void setMaxPrice(double p) {
		maxPrice = p;
	}

	public static double getMinQuality() {
		return minQuality;
	}

	public static double getMaxQuality() {
		return maxQuality;
	}

	public static double getMaxInitialQuality() {
		// The purpose of this function is to avoid reaching the maxQ
		// Ideally a firm that increases its quality every step, shouldn't reach
		// maxQ
		return maxQuality / 4.0;
	}

	public String toString() {
		return "Q: " + quality + " P: " + price;
	}
}

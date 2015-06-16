package firmState;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import pHX_2.Market;

public class Offer {

	private double quality;
	private double price;
	private OfferType offerType = null;

	// Read boundaries of parameters
	private static double minPrice;
	private static double maxPrice;
	private static double minQuality;
	private static double maxQuality;

	public static void resetStaticVars() {
		// resets static variables
		minPrice = (double) GetParameter("minPrice");
		maxPrice = (double) GetParameter("maxPrice");

		minQuality = (double) GetParameter("minQuality");
		maxQuality = (double) GetParameter("maxQuality");
	}

	public Offer() {
		setQuality(minQuality);
		setPrice(minPrice);
	}

	public Offer(double q, double p) {
		setQuality(q);
		setPrice(p);
	}

	public Offer(OfferType offerType, Offer currOffer) {

		double p = currOffer.getPrice();
		double q = currOffer.getQuality();
		
		setOfferType(offerType);
		setQuality(q);
		setPrice(p);
		
		double priceStep = Market.firms.getPriceStepDistrib().nextDouble();
		double qualityStep = Market.firms.getQualityStepDistrib().nextDouble();
//		double priceStep = (Double) GetParameter("priceStepMean");
//		double qualityStep = (Double) GetParameter("qualityStepMean");

		switch (offerType) {
		case INCREASE_PRICE:
			setPrice(p + priceStep );
			break;
		case DECREASE_PRICE:
			setPrice(p - priceStep);
			break;
		case INCREASE_QUALITY:
			setQuality(q + qualityStep);
			break;
		case DECREASE_QUALITY:
			setQuality(q - qualityStep);
			break;
		default:
			break;

		}
	}

	public boolean equals(Offer o) {
		return ((o.getPrice() == getPrice()) && (o.getQuality() == getQuality()));
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {

		if (price < minPrice)
			this.price = minPrice;

		else if (price > maxPrice)
			this.price = maxPrice;

		else
			this.price = price;

	}

	public double getQuality() {
		return quality;
	}

	public void setQuality(double quality) {

		if (quality < minQuality)
			this.quality = minQuality;

		else if (quality > maxQuality)
			this.quality = maxQuality;

		else
			this.quality = quality;

	}

	public OfferType getOfferType() {
		return offerType;
	}

	public void setOfferType(OfferType offerType) {
		this.offerType = offerType;
	}

	public static double getInitialQPerD() {
		return (getMaxQuality() - getMinQuality())
				/ (getMaxPrice() - getMinPrice());
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

	public String toString() {
		return "Q: " + quality + " P: " + price + " T: "
				+ ((offerType == null) ? "null" : offerType.toString());
	}
}

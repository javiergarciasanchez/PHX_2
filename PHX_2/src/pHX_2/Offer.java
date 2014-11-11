package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

public class Offer {

	private static final int MAX_X = 100, MAX_Y = 100, MIN_X = 0, MIN_Y = 0;
	private double quality;
	private double price;
	private OfferType offerType;

	// Read boundaries of parameters
	private static double minPrice = 0;
	private static double maxPrice = 0;
	private static double minQuality = 0;
	private static double maxQuality = 0;

	public Offer() {
		setQuality(minQuality);
		setPrice(minPrice);
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

	public static double getMinPrice() {
		if (minPrice == 0)
			minPrice = (double) GetParameter("minPrice");

		return minPrice;
	}

	public static double getMaxPrice() {
		if (maxPrice == 0)
			maxPrice = (double) GetParameter("maxPrice");

		return maxPrice;
	}

	public static double getMinQuality() {
		if (minQuality == 0)
			minQuality = (double) GetParameter("minQuality");

		return minQuality;
	}

	public static double getMaxQuality() {
		if (maxQuality == 0)
			maxQuality = (double) GetParameter("maxQuality");

		return maxQuality;
	}

	public static double getMaxX() {
		return MAX_X;
	}

	public static double getMaxY() {
		return MAX_Y;
	}

	public double getX() {
		return (price - minPrice) / (maxPrice - minPrice) * (MAX_X - MIN_X)
				+ MIN_X;
	}

	public double getY() {
		return (quality - minQuality) / (maxQuality - minQuality)
				* (MAX_Y - MIN_Y) + MIN_Y;
	}

	public String toString() {
		return "Q: " + quality + " P: " + price + " T: "
				+ ((offerType == null) ? "null" : offerType.toString());
	}
}

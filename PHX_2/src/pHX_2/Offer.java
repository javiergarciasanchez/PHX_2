package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

public class Offer {

	private double quality;
	private double price;

	// Read boundaries of parameters
	private static double MinPrice = 0;
	private static double MaxPrice = 0;
	private static double MinQuality = 0;
	private static double MaxQuality = 0;

	public Offer() {
		setQuality(MinQuality);
		setPrice(MinPrice);
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {

		if (price < MinPrice)
			this.price = MinPrice;

		else if (price > MaxPrice)
			this.price = MaxPrice;

		else
			this.price = price;

	}

	public double getQuality() {
		return quality;
	}

	public void setQuality(double quality) {

		if (quality < MinQuality)
			this.quality = MinQuality;

		else if (quality > MaxQuality)
			this.quality = MaxQuality;

		else
			this.quality = quality;

	}

	public static double getMinPrice() {
		if (MinPrice == 0)
			MinPrice = (double) GetParameter("minPrice");

		return MinPrice;
	}

	public static double getMaxPrice() {
		if (MaxPrice == 0)
			MaxPrice = (double) GetParameter("maxPrice");

		return MaxPrice;
	}

	public static double getMinQuality() {
		if (MinQuality == 0)
			MinQuality = (double) GetParameter("minQuality");

		return MinQuality;
	}

	public static double getMaxQuality() {
		if (MaxQuality == 0)
			MaxQuality = (double) GetParameter("maxQuality");

		return MaxQuality;
	}

	public static double getMaxX() {
		return getMaxPrice();
	}

	public static double getMaxY() {
		return getMaxQuality() - getMinQuality();
	}

	public double getX() {
		return price;
	}

	public double getY() {
		return quality - getMinQuality();
	}
}

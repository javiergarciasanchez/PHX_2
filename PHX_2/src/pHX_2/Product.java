package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

public class Product {

	private double quality;
	private double price;

	// Read boundaries of parameters
	private static double MinPrice = (double) GetParameter("minPrice");
	private static double MaxPrice = (double) GetParameter("maxPrice");
	private static double MinQuality = (double) GetParameter("minQuality");
	private static double MaxQuality = (double) GetParameter("maxQuality");

	public static double MaxX = MaxPrice;
	public static double MaxY = MaxQuality - MinQuality;

	public Product(Market market) {

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

	public double getX() {
		return price;
	}

	public double getY() {
		return quality - MinQuality;
	}
}

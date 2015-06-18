package firmTypes;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.awt.Color;

import consumers.Consumers;
import consumers.Pareto;
import firmState.Offer;
import firms.Firm;
import firms.Utils;
import pHX_2.Market;

// This firm offers low price and low quality
public class BasePyramidFirm extends Firm {

	public BasePyramidFirm() {
		super();

		assignMaxMargUtilForSegment();

	}

	private void assignMaxMargUtilForSegment() {

		// Assign minimum Marginal Utility of Quality for the segment
		double acumProb = (double) GetParameter("basePyramidSegment");

		double gini = (double) GetParameter("gini");
		double lambda = (1.0 + gini) / (2.0 * gini);

		double minimum = Consumers.getMinMargUtilOfQuality();

		maxPoorestConsumerMargUtil = Pareto.inversePareto(acumProb, minimum,
				lambda);

	}

	protected double getInitialQuality() {
		double lowerQ = Offer.getMinQuality();
		double higherQ = (Offer.getMinQuality() + Offer.getMaxQuality()) / 2.0;
		return Utils.getRandomInitialQuality(lowerQ, higherQ);
	}

	protected double getInitialPrice(double q) throws NoPrice {
		double minPrice = Math.max(Offer.getMinPrice(), unitCost(q));

		// Chooses a price that is one step below maximum competitive price
		double price = Utils.getMaxCompetitivePriceToEntry(q);

		if (price > minPrice) {
			double priceStep = Market.firms.getPriceStepDistrib().nextDouble();
			price = price - priceStep;

			// Price cannot be lower than cost
			price = Math.max(price, minPrice);
			
			return price;

		} else
			throw new NoPrice();

	}

	@Override
	public Color getColor() {
		return Color.CYAN;
	}

}

package firmTypes;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.awt.Color;

import consumers.Pareto;
import firmState.Offer;
import firmState.OfferType;
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

		double minimum = Market.consumers.getMinMargUtilOfQuality();

		maxPoorestConsumerMargUtil = Pareto.inversePareto(acumProb, minimum,
				lambda);

	}

	@Override
	protected void fillOfferTypePreference() {

		offerTypePreference[0] = OfferType.DECREASE_PRICE;
		offerTypePreference[1] = OfferType.DECREASE_QUALITY;
		offerTypePreference[2] = OfferType.INCREASE_PRICE;
		offerTypePreference[3] = OfferType.INCREASE_QUALITY;

	}

	@Override
	protected Offer getInitialOffer() {

		double q = getRandomInitialQuality();
		Offer offer = new Offer(q, getRandomInitialPrice(q));
		return offer;

	}

	private double getRandomInitialQuality() {
		double lowerQ = Offer.getMinQuality();
		double higherQ = (Offer.getMinQuality() + Offer.getMaxQuality()) / 2.0;
		return Utils.getRandomInitialQuality(lowerQ, higherQ);
	}

	private double getRandomInitialPrice(double q) {

		// Chooses a price that is one step below maximum competitive price
		double price = Utils.getMaxCompetitivePriceToEntry(q);
		double priceStep = Market.firms.getPriceStepDistrib().nextDouble();
		price = price - priceStep;

		// Price cannot be lower than cost
		price = Math.max(price, unitCost(q));
		
		return price;
		
	}

	@Override
	public Color getColor() {
		return Color.CYAN;
	}

}

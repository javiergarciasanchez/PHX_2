package firmTypes;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.awt.Color;

import consumers.Pareto;
import firmState.Offer;
import firmState.OfferType;
import firms.Firm;
import firms.Utils;
import pHX_2.Market;

// This firm firmState high price and high quality
public class PremiumFirm extends Firm {

	public PremiumFirm() {
		super();

		assignMinMargUtilForSegment();
	}

	private void assignMinMargUtilForSegment() {

		// Assign minimum Marginal Utility of Quality for the segment
		double acumProb = 1.0 - (double) GetParameter("premiumSegment");

		double gini = (double) GetParameter("gini");
		double lambda = (1.0 + gini) / (2.0 * gini);

		double minimum = Market.consumers.getMinMargUtilOfQuality();

		minPoorestConsumerMargUtil = Pareto.inversePareto(acumProb, minimum,
				lambda);

	}

	@Override
	protected Offer getInitialOffer() {

		double q = getRandomInitialQuality();
		Offer offer = new Offer(q, getRandomInitialPrice(q));
		return offer;

	}

	private double getRandomInitialQuality() {
		double lowerQ = (Offer.getMinQuality() + Offer.getMaxQuality()) / 2.0;
		double higherQ = Offer.getMaxQuality();
		return Utils.getRandomInitialQuality(lowerQ, higherQ);
	}

	private double getRandomInitialPrice(double q) {

		// Chooses a price that is one step below maximum competitive price
		double price = Utils.getMaxCompetitivePriceToEntry(q);
		double priceStep = Market.firms.getPriceStepDistrib().nextDouble();
		price = price - priceStep;

		// Price cannot be lower than the required by the premium segment
		// The limit is established in terms of minimal marginal utility of the
		// poorest consumer
		double premiumSegmentLimit = Utils.getPoorestConsumerMinPrice(
				minPoorestConsumerMargUtil, q);
		price = Math.max(price, premiumSegmentLimit);

		// Price cannot be lower than cost (This overcomes segment limit)
		price = Math.max(price, unitCost(q));

		return price;

	}

	@Override
	public Color getColor() {
		return Color.BLUE;
	}

	@Override
	protected void fillOfferTypePreference() {
		offerTypePreference[0] = OfferType.INCREASE_QUALITY;
		offerTypePreference[1] = OfferType.INCREASE_PRICE;
		offerTypePreference[2] = OfferType.DECREASE_PRICE;
		offerTypePreference[3] = OfferType.DECREASE_QUALITY;
	}
}

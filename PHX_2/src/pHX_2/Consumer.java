package pHX_2;

import java.util.ArrayList;

import repast.simphony.engine.schedule.ScheduledMethod;

public class Consumer {

	private Firm chosenFirm;

	private double margUtilOfQuality;
	private ArrayList<Firm> history;
	private ArrayList<Firm> knownFirms;
	private double utility;

	protected static long consumerIDCounter = 1;
	public static void setConsumerIDCounter(long consumerIDCounter) {
		Consumer.consumerIDCounter = consumerIDCounter;
	}

	protected long consumerIntID = consumerIDCounter++;
	protected String ID = "Cons. " + consumerIntID;

	public Consumer() {

		CreateMarket.consumersContext.add(this);

		history = new ArrayList<Firm>();
		knownFirms = new ArrayList<Firm>();

		assignPreferences();

		// Set location in projection
		CreateMarket.consumersProyection.moveTo(this, consumerX(), consumerY());

	}

	private double consumerX() {
		return margUtilOfQuality - Consumers.getMinMargUtilOfQuality();
	}

	private double consumerY() {
		// Puts the consumer in the middle of vertical axis
		return Consumers.getMaxY() / 2.0;
	}

	// Assigns parameters for the utility function
	private void assignPreferences() {

		// We need to introduce randomness

		// There is no need to introduce a marginal utility of money
		// because it is implicit in the marginal utility of quality
		margUtilOfQuality = Consumers.getMargUtilOfQualityDistrib()
				.nextDouble();

		// Assign border if out range
		margUtilOfQuality = Math.max(Consumers.getMinMargUtilOfQuality(),
				margUtilOfQuality);
		margUtilOfQuality = Math.min(Consumers.getMaxMargUtilOfQuality(),
				margUtilOfQuality);

	}

	public void addToKnownFirms(Firm firm) {
		knownFirms.add(firm);
	}

	@ScheduledMethod(start = 1, priority = RunPriority.CHOOSE_FIRM_PRIORITY, interval = 1)
	public Firm chooseFirm() {

		double utility = 0;
		Firm maxUtilFirm = null;
		referencePoint();

		// Choose firm
		for (int i = 0; i < knownFirms.size(); i++) {

			Firm f = knownFirms.get(i);

			// check the firm still exists
			if (!CreateMarket.market.contains(f))
				continue;

			double tmpUtil = utility(f);

			if (tmpUtil > utility) {
				maxUtilFirm = f;
				utility = tmpUtil;
			}
		}

		// Adjust demand and keep record
		if (maxUtilFirm != null) {
			maxUtilFirm.setDemand(maxUtilFirm.getDemand() + 1);
			history.add(maxUtilFirm);
		}

		chosenFirm = maxUtilFirm;

		return chosenFirm;

	}

	private double utility(Firm f) {

		utility = margUtilOfQuality * perceivedQuality(f) - f.getPrice();

		return utility;

	}

	private double perceivedQuality(Firm f) {
		// If consumer has already bought from firm f, the perceived quality is
		// the real one
		// if f is unknown, then consumer chooses a reference quality per dollar
		// and multiplies it by the price asked by f

		double qPerDollar;

		if (history.isEmpty()) {
			// First purchase. quality per dollar is estimated using range
			// borders
			// Avg of Max and Min points are used to estimate quality per dollar

			qPerDollar = (Offer.getMaxQuality() / Offer.getMaxPrice() + Offer
					.getMinQuality() / Offer.getMinPrice()) / 2.0;

		} else if (history.contains(f)) {
			// The firm was chosen in the the past, use its real quality

			return f.getQuality();

		} else {
			// Use last purchase quality per dollar

			// Use quality per Dollar of last purchase
			Firm lastFirm = history.get(history.size() - 1);

			qPerDollar = lastFirm.getQuality() / lastFirm.getPrice();

		}

		return qPerDollar * f.getPrice();
	}

	private Offer referencePoint() {

		int hSize = history.size();

		if (hSize == 0)
			return null;
		else {
			double sumPrice = 0., sumQuality = 0.;
			Offer rPoint = new Offer();

			for (int i = 0; i < hSize; i++) {
				Firm f = history.get(i);

				// Check that f still exists
				if (!CreateMarket.consumersContext.contains(f)) {
					history.remove(f);
					continue;
				}

				// Price of last period is assumed known
				sumPrice += f.getPrice();

				// Quality is assumed constant, otherwise historical q should be
				// used
				sumQuality += f.getQuality();
			}

			rPoint.setPrice(sumPrice / hSize);
			rPoint.setQuality(sumQuality / hSize);

			return rPoint;

		}

	}

	// Procedures for inspecting values
	public double getMargUtilOfQuality() {
		return margUtilOfQuality;
	}

	public String getChosenFirmID() {
		return (chosenFirm == null) ? "Substitute" : chosenFirm.ID;
	}

	public double getChosenFirmIntID() {
		return (chosenFirm == null) ? 0.0 : (double) chosenFirm.getFirmIntID();
	}

	public double getConsumerIntID() {
		return consumerIntID;
	}
	
	public String getConsumerNumID() {
		return Long.toString(consumerIntID);
	}

	public String getConsumerID() {
		return ID;
	}

	public double getUtility() {
		return utility;
	}

	public String toString() {
		return ID;
	}

}

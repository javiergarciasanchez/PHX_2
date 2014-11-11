package pHX_2;

import java.util.ArrayList;

import repast.simphony.engine.schedule.ScheduledMethod;

public class Consumer {

	private double margUtilOfQuality;
	private ArrayList<Firm> history;
	private ArrayList<Firm> knownFirms;

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
		CreateMarket.consumersProyection.moveTo(this, getX(), getY());

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

	private double getX() {
		return (margUtilOfQuality - Consumers.getMinMargUtilOfQuality())
				/ (Consumers.getMaxMargUtilOfQuality() - Consumers
						.getMinMargUtilOfQuality())
				* (Consumers.getMaxX() - Consumers.getMinX())
				+ Consumers.getMinX();
	}

	private double getY() {
		// Puts the consumer in the middle of vertical axis
		return (Consumers.getMaxY() - Consumers.getMinY()) / 2.0
				+ Consumers.getMinY();
	}

	public void addToKnownFirms(Firm firm) {
		knownFirms.add(firm);
	}

	@ScheduledMethod(start = 1, priority = RunPriority.CHOOSE_FIRM_PRIORITY, interval = 1)
	public Firm chooseFirm() {

		double utility = 0;
		Firm maxUtilFirm = null;

		// Choose firm among the known ones
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

		return maxUtilFirm;

	}

	private double utility(Firm f) {

		return margUtilOfQuality * perceivedQuality(f) - f.getPrice();

	}

	private double perceivedQuality(Firm f) {
		// If consumer has already bought from firm f, the perceived quality is
		// the current real utility of firm f
		// if f is unknown, then consumer chooses a reference quality per dollar
		// and multiplies it by the price asked by f

		double qPerDollar;

		if (history.contains(f)) {
			// The firm was chosen in the the past, use its current real quality

			return f.getQuality();

		} else if (history.isEmpty()) {
			// First purchase.
			// quality per dollar is estimated using range borders
			// avg quality / avg price

			qPerDollar = ((Offer.getMaxQuality() + Offer.getMinQuality()) / 2.0)
					/ ((Offer.getMaxPrice() + Offer.getMinPrice()) / 2.0);

		} else {
			// Use last purchase quality per dollar
			// Use quality per Dollar of last purchase
			Firm lastFirm = history.get(history.size() - 1);

			qPerDollar = lastFirm.getQuality() / lastFirm.getPrice();

		}

		return qPerDollar * f.getPrice();
	}

	// Procedures for inspecting values
	public double getMargUtilOfQuality() {
		return margUtilOfQuality;
	}

	public String getChosenFirmID() {
		int size = history.size();

		if (size > 0)
			return history.get(size - 1).toString();
		else
			return "Substitute";
	}

	public double getChosenFirmIntID() {
		int size = history.size();

		if (size > 0)
			return history.get(size - 1).getFirmIntID();
		else
			return 0.0;
	}

	public double getUtility() {
		int size = history.size();

		if (size > 0)
			return utility(history.get(size - 1));
		else
			return 0.0;
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

	public String toString() {
		return ID;
	}

}

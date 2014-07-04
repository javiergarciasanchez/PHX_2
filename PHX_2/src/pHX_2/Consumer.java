package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.util.ArrayList;

import repast.simphony.space.continuous.ContinuousSpace;

public class Consumer {

	private Firm chosenFirm;
	private Market market;
	private double margUtilOfQuality;
	private ArrayList<Firm> history;
	private Product referencePoint;
	private double utility;

	protected static long consumerIDCounter = 1;
	protected long consumerIntID = consumerIDCounter++;
	protected String ID = "Cons. " + consumerIntID;

	public static double MinMargUtilOfQuality = (double) GetParameter("minMargUtilOfQuality");
	public static double MaxMargUtilOfQuality = (double) GetParameter("maxMargUtilOfQuality");

	public static double MaxX = MaxMargUtilOfQuality - MinMargUtilOfQuality;
	public static double MaxY = MaxX / 10.0;

	public Consumer(Market market) {

		this.market = market;
		market.context.add(this);

		assignPreferences();

		referencePoint = new Product(market);
		history = new ArrayList<Firm>();

		// Set location in projection
		@SuppressWarnings("unchecked")
		ContinuousSpace<Object> p = market.context.getProjection(
				ContinuousSpace.class, "Consumers");

		// Assign marginal utility of quality and check Boundaries
		double margUtilOfQuality = getMargUtilOfQuality();

		if (margUtilOfQuality < MinMargUtilOfQuality) {

			margUtilOfQuality = MinMargUtilOfQuality;

		} else if (margUtilOfQuality > MaxMargUtilOfQuality) {

			margUtilOfQuality = MaxMargUtilOfQuality;
		}

		p.moveTo(this, consumerX(), consumerY());

	}

	private double consumerX() {
		return margUtilOfQuality - MinMargUtilOfQuality;
	}

	private double consumerY() {
		// Puts the consumer in the middel of vertical axis
		return MaxY / 2.0;
	}

	// Assigns parameters for the utility function
	private void assignPreferences() {

		// We need to introduce randomness

		// There is no need to introduce a marginal utility of money
		// because it is implicit in the marginal utility of quality
		margUtilOfQuality = market.margUtilOfQualityDistrib.nextDouble();

	}

	public Firm chooseFirmAndNextStep() {

		double utility = 0;
		Firm maxUtilFirm = null;
		referencePoint = referencePoint();

		// Choose firm
		for (Object f : market.context.getObjects(Firm.class)) {

			double tmpUtil = utility((Firm) f);

			if (tmpUtil > utility) {
				maxUtilFirm = (Firm) f;
				utility = tmpUtil;
			}
		}

		// Next Step
		if (maxUtilFirm != null)
			history.add(maxUtilFirm);

		chosenFirm = maxUtilFirm;

		return chosenFirm;

	}

	private double utility(Firm f) {

		utility = margUtilOfQuality * perceivedQuality(f)
				- f.firmState.product.getPrice();

		return utility;

	}

	private double perceivedQuality(Firm f) {

		// The firm was chosen in the the past
		if (history.contains(f)) {

			return f.firmState.product.getQuality();

		} else {

			return referencePoint.getQuality() / referencePoint.getPrice()
					* f.firmState.product.getPrice();

		}

	}

	private Product referencePoint() {

		int hSize = history.size();

		if (hSize == 0)
			return market.substitute;
		else {
			double sumPrice = 0., sumQuality = 0.;
			Product rPoint = new Product(market);

			for (int i = 0; i < hSize; i++) {
				Firm f = history.get(i);

				// Check that f still exists
				if (!market.context.contains(f)) {
					history.remove(f);
					continue;
				}

				// Price of last period is assumed known
				sumPrice += f.history[0].product.getPrice();

				// Quality is assumed constant, otherwise historical q should be
				// used
				sumQuality += f.history[0].product.getQuality();
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

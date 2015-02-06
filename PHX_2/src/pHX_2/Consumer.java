package pHX_2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import repast.simphony.engine.schedule.ScheduledMethod;

public class Consumer {

	private double margUtilOfQuality;
	private double explorationPref;
	private Firm chosenFirm;
	private HashSet<Firm> exploredFirms;
	private ArrayList<Firm> knownFirmsNotExplored;

	protected static long consumerIDCounter = 1;

	public static void setConsumerIDCounter(long consumerIDCounter) {
		Consumer.consumerIDCounter = consumerIDCounter;
	}

	protected long consumerIntID = consumerIDCounter++;
	protected String ID = "Cons. " + consumerIntID;

	public Consumer() {

		CreateMarket.consumersContext.add(this);

		exploredFirms = new HashSet<Firm>();
		knownFirmsNotExplored = new ArrayList<Firm>();

		assignPreferences();

		// Set location in projection
		CreateMarket.consumersProyection.moveTo(this, getX(), getY());

	}

	// Assigns parameters for the choice function
	private void assignPreferences() {

		// We need to introduce randomness

		setMargUtilOfQuality();

		setExplorationPref();

	}

	private void setMargUtilOfQuality() {

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

	private void setExplorationPref() {

		explorationPref = Consumers.getExplorationPrefDistrib().nextDouble();

		// Probability should be between 0 and 1
		explorationPref = Math.max(0.0, explorationPref);
		explorationPref = Math.min(1.0, explorationPref);

	}

	public void addToKnownFirms(Firm firm) {
		knownFirmsNotExplored.add(firm);
	}

	@ScheduledMethod(start = 1, priority = RunPriority.CHOOSE_FIRM_PRIORITY, interval = 1)
	public void chooseFirm() {

		if (exploredFirms.isEmpty()
				|| Consumers.explorationDistrib.nextInt(1, explorationPref) == 1)
			chosenFirm = exploreKnownFirm();
		else
			chosenFirm = chooseMaximizingFirm();

		// Adjust demand and keep record
		if (chosenFirm != null) {
			chosenFirm.setDemand(chosenFirm.getDemand() + 1);
			exploredFirms.add(chosenFirm);
			knownFirmsNotExplored.remove(chosenFirm);
		}

	}

	private Firm exploreKnownFirm() {
		Firm f = null;

		// Returns a known firm that has not been explored

		if (knownFirmsNotExplored.isEmpty()) {
			// Either all known firms are explored or there is not known firm
			return chooseMaximizingFirm();
		}
		
		Collections.shuffle(knownFirmsNotExplored);
		
		for (Iterator<Firm> i = knownFirmsNotExplored.iterator(); i.hasNext();) {
			f = i.next();

			// check the firm still exists
			if (!CreateMarket.market.contains(f)) {
				i.remove();
				continue;
			} else {
				return f;
			}

		}

		// No firm was chosen because all are already dead
		return null;

	}

	private Firm chooseMaximizingFirm() {

		// No firms to choose from
		if (exploredFirms.isEmpty())
			return null;

		// Search among the explored ones the one that maximizes utility
		double utility = 0;
		Firm maxUtilFirm = null;

		for (Iterator<Firm> i = exploredFirms.iterator(); i.hasNext();) {

			Firm f = i.next();

			// check the firm still exists
			if (!CreateMarket.market.contains(f)) {
				i.remove();
				continue;
			}

			double tmpUtil = utility(f);

			if (tmpUtil > utility) {
				maxUtilFirm = f;
				utility = tmpUtil;
			}
		}

		return maxUtilFirm;

	}

	private double utility(Firm f) {

		return margUtilOfQuality * f.getQuality() - f.getPrice();

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

	// Procedures for inspecting values

	public double getMargUtilOfQuality() {
		return margUtilOfQuality;
	}

	public String getChosenFirmID() {
		if (chosenFirm == null)
			return "Substitute";
		else
			return chosenFirm.toString();
	}

	public double getChosenFirmIntID() {
		if (chosenFirm == null)
			return 0.0;
		else
			return chosenFirm.getFirmIntID();
	}

	public double getUtility() {
		if (chosenFirm == null)
			return 0.0;
		else
			return utility(chosenFirm);
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

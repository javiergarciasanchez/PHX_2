package demand;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;

import pHX_2.Market;
import pHX_2.Firm;
import pHX_2.RunPriority;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public class Consumer {

	private double margUtilOfQuality;
	private double explorationPref;
	private Firm chosenFirm;
	private HashSet<Firm> exploredFirms;
	private ArrayList<Firm> knownFirmsNotExplored;

	protected static long consumerIDCounter = 1;

	protected long consumerIntID = consumerIDCounter++;
	protected String ID = "Cons. " + consumerIntID;

	public static void resetStaticVars() {
		// resets static variables
		consumerIDCounter = 1;
	}

	public Consumer() {

		Market.consumers.add(this);

		exploredFirms = new HashSet<Firm>();
		knownFirmsNotExplored = new ArrayList<Firm>();

		assignPreferences();

	}

	// Assigns parameters for the choice function
	private void assignPreferences() {

		// We need to introduce randomness

		setMargUtilOfQuality();

		setExplorationPref();

	}

	public void addToProjections() {
		// Set location in projections
		Market.consumersProjection.add(this);
		Market.margUtilProjection.add(this);
	}

	private void setMargUtilOfQuality() {

		// There is no need to introduce a marginal utility of money
		// because it is implicit in the marginal utility of quality
		margUtilOfQuality = Market.consumers.getMargUtilOfQualityDistrib()
				.nextDouble();

		// Assign border if out range
		margUtilOfQuality = Math.max(
				Market.consumers.getMinMargUtilOfQuality(), margUtilOfQuality);
		
		// Upper limit is unbounded. The max marg utility is updated to scale graphs
		Market.consumers.setMaxMargUtilOfQuality(Math.max(
				Market.consumers.getMaxMargUtilOfQuality(), margUtilOfQuality));

	}

	private void setExplorationPref() {

		explorationPref = Market.consumers.getExplorationPrefDistrib()
				.nextDouble();

		// Probability should be between 0 and 1
		explorationPref = Math.max(0.0, explorationPref);
		explorationPref = Math.min(1.0, explorationPref);

	}

	public void addToKnownFirms(Firm firm) {
		knownFirmsNotExplored.add(firm);
	}

	public void removeFromKnownFirms(Firm firm) {
		knownFirmsNotExplored.remove(firm);
	}

	public void removeFromExploredFirms(Firm firm) {
		exploredFirms.remove(firm);
	}

	@ScheduledMethod(start = 1, priority = RunPriority.CHOOSE_FIRM_PRIORITY, interval = 1)
	public void chooseFirm() {

		if (!exploredFirms.isEmpty() && !knownFirmsNotExplored.isEmpty())

			if (RandomHelper.nextIntFromTo(0, 1) == 1)
				chosenFirm = exploreKnownFirm();
			else
				chosenFirm = chooseMaximizingFirm();

		else if (exploredFirms.isEmpty() && !knownFirmsNotExplored.isEmpty())
			chosenFirm = exploreKnownFirm();

		else if (!exploredFirms.isEmpty() && knownFirmsNotExplored.isEmpty())
			chosenFirm = chooseMaximizingFirm();

		else
			// It should never come here
			chosenFirm = null;

		// Adjust demand
		if (chosenFirm != null)
			chosenFirm.setDemand(chosenFirm.getDemand() + 1);

	}

	public Firm getChosenFirm() {
		return chosenFirm;
	}

	// Returns a known firm that has not been explored
	// Before calling, it should be checked that knownFirmsNotExplored be
	// not Empty
	// He doesn't check for negative utility because he never tried these firms
	private Firm exploreKnownFirm() {
		Firm f;

		int i = RandomHelper.nextIntFromTo(0, knownFirmsNotExplored.size() - 1);
		f = knownFirmsNotExplored.get(i);

		exploredFirms.add(f);
		knownFirmsNotExplored.remove(f);

		return f;
	}

	// Returns the explored firm that maximizes utility
	// Before calling, it should be checked that exploredFirms be
	// not Empty
	private Firm chooseMaximizingFirm() {

		double utility = 0;
		Firm maxUtilFirm = null;

		for (Firm f : exploredFirms) {

			double tmpUtil = utility(f);

			if (tmpUtil > utility) {
				maxUtilFirm = f;
				utility = tmpUtil;
			}

		}

		// Consumer doesn't choose any firm if utility is negative
		if (utility > 0)
			return maxUtilFirm;
		else
			return null;

	}

	private double utility(Firm f) {

		return margUtilOfQuality * f.getQuality() - f.getPrice();

	}

	public int getRed() {
		return getChosenFirmColor().getRed();
	}

	public int getBlue() {
		return getChosenFirmColor().getBlue();
	}

	public int getGreen() {
		return getChosenFirmColor().getGreen();
	}

	private Color getChosenFirmColor() {
		if (chosenFirm == null)
			return Color.BLACK;
		else
			return chosenFirm.getColor();
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

package consumers;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;

import firms.Firm;
import pHX_2.Market;
import pHX_2.RecessionsHandler;
import pHX_2.RunPriority;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public class Consumer {

	// Set either "exploreKnownFirmByMaximExpect" or "randomlyExploreKnownFirm"
	private static final String EXPLORE_METHOD_NAME = "randomlyExploreKnownFirm";

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

	private void setMargUtilOfQuality() {

		// There is no need to introduce a marginal utility of money
		// because it is implicit in the marginal utility of quality
		margUtilOfQuality = Market.consumers.getMargUtilOfQualityDistrib()
				.nextDouble();

		// Assign border if out range
		margUtilOfQuality = Math.max(
				Market.consumers.getMinMargUtilOfQuality(), margUtilOfQuality);

		// Upper limit is unbounded. The max marg utility is updated to scale
		// graphs
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

	public void addToExploredFirms(Firm f) {
		exploredFirms.add(f);
		knownFirmsNotExplored.remove(f);
	}

	public void removeFromExploredFirms(Firm firm) {
		exploredFirms.remove(firm);
	}

	@ScheduledMethod(start = 1, priority = RunPriority.CHOOSE_FIRM_PRIORITY, interval = 1)
	public void chooseFirm() {
		Method exploreKnownFirmsMethod = null;

		try {
			exploreKnownFirmsMethod = Consumer.class
					.getDeclaredMethod(EXPLORE_METHOD_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (RandomHelper.nextIntFromTo(0, 1) == 1)
			chosenFirm = exploreOrChooseMax(exploreKnownFirmsMethod);
		else
			chosenFirm = chooseMaxOrExplore(exploreKnownFirmsMethod);

		// Adjust consumers
		if (chosenFirm != null)
			chosenFirm.setDemand(chosenFirm.getDemand() + 1);

	}

	// First explore and if no firm available then choose among explored
	private Firm exploreOrChooseMax(Method exploreMethod) {
		Firm f = null;

		try {
			f = (Firm) exploreMethod.invoke(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (f == null)
			f = chooseMaximizingFirm();

		return f;
	}

	// First choose among explored and if no firm available then explore
	private Firm chooseMaxOrExplore(Method exploreMethod) {

		Firm f = chooseMaximizingFirm();

		if (f == null) {
			try {
				f = (Firm) exploreMethod.invoke(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return f;

	}

	@ScheduledMethod(start = 1, priority = RunPriority.UPDATE_PROJECTIONS_PRIORITY, interval = 1)
	public void updateProjections() {
		Market.consumersProjection.update(this);
		Market.margUtilProjection.update(this);
		Market.consumptionProjection.update(this);
	}

	// Returns a known firm that has not been explored
	// Before calling, it should be checked that knownFirmsNotExplored be
	// not Empty
	// it doesn't check for negative utility because he never tried these firms
	@SuppressWarnings("unused")
	private Firm randomlyExploreKnownFirm() {

		if (exploredFirms.isEmpty())
			return null;
		else {

			Firm f;

			int i = RandomHelper.nextIntFromTo(0,
					knownFirmsNotExplored.size() - 1);
			f = knownFirmsNotExplored.get(i);

			addToExploredFirms(f);

			return f;
		}
	}

	// Returns a known firm that has not been explored
	// if knownFirmsNotExplored is empty return null

	@SuppressWarnings("unused")
	private Firm exploreKnownFirmByMaximExpect() {

		double utility = 0;
		Firm maxExpectUtilFirm = null;

		for (Firm f : knownFirmsNotExplored) {

			// it access f expected utility because f is known
			double tmpUtil = utility(f.getExpectedQuality(), f.getPrice());

			if (tmpUtil > utility) {
				maxExpectUtilFirm = f;
				utility = tmpUtil;
			}

		}

		// Consumer doesn't choose any firm if expected utilities are all
		// negative
		if (utility > 0) {
			addToExploredFirms(maxExpectUtilFirm);
			return maxExpectUtilFirm;
		} else
			return null;
	}

	// Returns the explored firm that maximizes utility
	// If exploredFirms is empty returns null
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

		return utility(f.getQuality(), f.getPrice());

	}

	private double utility(double q, double p) {
		return margUtilOfQuality * q - p;
	}

	private Color getChosenFirmColor() {
		if (chosenFirm == null)
			return Color.BLACK;
		else
			return chosenFirm.getColor();
	}

	// Procedures for inspecting values

	public int getRed() {
		return getChosenFirmColor().getRed();
	}

	public int getBlue() {
		return getChosenFirmColor().getBlue();
	}

	public int getGreen() {
		return getChosenFirmColor().getGreen();
	}

	public Firm getChosenFirm() {
		return chosenFirm;
	}

	public double getMargUtilOfQuality() {
		double recessionImpact = 1 - RecessionsHandler.getRecesMagnitude();
		return margUtilOfQuality * recessionImpact;
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

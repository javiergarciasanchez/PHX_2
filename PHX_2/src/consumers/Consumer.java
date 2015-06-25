package consumers;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;

import cern.jet.random.Binomial;
import firmState.Offer;
import firms.Firm;
import pHX_2.Market;
import pHX_2.RecessionsHandler;
import pHX_2.RunPriority;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public class Consumer {

	private double margUtilOfQuality;
	private double utilityDiscount;
	private Firm chosenFirm;
	private HashSet<Firm> exploredFirms;
	private ArrayList<Firm> knownFirmsNotExplored;
	private Binomial explorationDistrib;

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

		setUtilityDiscount();

		setExplorationDistrib();

	}

	private void setMargUtilOfQuality() {

		// There is no need to introduce a marginal utility of money
		// because it is implicit in the marginal utility of quality
		margUtilOfQuality = Consumers.getMargUtilOfQualityDistrib()
				.nextDouble();

		// Upper limit is unbounded. The max marg utility is updated to scale
		// graphs
		Consumers.setMaxMargUtilOfQuality(Math.max(
				Consumers.getMaxMargUtilOfQuality(), margUtilOfQuality));

	}

	private void setUtilityDiscount() {
		utilityDiscount = Consumers.getUtilityDicountDistrib().nextDouble();

	}

	private void setExplorationDistrib() {

		// Determines the probability of exploring when choosing a firm.
		// If consumer decides to explore it chooses randomly a firm from the
		// known ones but one never tried
		// Otherwise he chooses among the tried firms the one that maximizes
		// utility

		double p = (double) GetParameter("consumerExplorationProb");

		explorationDistrib = RandomHelper.createBinomial(1, p);

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

		if (knownFirmsNotExplored.isEmpty())
			chosenFirm = chooseMaximizingFirm();

		else if (exploredFirms.isEmpty())
			chosenFirm = exploreKnownFirms();

		else if (explorationDistrib.nextInt() == 1)
			chosenFirm = exploreKnownFirms();

		else
			chosenFirm = chooseMaximizingFirm();

		// Adjust Demand
		if (chosenFirm != null)
			chosenFirm.setDemand(chosenFirm.getDemand() + 1);

	}

	@ScheduledMethod(start = 1, priority = RunPriority.UPDATE_PROJECTIONS_PRIORITY, interval = 1)
	public void updateProjections() {
		Market.consumersProjection.update(this);
		Market.margUtilProjection.update(this);
		Market.consumptionProjection.update(this);
	}

	// Randomly returns a known firm that has not been explored
	// It is assumed knownFirmsNotExplored is NOT empty
	private Firm exploreKnownFirms() {

		int i = RandomHelper.nextIntFromTo(0, knownFirmsNotExplored.size() - 1);
		Firm f = knownFirmsNotExplored.get(i);

		addToExploredFirms(f);

		return f;

	}

	// Returns the known firm that maximizes utility
	// or expected utility depending if firm is explored or not
	private Firm chooseMaximizingFirm() {
		Firm maxKnown, maxExplored;

		maxExplored = getMaximUtilFromExploredFirm();
		maxKnown = getMaximExpectUtilFromKnownFirm();

		if (maxKnown != null && maxExplored != null)
			// Return the best of both
			if (utility(maxExplored) > expectedUtility(maxKnown))
				return maxExplored;
			else {
				addToExploredFirms(maxKnown);
				return maxKnown;
			}

		else if (maxExplored == null && maxKnown == null)
			return null;

		else if (maxKnown != null) {
			addToExploredFirms(maxKnown);
			return maxKnown;

		} else
			// maxExplored != null
			return maxExplored;
	}

	// Returns the explored firm that maximizes utility
	private Firm getMaximUtilFromExploredFirm() {

		double utility = 0;
		Firm maxUtilFirm = null;

		for (Firm f : exploredFirms) {

			double tmpUtil = utility(f);

			if (tmpUtil > utility) {
				maxUtilFirm = f;
				utility = tmpUtil;
			}
		}

		// Consumer doesn't consider any firm with negative utility
		if (utility > 0)
			return maxUtilFirm;
		else
			return null;

	}

	// Returns the known firm not explored that maximizes expected utility
	// Returns null if all expected utilities are negative
	private Firm getMaximExpectUtilFromKnownFirm() {

		double utility = 0;
		Firm maxExpectUtilFirm = null;

		for (Firm f : knownFirmsNotExplored) {

			double tmpUtil = expectedUtility(f);

			if (tmpUtil > utility) {
				maxExpectUtilFirm = f;
				utility = tmpUtil;
			}

		}

		// Consumer doesn't consider any firm with negative expected utilities
		if (utility > 0)
			return maxExpectUtilFirm;
		else
			return null;
	}

	private double expectedUtility(Firm f) {
		return utilityDiscount * utility(f);
	}

	private double utility(Firm f) {

		return utility(f.getCurrentOffer());

	}

	private double utility(Offer o) {
		return margUtilOfQuality * o.getQuality() - o.getPrice();
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

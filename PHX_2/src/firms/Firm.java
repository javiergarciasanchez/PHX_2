package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.awt.Color;
import java.util.ArrayList;

import consumers.Consumer;
import consumers.Consumers;
import firmHistory.FirmHistory;
import firmHistory.FirmState;
import firmHistory.Offer;
import pHX_2.Market;
import pHX_2.RunPriority;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.random.RandomHelper;

public abstract class Firm {

	private FirmHistory history = new FirmHistory(FirmHistory.HISTORY_SIZE);

	protected double minPoorestConsumerMargUtil = Consumers
			.getMinMargUtilOfQuality();
	protected double maxPoorestConsumerMargUtil = Consumers
			.getMaxMargUtilOfQuality();

	private double accumProfit = 0;
	private double autoRegressiveProfit = 0;
	private double fixedCost;
	private double born;
	private ArrayList<Consumer> notYetKnownBy, alreadyKnownBy;

	private Firm loLimitFirm, hiLimitFirm;

	// Cost Scale is the same for all firms. It could be changed easily
	private static double costScale;
	private static double currentProfitWeight;

	protected static long firmIDCounter;

	private long firmIntID = firmIDCounter++;
	private String ID = "Firm " + firmIntID;

	public static void resetStaticVars() {
		// resets static variables
		costScale = (Double) GetParameter("costScale");
		currentProfitWeight = (Double) GetParameter("currentProfitWeight");
		firmIDCounter = 1;
	}

	public Firm() {

		Market.firms.add(this);

		alreadyKnownBy = new ArrayList<Consumer>();
		notYetKnownBy = new ArrayList<Consumer>();

		fixedCost = (Double) Market.firms.getFixedCostDistrib().nextDouble();

		born = RepastEssentials.GetTickCount();

		// Expand max price if cost at highest quality is higher than max price
		double maxUnitCost = unitCost(Offer.getMaxQuality());
		if (maxUnitCost > Offer.getMaxPrice()) {
			Offer.setMaxPrice(maxUnitCost
					+ Market.firms.getPriceStepDistrib().nextDouble());
		}

		makeInitialOffer();

	}

	private void makeInitialOffer() {

		Offer offer = getInitialOffer();

		history.addCurrentState(new FirmState(offer));

		Market.firms.addToFirmsByQ(this);

		initializeConsumerKnowledge();

	}

	private Offer getInitialOffer() {
		double p;

		double q = getInitialQuality();
		q = Offer.getAvailableQ(q);

		p = getInitialPrice(q);

		return new Offer(q, p);

	}

	protected double getInitialQuality() {
		double lowerQ = Offer.getMinQuality();
		double higherQ = Offer.getMaxInitialQuality();
		return Utils.getRandomInitialQuality(lowerQ, higherQ);
	}

	protected abstract double getInitialPrice(double q);

	@ScheduledMethod(start = 1, priority = RunPriority.MAKE_OFFER_PRIORITY, interval = 1)
	public void makeOffer() {

		setNewOffer();

		updateConsumerKnowledge();

	}

	/*
	 *  it should add the new offer to history
	 *  it should remove the firm from and add it back to FirmsByQ 
	 */
	protected abstract void setNewOffer();

	protected boolean isInTheMarket() {
		boolean isFirstInMarket = Market.firms.isFirstLimitingFirm(this);

		return (getLoLimitFirm() != null || getHiLimitFirm() != null || isFirstInMarket);
	}

	@ScheduledMethod(start = 1, priority = RunPriority.NEXT_STEP_FIRM_PRIORITY, interval = 1)
	public void nextStep() {
		// This is run after all offers are made and consumers have chosen
		// Calculates profit, accumProfit and kills the firm if necessary
		// History was kept when Current State was established

		// Calculate profits of period
		setProfit(profit());

		accumProfit += getProfit();

		if (born == RepastEssentials.GetTickCount())
			// Entry moment
			autoRegressiveProfit = getProfit();
		else
			autoRegressiveProfit = currentProfitWeight * getProfit()
					+ (1 - currentProfitWeight) * autoRegressiveProfit;

		if (isToBeKilled())
			Market.toBeKilled.add(this);
		else
			// Updates Projections of results
			updateProjections();

	}

	@ScheduledMethod(start = 1, priority = RunPriority.UPDATE_PROJECTIONS_PRIORITY, interval = 1)
	public void updateProjections() {
		Market.firms2DProjection.update(this);
		Market.firmsDemandProjection.update(this);
		Market.firmsProfitProjection.update(this);
		Market.margUtilProjection.update(this);
	}

	public abstract Color getColor();

	private void initializeConsumerKnowledge() {

		if ((boolean) GetParameter("allExplored")
				&& (boolean) GetParameter("allKnown"))
			// Firms are known and immediately become explored
			for (Consumer c : Market.consumers.getObjects(Consumer.class)) {
				c.addToExploredFirms(this);
				alreadyKnownBy.add(c);
			}

		else if ((boolean) GetParameter("allKnown")) {
			// Firms are known but not explored
			for (Consumer c : Market.consumers.getObjects(Consumer.class)) {
				c.addToKnownFirms(this);
				alreadyKnownBy.add(c);
			}

		} else if ((boolean) GetParameter("allExplored")) {
			// Firms should be known but are automatically explored

			// Take out of the list the initial "knower's" and set them as
			// explored
			getFromIgnoranceAndExplore((int) Math
					.round((Double) GetParameter("initiallyKnownByPerc")
							* Market.consumers.size()));
		} else {
			// Firms should be known and then explored
			for (Consumer c : Market.consumers.getObjects(Consumer.class))
				notYetKnownBy.add(c);

			// Take out of the list the initial "knower's"
			getFromIgnorance((int) Math
					.round((Double) GetParameter("initiallyKnownByPerc")
							* Market.consumers.size()));

		}
	}

	private void getFromIgnorance(int amount) {
		Consumer c;

		for (int k = 0; (k < amount) && !notYetKnownBy.isEmpty(); k++) {

			int i = RandomHelper.getUniform().nextIntFromTo(0,
					notYetKnownBy.size() - 1);

			c = notYetKnownBy.get(i);
			notYetKnownBy.remove(i);

			c.addToKnownFirms(this);
			alreadyKnownBy.add(c);
		}
	}

	private void getFromIgnoranceAndExplore(int amount) {
		Consumer c;

		for (int k = 0; (k < amount) && !notYetKnownBy.isEmpty(); k++) {

			int i = RandomHelper.getUniform().nextIntFromTo(0,
					notYetKnownBy.size() - 1);

			c = notYetKnownBy.get(i);
			notYetKnownBy.remove(i);

			alreadyKnownBy.add(c);

			c.addToExploredFirms(this);
		}

	}

	private void updateConsumerKnowledge() {
		Consumers consumers = Market.consumers;
		int mktSize = consumers.size();

		// if all Consumers know the firm then return
		if (notYetKnownBy.isEmpty())
			return;

		// Take some consumers out of "ignorance" and let them know this
		// firm
		// Knowledge is spread using the logistic growth equation (Bass
		// model with 100% imitators)

		double alreadyK = mktSize - notYetKnownBy.size();

		int knownByIncrement = (int) Math
				.round(Market.firms.diffusionSpeedParam * alreadyK
						* (1.0 - alreadyK / mktSize));

		knownByIncrement = Math.min(mktSize, knownByIncrement);
		knownByIncrement = Math.max(1, knownByIncrement);

		getFromIgnorance(knownByIncrement);

	}

	private double profit() {
		return (getPrice() - unitCost(getQuality())) * getDemand() - fixedCost;
	}

	public double unitCost(double quality) {
		// Cost grows with quality
		return costScale * quality;
	}

	protected double getMarginalCostOfQuality() {
		return costScale;
	}

	private void setProfit(double profit) {
		history.setCurrentProfit(profit);
	}

	private boolean isToBeKilled() {
		// Returns true if firm should exit the market
		return (autoRegressiveProfit < Market.firms.minimumProfit);
	}

	public FirmHistory getHistory() {
		return history;
	}

	public void killFirm() {
		Market.firms.removeFromFirmsByQ(this);

		// Remove firm from consumers lists
		for (Consumer c : alreadyKnownBy) {
			c.removeTraceOfFirm(this);
		}

		Market.firms.remove(this);

	}

	public void setDemand(int i) {
		history.setCurrentDemand(i);
	}

	/*
	 * Setters to probe DO NOT USE FOR CODE
	 */
	public void setPrice(double p) {
		Market.firms.removeFromFirmsByQ(this);

		Offer o = getCurrentOffer();
		o.setPrice(p);

		Market.firms.addToFirmsByQ(this);
	}

	public void setQuality(double q) {
		Market.firms.removeFromFirmsByQ(this);

		Offer o = getCurrentOffer();
		o.setQuality(q);

		Market.firms.addToFirmsByQ(this);
	}
	
	public double getHistoryVariation(){
		return history.getHistoryVariation();
	}

	//
	// Getters to probe
	//

	public double getMarginalProfitQ() {
		return Utils.getMarginalProfitOfQuality(this);
	}

	public double getMarginalProfitP() {
		return Utils.getMarginalProfitOfPrice(this);
	}

	public String getLoLimitFirmID() {
		Firm f = getLoLimitFirm();

		return (f == null ? null : f.getFirmID());
	}

	public double getLoLimitValue() {
		Firm f = getLoLimitFirm();

		return (f == null ? 0 : Utils.calcLimit(f, this));
	}

	public String getHiLimitFirmID() {
		Firm f = getHiLimitFirm();

		return (f == null ? null : f.getFirmID());
	}

	public double getHiLimitValue() {
		Firm f = getHiLimitFirm();

		return (f == null ? 0 : Utils.calcLimit(this, f));
	}

	public int getDemand() {
		return history.getCurrentDemand();
	}

	public Offer getCurrentOffer() {
		return history.getCurrentOffer();
	}

	public Firm getLoLimitFirm() {
		return loLimitFirm;
	}

	public void setLoLimitFirm(Firm loSegmentFirm) {
		this.loLimitFirm = loSegmentFirm;
	}

	public Firm getHiLimitFirm() {
		return hiLimitFirm;
	}

	public void setHiLimitFirm(Firm hiSegmentFirm) {
		this.hiLimitFirm = hiSegmentFirm;
	}

	public int getRed() {
		return getColor().getRed();
	}

	public int getBlue() {
		return getColor().getBlue();
	}

	public int getGreen() {
		return getColor().getGreen();
	}

	public double getProfit() {
		return history.getCurrentProfit();
	}

	public double getPrice() {
		return history.getCurrentPrice();
	}

	public double getQuality() {
		return history.getCurrentQuality();
	}

	public double getPoorestConsumerMargUtil() {
		return Utils.getPoorestConsumerMargUtil(getQuality(), getPrice());
	}

	public double getAge() {
		return RepastEssentials.GetTickCount() - born;
	}

	public String getFirmType() {
		return getClass().toString().substring(16, 17);
	}

	public String getFirmID() {
		return getFirmType() + " " + getFirmNumID();
	}

	public long getFirmIntID() {
		return firmIntID;
	}

	public String getFirmNumID() {
		return Long.toString(firmIntID);
	}

	public double getAccumProfit() {
		return accumProfit;
	}

	public double getAutoRegressiveProfit() {
		return autoRegressiveProfit;
	}

	public double getFixedCost() {
		return fixedCost;
	}

	public double getGrossMargin() {
		return (getPrice() - unitCost(getQuality())) / getPrice();
	}

	public double getMargin() {
		return getProfit() / (getDemand() * getPrice());
	}

	public String toString() {
		return ID;
	}

}

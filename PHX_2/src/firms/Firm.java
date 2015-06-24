package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.awt.Color;
import java.util.ArrayList;

import consumers.Consumer;
import consumers.Consumers;
import firmState.FirmState;
import firmState.Offer;
import firmTypes.NoPrice;
import pHX_2.Market;
import pHX_2.RunPriority;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public abstract class Firm {

	private FirmHistory history = new FirmHistory(FirmHistory.HISTORY_SIZE);

	protected double minPoorestConsumerMargUtil = Consumers
			.getMinMargUtilOfQuality();
	protected double maxPoorestConsumerMargUtil = Consumers
			.getMaxMargUtilOfQuality();

	private double accumProfit = 0;
	private double fixedCost;
	private ArrayList<Consumer> notYetKnownBy, alreadyKnownBy;

	private Firm loLimitFirm, hiLimitFirm;

	// Cost Scale is the same for all firms. It could be changed easily
	private static double costScale;

	protected static long firmIDCounter;

	private long firmIntID = firmIDCounter++;
	private String ID = "Firm " + firmIntID;

	public static void resetStaticVars() {
		// resets static variables
		costScale = (Double) GetParameter("costScale");
		firmIDCounter = 1;
	}

	public Firm() {

		Market.firms.add(this);

		alreadyKnownBy = new ArrayList<Consumer>();
		notYetKnownBy = new ArrayList<Consumer>();

		fixedCost = (Double) Market.firms.getFixedCostDistrib().nextDouble();

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

		try {
			p = getInitialPrice(q);
		} catch (NoPrice e) {
			// Put any price because there is no meaningful price
			p = (Offer.getMaxPrice() + Offer.getMinPrice()) / 2.0;
		}

		return new Offer(q, p);

	}

	protected abstract double getInitialQuality();

	protected abstract double getInitialPrice(double q) throws NoPrice;

	@ScheduledMethod(start = 1, priority = RunPriority.MAKE_OFFER_PRIORITY, interval = 1)
	public void makeOffer() {

		Offer offer;

		boolean wasInTheMarket = isInTheMarket();

		Market.firms.removeFromFirmsByQ(this);
		
		if (wasInTheMarket)
			offer = getMaximizingOffer();
		else
			offer = getReenteringOffer();

		history.addCurrentState(new FirmState(offer));

		Market.firms.addToFirmsByQ(this);

		updateNotYetKnownBy();

	}

	private Offer getReenteringOffer() {
		// It is assumed it was removed from FirmsByQ

		// Delete history to start again but keep current offer
		Offer o = getCurrentOffer();
		history.clear();

		try {
			o.setPrice(getInitialPrice(o.getQuality()));
		} catch (NoPrice e) {
			// There is no available price to compete
			// Keep current Offer
		}

		return o;

	}

	private boolean isInTheMarket() {
		boolean isFirstInMarket = Market.firms.isFirstLimitingFirm(this);

		return (getLoLimitFirm() != null || getHiLimitFirm() != null || isFirstInMarket);
	}

	private Offer getMaximizingOffer() {
		// It is assumed it is in the market

		double qStep = history.getCurrentQualityStep();
		double pStep = history.getCurrentPriceStep();

		double nextQStep = (qStep == 0 ? Offer.getDefaultQualityStep() : Math
				.abs(qStep));
		double nextPStep = (pStep == 0 ? Offer.getDefaultPriceStep() : Math
				.abs(pStep));

		double margProfQ = Utils.getMarginalProfitOfQuality(this) * nextQStep;
		double margProfP = Utils.getMarginalProfitOfPrice(this) * nextPStep;

		Offer o = new Offer(getCurrentOffer());

		if (Math.abs(margProfP) > Math.abs(margProfQ)) {
			// Modify price
			if (margProfP > 0)
				o.modifyPrice(pStep, +1);
			else
				o.modifyPrice(pStep, -1);
		} else {
			// Modify quality
			if (margProfQ > 0)
				o.modifyQuality(qStep, +1);
			else
				o.modifyQuality(qStep, -1);
		}

		return o;

	}

	@ScheduledMethod(start = 1, priority = RunPriority.NEXT_STEP_FIRM_PRIORITY, interval = 1)
	public void nextStep() {
		// This is run after all offers are made and consumers have chosen
		// Calculates profit, accumProfit and kills the firm if necessary
		// History was kept when Current State was established

		// Calculate profits of period
		setProfit(profit());

		accumProfit += getProfit();

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

		if ((boolean) GetParameter("perfectKnowledge"))
			// All firms are known and immediately become explored
			for (Consumer c : Market.consumers.getObjects(Consumer.class))
				c.addToExploredFirms(this);

		else if ((boolean) GetParameter("allKnown")) {
			// All firms are known but not explored
			for (Consumer c : Market.consumers.getObjects(Consumer.class)) {
				c.addToKnownFirms(this);
				alreadyKnownBy.add(c);
			}

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

	private void updateNotYetKnownBy() {
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

	protected double unitCost(double quality) {
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
		return (accumProfit < Market.firms.minimumProfit);
	}

	public void killFirm() {
		Market.firms.removeFromFirmsByQ(this);

		// Remove firm from consumers lists
		for (Consumer c : alreadyKnownBy) {
			c.removeFromKnownFirms(this);
			c.removeFromExploredFirms(this);
		}

		Market.firms.remove(this);

	}

	public void setDemand(int i) {
		history.setCurrentDemand(i);
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

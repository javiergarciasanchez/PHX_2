package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.awt.Color;
import java.util.ArrayList;

import cern.jet.random.Binomial;
import consumers.Consumer;
import consumers.Consumers;
import firmState.FirmState;
import firmState.Offer;
import firmState.OfferType;
import pHX_2.Market;
import pHX_2.RunPriority;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public abstract class Firm {

	private FirmHistory history = new FirmHistory(FirmHistory.HISTORY_SIZE);

	protected double minPoorestConsumerMargUtil = Market.consumers
			.getMinMargUtilOfQuality(),
			maxPoorestConsumerMargUtil = Market.consumers
					.getMaxMargUtilOfQuality();

	private Binomial explorationDistrib;
	private double accumProfit = 0;
	private double fixedCost;
	private double expectedQuality = 0;
	private ArrayList<Consumer> notYetKnownBy, alreadyKnownBy;

	protected OfferType[] offerTypePreference = new OfferType[4];

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

	protected static double getRandomInitialQuality(double lowerQ,
			double higherQ) {
		// Uses default uniform distribution between lower and high quality

		double tmpQ;

		// Quality should be checked for existence
		// because two different firms cannot have the same quality
		do {

			tmpQ = RandomHelper.nextDoubleFromTo(lowerQ, higherQ);

		} while (Market.segments.containsQ(tmpQ));

		return tmpQ;
	}

	public Firm() {

		Market.firms.add(this);

		setExplorationDistrib();

		fillOfferTypePreference();
		
		alreadyKnownBy = new ArrayList<Consumer>();
		notYetKnownBy = new ArrayList<Consumer>();

		fixedCost = (Double) Market.firms.getFixedCostDistrib().nextDouble();

		// Expand max price if cost at highest quality is higher than max price
		double maxUnitCost = unitCost(Offer.getMaxQuality());
		if (maxUnitCost > Offer.getMaxPrice()) {
			Offer.setMaxPrice(maxUnitCost
					+ Market.firms.getPriceStepDistrib().nextDouble());
		}

	}

	protected abstract void fillOfferTypePreference();

	private void setExplorationDistrib() {

		double p = (double) GetParameter("firmExplorationProb");

		explorationDistrib = RandomHelper.createBinomial(1, p);

	}

	@ScheduledMethod(start = 1, priority = RunPriority.MAKE_OFFER_PRIORITY, interval = 1)
	public void makeOffer() {
		Offer offer;

		if (history.size() == 0) {
			// ENTRY

			offer = getInitialOffer();

			initializeConsumerKnowledge();

		} else {

			Market.segments.removeFromSegments(this);

			if (explorationDistrib.nextInt() == 1) {
				offer = getExploratoryOffer();
			} else {
				offer = getMaximizingOffer();
			}

			updateNotYetKnownBy();

		}

		history.addCurrentState(new FirmState(offer));

		Market.segments.addToSegments(this);

	}

	private Offer getExploratoryOffer() {
		Offer o;
		
		for (OfferType oType : offerTypePreference) {
			o = new Offer(oType, history.getMaxProfitOffer());
			
			if (!history.containsOffer(o))
				return o;
		}

		return null;
	}

	private Offer getMaximizingOffer() {
		return history.getMaxProfitOffer();
	}

	@ScheduledMethod(start = 1, priority = RunPriority.NEXT_STEP_FIRM_PRIORITY, interval = 1)
	public void nextStep() {
		// This is run after all offers are made and consumers have chosen
		// Calculates profit, accumProfit and kills the firm if necessary
		// History was kept when Current State was established

		// Updates expectations
		updateExpectedQuality();

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

	protected abstract Offer getInitialOffer();

	private void initializeConsumerKnowledge() {

		if ((boolean) GetParameter("perfectKnowledge"))
			// All firms immediately become explored
			for (Consumer c : Market.consumers.getObjects(Consumer.class))
				c.addToExploredFirms(this);

		else {
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

	private OfferType getOfferType() {
		return history.getCurrentOfferType();
	}

	private void updateExpectedQuality() {

		double expInertia;
		double consumPerc;
		double currExpQ;

		consumPerc = getDemand() / Consumers.getMaxConsumers();
		currExpQ = consumPerc * getQuality() + (1 - consumPerc)
				* Market.getExpectedQPerDollar() * getPrice();

		expInertia = (Double) GetParameter("expectationsInertia");
		expectedQuality = expInertia * expectedQuality + (1 - expInertia)
				* currExpQ;

	}

	private void setProfit(double profit) {
		history.setCurrentProfit(profit);
	}

	private boolean isToBeKilled() {
		// Returns true if firm should exit the market
		return (accumProfit < Market.firms.minimumProfit);
	}

	public double getExpectedQuality() {
		// Only consumers who know the firm could access
		// to the expected quality
		return expectedQuality;
	}

	public void setDemand(int i) {
		history.setCurrentDemand(i);
	}

	public void killFirm() {
		Market.segments.removeFromSegments(this);

		// Remove firm from consumers lists
		for (Consumer c : alreadyKnownBy) {
			c.removeFromKnownFirms(this);
			c.removeFromExploredFirms(this);
		}

		Market.firms.remove(this);

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

	//
	// Getters to probe
	//

	public int getDemand() {
		return history.getCurrentDemand();
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
		return Firms.getPoorestConsumerMargUtil(getQuality(), getPrice());
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

	public String getOfferTypeStr() {
		if (getOfferType() == null)
			return "NULL";
		else
			return getOfferType().toString();
	}

	public String toString() {
		return ID;
	}

}

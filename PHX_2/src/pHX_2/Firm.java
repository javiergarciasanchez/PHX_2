package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.util.ArrayList;
import offer.Offer;
import offer.OfferType;
import demand.Consumer;
import demand.Consumers;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public abstract class Firm {

	private FirmHistory history = new FirmHistory(FirmHistory.HISTORY_SIZE);

	private double accumProfit = 0;
	private double fixedCost;
	private ArrayList<Consumer> notYetKnownBy, alreadyKnownBy;

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

		fixedCost = (Double) Market.firms.getFixedCostDistrib().nextDouble();

		// Expand max price if cost at highest quality is higher than max price
		double maxUnitCost = unitCost(Offer.getMaxQuality());
		if (maxUnitCost > Offer.getMaxPrice()) {
			Offer.setMaxPrice(maxUnitCost
					+ Market.firms.getPriceStepDistrib().nextDouble());
		}

	}

	@ScheduledMethod(start = 1, priority = RunPriority.MAKE_OFFER_PRIORITY, interval = 1)
	public void makeOffer() {
		Offer offer;

		if (history.size() == 0) {
			// ENTRY

			// Set quality according to firm's strategy
			double q = getRandomInitialQuality();
			offer = new Offer(q, getRandomInitialPrice(q));

			initializeNotYetKnownBy();

		} else {
			double q = getQuality();
			double p = getPrice();

			Market.firms.removeQ(getQuality());

			if (history.size() == 1) {

				// not enough history
				offer = new Offer(OfferType.getRandomOfferType(), q, p);

			} else {

				// Enough history to check improvement
				FirmState prevState = history.getPreviousState();

				if (getProfit() > prevState.getProfit()) {
					// Previous offer worked, so firm goes in the same direction
					offer = new Offer(prevState.getOfferType(), q, p);

				} else {
					// Previous offer didn't work. A new type of offer is
					// selected
					OfferType newOfferType;

					do {
						newOfferType = OfferType.getRandomOfferType();

					} while (newOfferType == prevState.getOfferType());

					offer = new Offer(newOfferType, q, p);
				}

			}

			updateNotYetKnownBy();
		}

		history.addCurrentState(new FirmState(offer));

		Market.firms.putQ(getQuality(), this);

		Market.firmsProyection.moveTo(this, offer.getX(), offer.getY());

	}

	protected abstract double getRandomInitialQuality();

	protected double getRandomInitialQuality(double lowerQ, double higherQ) {
		// Uses default uniform distribution between lower and high quality

		double tmpQ;

		// Quality should be checked for existence
		// because two different firms cannot have the same quality
		do {

			tmpQ = RandomHelper.nextDouble() * (higherQ - lowerQ) + lowerQ;

		} while (Market.firms.containsQ(tmpQ));

		return tmpQ;
	}

	protected abstract double getRandomInitialPrice(double q);

	private void initializeNotYetKnownBy() {
		notYetKnownBy = new ArrayList<Consumer>();

		for (Consumer c : Market.consumers.getObjects(Consumer.class))
			notYetKnownBy.add(c);

		// Take out of the list the initial "knower's"
		getFromIgnorance((int) Math
				.round((Double) GetParameter("initiallyKnownByPerc")
						* Market.consumers.size()));

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

	}

	private double profit() {
		return (getPrice() - unitCost(getQuality())) * getDemand() - fixedCost;
	}

	private void getFromIgnorance(int amount) {

		for (int k = 0; (k < amount) && !notYetKnownBy.isEmpty(); k++) {

			int i = RandomHelper.getUniform().nextIntFromTo(0,
					notYetKnownBy.size() - 1);

			notYetKnownBy.get(i).addToKnownFirms(this);
			notYetKnownBy.remove(i);
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

	protected double unitCost(double quality) {
		// Cost grows with quality
		return costScale * quality;
	}

	private boolean isToBeKilled() {
		// Returns true if firm should exit the market
		return (accumProfit < Market.firms.minimumProfit);
	}

	private OfferType getOfferType() {
		return history.getCurrentState().getOfferType();
	}

	private void setProfit(double profit) {
		history.getCurrentState().setProfit(profit);
	}

	public void setDemand(int i) {
		history.getCurrentState().setDemand(i);
	}

	public void killFirm() {
		// quality identifies firms, because we don't let two firms have the
		// same quality
		Market.firms.removeQ(getQuality());

		// Remove firm from consumers lists
		for (Consumer c : alreadyKnownBy) {
			c.removeFromKnownFirms(this);
			c.removeFromExploredFirms(this);
		}

		Market.firms.remove(this);

	}

	public void addToAlreadyKnownBy(Consumer c) {
		alreadyKnownBy.add(c);
	}

	public int getDemand() {
		return history.getCurrentState().getDemand();
	}


	//
	// Getters to probe
	//
	public double getPoorestConsumer() {
		return getPrice() / getQuality();
	}

	public double getProfit() {
		return history.getCurrentState().getProfit();
	}

	public double getPrice() {
		return history.getCurrentState().getPrice();
	}

	public double getQuality() {
		return history.getCurrentState().getQuality();
	}

	public String getFirmType() {
		return getClass().toString().substring(12, 13);
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

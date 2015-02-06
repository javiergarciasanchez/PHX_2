package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.util.ArrayList;
import java.util.Iterator;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public abstract class Firm {

	private FirmHistory history = new FirmHistory(FirmHistory.HISTORY_SIZE);

	private double accumProfit = 0;
	private double fixedCost;
	private ArrayList<Consumer> notYetKnownBy;

	protected static long firmIDCounter = 1;

	public static void setFirmIDCounter(long firmIDCounter) {
		Firm.firmIDCounter = firmIDCounter;
	}

	private long firmIntID = firmIDCounter++;
	private String ID = "Firm " + firmIntID;

	public Firm() {

		CreateMarket.firmsContext.add(this);

		fixedCost = (Double) Firms.getFixedCostDistrib().nextDouble();

	}

	@ScheduledMethod(start = 1, priority = RunPriority.MAKE_OFFER_PRIORITY, interval = 1)
	public void makeOffer() {
		Offer offer;

		if (history.size() == 0) {
			// ENTRY

			// Entry is not aggressive against competitors
			offer = new Offer();

			// Set quality according to firm's strategy
			double q;
			q = getRandomInitialQuality();
			offer.setQuality(q);
			Firms.putQ(q, this);

			offer.setPrice(getRandomInitialPrice(q));
			offer.setOfferType(null);

			initializeNotYetKnownBy();

		} else {

			Firms.removeQ(getQuality());

			if (history.size() == 1) {

				// not enough history
				offer = offerNewOfferType();

			} else {

				// Enough history to check improvement
				FirmState prevState = getPreviousState();
				if (offerTypeHasWorked(prevState)) {
					// Offer type has worked
					offer = offerKeepingOfferType(getOfferType());
				} else {
					// Offer type has NOT worked
					offer = offerChangingOfferType(getOfferType());
				}

			}

			updateNotYetKnownBy();
		}

		setCurrentState(new FirmState(offer));

		Firms.putQ(getQuality(), this);

		CreateMarket.firmsProyection.moveTo(this, offer.getX(), offer.getY());

	}

	protected abstract double getRandomInitialQuality();

	protected abstract double getRandomInitialPrice(double q);

	private FirmState getCurrentState() {
		return history.getCurrentState();
	}

	private void setCurrentState(FirmState firmState) {
		history.add(firmState);
	}

	private void initializeNotYetKnownBy() {
		notYetKnownBy = new ArrayList<Consumer>();

		Iterator<Consumer> it = CreateMarket.consumersContext.getObjects(
				Consumer.class).iterator();

		// Put all the consumers in the list
		while (it.hasNext())
			notYetKnownBy.add(it.next());

		// Take out of the list the initial "knower's"
		getFromIgnorance((int) Math
				.round((Double) GetParameter("initiallyKnownByPerc")
						* CreateMarket.consumersContext.size()));

	}

	@ScheduledMethod(start = 1, priority = RunPriority.NEXT_STEP_FIRM_PRIORITY, interval = 1)
	public void nextStep() {
		// This is run after all offers are made and consumers have chosen
		// Calculates profit, accumProfit and kills the firm if necessary
		// History was kept when Current State was established

		// Calculate profits of period
		setProfit((getPrice() - unitCost(getQuality())) * getDemand()
				- fixedCost);

		accumProfit += getProfit();

		if (isToBeKilled())
			CreateMarket.toBeKilled.add(this);

	}

	private boolean offerTypeHasWorked(FirmState prevState) {

		if (getProfit() > prevState.getProfit())
			return true;
		else
			return false;

	}

	private Offer offerNewOfferType() {
		return makeOfferOfType(OfferType.getRandomOfferType());
	}

	private Offer offerKeepingOfferType(OfferType offerType) {
		return makeOfferOfType(offerType);
	}

	private Offer offerChangingOfferType(OfferType prevOfferType) {
		OfferType newOfferType = OfferType.getRandomOfferType();

		while (newOfferType == prevOfferType) {
			newOfferType = OfferType.getRandomOfferType();
		}

		return makeOfferOfType(newOfferType);

	}

	protected Offer makeOfferOfType(OfferType offerType) {

		Offer offer = new Offer();

		offer.setOfferType(offerType);

		switch (offerType) {
		case INCREASE_PRICE:
			offer.setPrice(getPrice()
					+ Firms.getPriceStepDistrib().nextDouble());
			offer.setQuality(getQuality());

			break;
		case DECREASE_PRICE:
			offer.setPrice(getPrice()
					- Firms.getPriceStepDistrib().nextDouble());
			offer.setQuality(getQuality());
			break;
		case INCREASE_QUALITY:
			offer.setQuality(getQuality()
					+ Firms.getQualityStepDistrib().nextDouble());
			offer.setPrice(getPrice());
			break;
		case DECREASE_QUALITY:
			offer.setQuality(getQuality()
					- Firms.getQualityStepDistrib().nextDouble());
			offer.setPrice(getPrice());
			break;
		default:
			break;

		}

		return offer;
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
		Consumers consumers = CreateMarket.consumersContext;
		int mktSize = consumers.size();

		// if all Consumers know the firm then return
		if (notYetKnownBy.isEmpty())
			return;

		// Take some consumers out of "ignorance" and let them know this
		// firm
		// Knowledge is spread using the logistic growth equation (Bass
		// model with 100% imitators)

		double alreadyK = mktSize - notYetKnownBy.size();

		int knownByIncrement = (int) Math.round(Firms.diffusionSpeedParam
				* alreadyK * (1.0 - alreadyK / mktSize));

		knownByIncrement = Math.min(mktSize, knownByIncrement);
		knownByIncrement = Math.max(1, knownByIncrement);

		getFromIgnorance(knownByIncrement);

	}

	protected double unitCost(double quality) {
		// Cost grows with quality
		return Firms.costScale * quality;
	}

	private boolean isToBeKilled() {
		// Returns true if firm should exit the market
		return (accumProfit < Firms.minimumProfit);
	}

	private OfferType getOfferType() {
		return getCurrentState().getOfferType();
	}

	private void setProfit(double profit) {
		getCurrentState().setProfit(profit);
	}

	private FirmState getPreviousState() {
		return history.getLastState();
	}

	public void killFirm() {
		// quality identifies firms, because we don't let two firms have the
		// same quality
		Firms.removeQ(getQuality());
		CreateMarket.firmsContext.remove(this);
	}

	public int getDemand() {
		return getCurrentState().getDemand();
	}

	public void setDemand(int i) {
		getCurrentState().setDemand(i);
	}

	/*
	 * If it is the lowest quality firm in the market, it returns a null firm
	 * and P/Q as low limit
	 */
	public FirmLimit getLowerCompetitor() {

		// absolute limit
		double absLimit = getPrice() / getQuality();

		Firm prevFirm = Firms.getPrevQFirm(this);

		if (prevFirm == null)
			return new FirmLimit(null, absLimit);

		double limit = (getPrice() - prevFirm.getPrice())
				/ (getQuality() - prevFirm.getQuality());

		Firm antePrevFirm = prevFirm.getLowerCompetitor().f;

		if (antePrevFirm == null)
			// Prev is the lowest quality firm in the market
			return new FirmLimit(prevFirm, Math.max(absLimit, limit));

		else {

			double prevLimit = (prevFirm.getPrice() - antePrevFirm.getPrice())
					/ (prevFirm.getQuality() - antePrevFirm.getQuality());

			if (prevLimit < limit)
				// prev is in the market
				return new FirmLimit(prevFirm, Math.max(absLimit, limit));

			else {
				// Prev is not in the market. Recalculate limit with antePrev
				limit = (getPrice() - antePrevFirm.getPrice())
						/ (getQuality() - antePrevFirm.getQuality());

				return new FirmLimit(antePrevFirm, Math.max(absLimit, limit));

			}

		}

	}

	/*
	 * 
	 * Returns null if it has no place in the market It return a FirmLimit with
	 * firm = 0 in case is the highest quality firm in the market
	 */
	public FirmLimit getHigherCompetitor(double lowerLimit) {

		Firm nextFirm = Firms.getNextQFirm(this);
		if (nextFirm == null)
			return new FirmLimit(null, 0.0);

		double higherLimit = (nextFirm.getPrice() - getPrice())
				/ (nextFirm.getQuality() - getQuality());

		if (lowerLimit < higherLimit) {
			// it has a place in the market
			// Search the neighbor
			FirmLimit postNextFirmLimit = nextFirm
					.getHigherCompetitor(higherLimit);
			if (postNextFirmLimit != null)
				// next firm has a place in the market
				return new FirmLimit(nextFirm, higherLimit);
			else
				// next firm does not have a place in the market
				return postNextFirmLimit;

		} else
			// it does not have a place in the market
			return null;

	}

	//
	// Getters to probe
	//

	public double getMarketLowerLimit() {
		return Firms.marketLowerLimits.getLimitOfFirm(this);
	}

	public double getPoorestConsumer() {
		return getPrice() / getQuality();
	}

	public double getProfit() {
		return getCurrentState().getProfit();
	}

	public double getPrice() {
		return getCurrentState().getPrice();
	}

	public double getQuality() {
		return getCurrentState().getQuality();
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

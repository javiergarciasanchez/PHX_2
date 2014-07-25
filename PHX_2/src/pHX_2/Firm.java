package pHX_2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public abstract class Firm {

	private FirmHistory history = new FirmHistory(FirmHistory.HISTORY_SIZE);
	private QualityOfferType qualityOfferType;
	private PriceOfferType priceOfferType;

	private double accumProfit = 0;
	private double fixedCost;
	private ArrayList<Consumer> notYetKnownBy = new ArrayList<Consumer>();

	protected static long firmIDCounter = 1;

	public static void setFirmIDCounter(long firmIDCounter) {
		Firm.firmIDCounter = firmIDCounter;
	}

	protected long firmIntID = firmIDCounter++;
	protected String ID = "Firm " + firmIntID;

	public Firm(QualityOfferType qualityOfferType, PriceOfferType priceOfferType) {
		this.qualityOfferType = qualityOfferType;
		this.priceOfferType = priceOfferType;

		CreateMarket.firmsContext.add(this);

		fixedCost = (Double) Firms.getFixedCostDistrib().nextDouble();

		makeInitialOffer();

	}

	private void setInitialKnownBy() {
		Consumers consumers = CreateMarket.consumersContext;

		long notKnownByNum = Math.round(consumers.size()
				* (1.0 - Firms.initiallyKnownByPerc));

		notYetKnownBy.addAll((Collection<? extends Consumer>) consumers
				.getRandomObjects(Consumer.class, notKnownByNum));
	}

	protected void makeInitialOffer() {
		Offer offer = new Offer();

		// Set quality
		offer.setQuality(getRandomQuality());
		Firms.sortQFirms.put(offer.getQuality(), this);

		// Set price
		offer.setPrice(Math.max(getMinimumPrice(offer.getQuality()),
				getRandomInitialPrice()));

		// Check if offer has a profit above 0 otherwise try to adapt
		// If adaptation was unsuccessful kill firm
		if (getEstimatedProfit(offer) > 0.0) {

			// Set location in projection
			CreateMarket.firmsProyection.moveTo(this, offer.getX(),
					offer.getY());

			setCurrentState(new FirmState(offer));

			// Establish which consumers know the firm
			setInitialKnownBy();
		} else if (!adjustOffer(offer, 0.0)) {
			killFirm();
		}
	}

	private double getEstimatedProfit(Offer offer) {
		return (offer.getPrice() - cost(offer.getQuality()))
				* getEstimatedDemand(offer) - fixedCost;
	}

	private double getMinimumPrice(double quality) {
		// Minimum price for a positive margin
		return cost(quality);
	}

	private double getMinimumDemand(Offer offer) {
		// Minimum quantity sold to afford fixed costs given price and quality
		return fixedCost / (offer.getPrice() - cost(offer.getQuality()));
	}

	private double getEstimatedDemand(Offer offer) {
		// TODO Auto-generated method stub

		return 0;
	}

	private boolean adjustOffer(Offer offer, double minProfitExpected) {

		Iterator<Offer> trailOffers = getTrailOffers(offer);

		while ((getEstimatedProfit(offer) <= minProfitExpected)
				&& (trailOffers.hasNext())) {

			offer = trailOffers.next();
		}

		if (getEstimatedProfit(offer) > minProfitExpected)
			return true;
		else
			return false;
	}

	private Iterator<Offer> getTrailOffers(Offer offer) {
		// TODO Auto-generated method stub
		return null;
	}

	@ScheduledMethod(start = 1, priority = RunPriority.NEXT_STEP_FIRM_PRIORITY, interval = 1)
	public void nextStep() {
		// Calculates profit, accumProfit and kills the firm if necessary
		// History was kept when Current State was established

		// Calculate profits of period
		setProfit((getPrice() - cost(getQuality())) * getDemand() - fixedCost);

		accumProfit += getProfit();

		if (isToBeKilled())
			CreateMarket.toBeKilled.add(this);

	}

	@ScheduledMethod(start = 1, priority = RunPriority.MAKE_OFFER_PRIORITY, interval = 1)
	public void makeOffer() {

		Offer offer = new Offer();

		// Quality is constant, set at moment of entry
		offer.setQuality(getQuality());

		// Set price according to two scenarios: Deepen our strategy or go back
		// to previous price
		int strategy;

		switch (priceOfferType) {
		case HIGH_PRICE:
			strategy = 1;
			break;
		case LOW_PRICE:
			strategy = -1;
			break;
		default:
			strategy = 0;

		}

		// If there is not enough history to adapt (at least two periods)
		// or profit increased or remained constant then deepen our strategy
		// There is at least one offer, because initialOffer was called before
		if ((getLastOffer() == null)
				|| (getProfit() >= getLastOffer().getProfit())) {

			offer.setPrice(getPrice() + strategy
					* Firms.getPriceStepDistrib().nextDouble());

		} else {

			// Profit decreased so let's return to previous price
			offer.setPrice(getLastOffer().getPrice());
		}

		setCurrentState(new FirmState(offer));

		CreateMarket.firmsProyection.moveTo(this, offer.getX(), offer.getY());

		updateNotYetKnownBy();

	}

	protected double getRandomQuality() {
		double q;
		BetaSubj distrib;

		switch (qualityOfferType) {
		case HIGH_QUALITY:
			distrib = Firms.getHighQualityDistrib();
			break;
		case LOW_QUALITY:
			distrib = Firms.getLowQualityDistrib();
			break;
		default:
			distrib = null; // it should never come here
			break;

		}

		// Quality should be checked for existence
		do {
			q = distrib.nextDouble();
		} while (Firms.sortQFirms.containsKey(q));

		return q;
	}

	protected double getRandomInitialPrice() {

		switch (priceOfferType) {
		case HIGH_PRICE:
			return Firms.getHighInitialPriceDistrib().nextDouble();
		case LOW_PRICE:
			return Firms.getLowInitialPriceDistrib().nextDouble();
		default:
			return 0.0; // it should never come here
		}

	}

	private double getInitialPrice(double quality) {
		// P/Q should be between P/Q of competitors with higher and lower
		// quality
		// A reference price is estimated using competitors and then the price
		// strategy is applied
		Firm lowerComp = getLowerCompetitor(quality);
		Firm higherComp = getHigherCompetitor(quality);
		double referencePrice;

		if ((lowerComp != null) && (higherComp != null)) {

			referencePrice = quality
					* (lowerComp.getPrice() / lowerComp.getQuality() + higherComp
							.getPrice() / higherComp.getQuality()) / 2.0;

		} else if (lowerComp != null) {

			// Entrant with highest quality
			referencePrice = quality * lowerComp.getPrice()
					/ lowerComp.getQuality();

		} else if (higherComp != null) {

			// Entrant with lowest quality
			referencePrice = quality * higherComp.getPrice()
					/ higherComp.getQuality();

		} else {

			// First entrant.
			// Sets price and quality in the price range using the same relative
			// position of quality
			double relativeQ = (quality - Offer.getMinQuality())
					/ (Offer.getMaxQuality() - Offer.getMinQuality());
			referencePrice = relativeQ
					* (Offer.getMaxPrice() - Offer.getMinPrice())
					+ Offer.getMinPrice();
		}

		// Set the offer in the middle, but taking into account the price
		// strategy
		int strategy = 0;
		switch (priceOfferType) {
		case HIGH_PRICE:
			strategy = 1;
			break;
		case LOW_PRICE:
			strategy = -1;
			break;
		}

		// Apply price strategy
		return referencePrice + strategy
				* Firms.getPriceStepDistrib().nextDouble();
	}

	private void updateNotYetKnownBy() {

		// if all Consumers know the firm then return
		if (notYetKnownBy.isEmpty())
			return;

		// Take some consumers out of "ignorance" and let them know this firm
		// Knowledge is spread using standard epidemic diffusion process

		double mktSize = CreateMarket.consumersContext.size();
		double alreadyK = mktSize - notYetKnownBy.size();

		int knownByIncrement = (int) Math.round(Firms.diffusionSpeedParam
				* alreadyK * (1 - alreadyK / mktSize));

		knownByIncrement = (int) Math.min(mktSize, knownByIncrement);
		knownByIncrement = Math.max(1, knownByIncrement);

		for (int k = 0; (k < knownByIncrement) && !notYetKnownBy.isEmpty(); k++) {

			int i = RandomHelper.getUniform().nextIntFromTo(0,
					notYetKnownBy.size() - 1);

			notYetKnownBy.get(i).addToKnownFirms(this);
			notYetKnownBy.remove(i);

		}

	}

	private void setCurrentState(FirmState firmState) {
		history.add(firmState);
	}

	private Firm getLowerCompetitor(double quality) {
		Map.Entry<Double, Firm> lowComp;

		lowComp = Firms.sortQFirms.lowerEntry(quality);

		if (lowComp == null)
			return null;
		else
			return lowComp.getValue();
	}

	private Firm getHigherCompetitor(double quality) {
		Map.Entry<Double, Firm> highComp;

		highComp = Firms.sortQFirms.higherEntry(quality);

		if (highComp == null)
			return null;
		else
			return highComp.getValue();
	}

	public boolean isToBeKilled() {
		// Returns true if firm should exit the market
		return (accumProfit < Firms.minimumProfit);
	}

	public void killFirm() {
		// quality identifies firms, because we don't let two firms have the
		// same quality
		Firms.sortQFirms.remove(getQuality());
		CreateMarket.firmsContext.remove(this);
	}

	private double cost(double quality) {
		// Cost grows with quality
		return Firms.costScale * quality;
	}

	public int getDemand() {
		return getCurrentOffer().getDemand();
	}

	public void setDemand(int i) {
		getCurrentOffer().setDemand(i);
	}

	public FirmState getCurrentOffer() {
		return history.getCurrentOffer();
	}

	private FirmState getLastOffer() {
		return history.getLastOffer();
	}

	// Getters to probe
	public double getProfit() {
		return getCurrentOffer().getProfit();
	}

	public void setProfit(double profit) {
		getCurrentOffer().setProfit(profit);
	}

	public double getPrice() {
		return getCurrentOffer().getPrice();
	}

	public double getQuality() {
		return getCurrentOffer().getQuality();
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
		return (getPrice() - cost(getQuality())) / getPrice();
	}

	public double getMargin() {
		return getProfit() / (getDemand() * getPrice());
	}

	public String toString() {
		return ID;
	}

}

package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import repast.simphony.space.continuous.ContinuousSpace;

public abstract class Firm {

	public Market market;

	public FirmState firmState; // current state

	// History keeps the last two periods
	public FirmState[] history;

	private double fixedCost;

	protected static long firmIDCounter = 1;
	protected long firmIntID = firmIDCounter++;
	protected String ID = "Firm " + firmIntID;

	public Firm(Market market) {

		this.market = market;
		market.context.add(this);

		firmState = new FirmState(market);

		fixedCost = (Double) market.fixedCostDistrib.nextDouble();

		history = new FirmState[2];

		makeInitialOffer();

		// Set location in projection
		@SuppressWarnings("unchecked")
		ContinuousSpace<Object> p = market.context.getProjection(
				ContinuousSpace.class, "Firms");

		p.moveTo(this, firmState.productX(), firmState.productY());

	}

	public abstract void makeInitialOffer();

	public abstract void makeOffer();

	protected void offer(int stepDirection) {

		// Quality is constant, set at moment of entry
		firmState.product.setQuality(history[0].product.getQuality());

		// Set price according to two scenarios: Deepen our strategy or go back
		// to previous price

		// If there is not enough history to adapt (history[1] == null)
		// or profit increased or remained constant
		// then deepen our strategy
		if ((history[1] == null) || (history[0].profit >= history[1].profit)) {

			firmState.product
					.setPrice(history[0].product.getPrice()
							* (1 + stepDirection
									* market.priceStepDistrib.nextDouble()));

		} else {

			// Profit decreased so let's return to previous price
			firmState.product.setPrice(history[1].product.getPrice());
		}

		@SuppressWarnings("unchecked")
		ContinuousSpace<Object> p = market.context.getProjection(
				ContinuousSpace.class, "Firms");

		p.moveTo(this, firmState.productX(), firmState.productY());

	}

	// All type of firms use the same product reference
	protected Product getProductReference() {

		return market.referenceProductForFirms;

	}

	public void nextStep() {

		// Calculate profits of period
		firmState.profit = (firmState.product.getPrice() - cost(firmState.product
				.getQuality())) * firmState.demand - fixedCost;

		// Keep History
		// dereference the last record and move the rest forward
		for (int i = history.length - 1; i > 0; i--)
			history[i] = history[i - 1];

		history[0] = firmState;

		// Reset state
		firmState = new FirmState(market);

	}

	private double cost(double quality) {
		// Cost grows exponentially with quality
		return (double) GetParameter("costScale") * Math.pow(quality, 2.0);
	}

	public boolean isToBeKilled() {
		// Returns true if firm should exit the market
		return false;
	}

	protected double qualityStep() {
		// It is calculated as percentage of substitute product
		return market.qualityStepDistrib.nextDouble()
				* market.substitute.getQuality();
	}

	// Getters to probe
	public String getFirmID() {
		return ID;
	}

	public double getProfit() {
		return history[0].profit;
	}

	public int getDemand() {
		return history[0].demand;
	}

	public double getPrice() {
		return firmState.product.getPrice();
	}

	public double getQuality() {
		return firmState.product.getQuality();
	}

	public String firmType() {
		return this.getClass().toString();
	}

	public String toString() {
		return ID;
	}

	public long getFirmIntID() {
		return this.firmIntID;
	}

}

package pHX_2;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import cern.jet.random.Gamma;
import static repast.simphony.essentials.RepastEssentials.GetParameter;
import repast.simphony.context.DefaultContext;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.essentials.RepastEssentials;

public class Firms extends DefaultContext<Firm> {

	// Random distributions

	// Initial quality and price distributions for different firm's strategies
	private static BetaSubj lowQualityDistrib = null;
	private static BetaSubj highQualityDistrib = null;
	private static BetaSubj lowInitialPriceDistrib = null;
	private static BetaSubj highInitialPriceDistrib = null;

	private static Gamma qualityStepDistrib = null;
	private static Gamma priceStepDistrib = null;
	private static Gamma fixedCostDistrib = null;

	// Firms ordered according to quality
	private static TreeMap<Double, Firm> sortQFirms;
	public static MarketLowerLimits marketLowerLimits;

	// Parameters for Firms
	static double initiallyKnownByPerc, minimumProfit, costScale,
			diffusionSpeedParam;

	public Firms() {

		super("Firms_Context");

		sortQFirms = new TreeMap<Double, Firm>();
		marketLowerLimits = new MarketLowerLimits();

		// Read parameters for firms
		initiallyKnownByPerc = (Double) GetParameter("initiallyKnownByPerc");
		minimumProfit = (Double) GetParameter("minimumProfit");
		costScale = (Double) GetParameter("costScale");
		diffusionSpeedParam = (Double) GetParameter("diffusionSpeedParam");

	}

	public static BetaSubj getLowQualityDistrib() {
		if (lowQualityDistrib == null)
			lowQualityDistrib = new BetaSubj(
					(Double) GetParameter("lowQualityMostLikely"),
					(Double) GetParameter("lowQualityMean"));

		return lowQualityDistrib;
	}

	public static BetaSubj getHighQualityDistrib() {
		if (highQualityDistrib == null)
			highQualityDistrib = new BetaSubj(
					(Double) GetParameter("highQualityMostLikely"),
					(Double) GetParameter("highQualityMean"));

		return highQualityDistrib;
	}

	public static BetaSubj getLowInitialPriceDistrib() {
		if (lowInitialPriceDistrib == null)
			lowInitialPriceDistrib = new BetaSubj(
					(Double) GetParameter("lowInitialPriceMostLikely"),
					(Double) GetParameter("lowInitialPriceMean"));

		return lowInitialPriceDistrib;
	}

	public static BetaSubj getHighInitialPriceDistrib() {
		if (highInitialPriceDistrib == null)
			highInitialPriceDistrib = new BetaSubj(
					(Double) GetParameter("highInitialPriceMostLikely"),
					(Double) GetParameter("highInitialPriceMean"));

		return highInitialPriceDistrib;
	}

	public static Gamma getQualityStepDistrib() {

		// Create distributions for strategic % steps on price and quality
		// We use Gamma distribution because the domain is > 0
		if (qualityStepDistrib == null) {
			double mean = (Double) GetParameter("qualityStepMean");
			double stdDevPercent = (Double) GetParameter("qualityStepStdDevPerc");
			double alfa = (1 / Math.pow(stdDevPercent, 2));
			double lamda = alfa / mean;

			qualityStepDistrib = RandomHelper.createGamma(alfa, lamda);
		}

		return qualityStepDistrib;
	}

	public static Gamma getPriceStepDistrib() {

		// Create distributions for strategic % steps on price and quality
		// We use Gamma distribution because the domain is > 0
		if (priceStepDistrib == null) {
			double mean = (Double) GetParameter("priceStepMean");
			double stdDevPercent = (Double) GetParameter("priceStepStdDevPerc");
			double alfa = (1 / Math.pow(stdDevPercent, 2));
			double lamda = alfa / mean;

			priceStepDistrib = RandomHelper.createGamma(alfa, lamda);
		}

		return priceStepDistrib;
	}

	public static Gamma getFixedCostDistrib() {

		// Create distribution for firms fixed cost
		// We use Gamma distribution because the domain is > 0
		if (fixedCostDistrib == null) {
			double mean = (Double) GetParameter("fixedCostMean");
			double stdDevPercent = (Double) GetParameter("fixedCostStdDevPerc");
			double alfa = (1 / Math.pow(stdDevPercent, 2));
			double lamda = alfa / mean;

			fixedCostDistrib = RandomHelper.createGamma(alfa, lamda);
		}
		return fixedCostDistrib;
	}

	public static Firm lowestQFirm() {

		return sortQFirms.firstEntry().getValue();

	}

	
	public static Firm getNextQFirm(double quality) {
		Map.Entry<Double, Firm> highComp;

		highComp = sortQFirms.higherEntry(quality);

		if (highComp == null)
			return null;
		else
			return highComp.getValue();
	}

	public static Firm getNextQFirm(Firm f) {

		return getNextQFirm(f.getQuality());

	}

	public static Firm getPrevQFirm(double quality) {
		Map.Entry<Double, Firm> lowComp;

		lowComp = sortQFirms.lowerEntry(quality);

		if (lowComp == null)
			return null;
		else
			return lowComp.getValue();
	}

	public static Firm getPrevQFirm(Firm f) {

		return getPrevQFirm(f.getQuality());

	}

	public static boolean containsQ(double q) {

		return sortQFirms.containsKey(q);

	}

	public static void putQ(double q, Firm firm) {
		sortQFirms.put(q, firm);
	}

	public static void removeQ(double q) {
		sortQFirms.remove(q);
	}

	@ScheduledMethod(start = 1, priority = RunPriority.ADD_FIRMS_PRIORITY, interval = 1)
	public void addFirms() {

		if ((boolean) GetParameter("firmsEntryOnlyAtStart")
				&& (RepastEssentials.GetTickCount() != 1))
			return;

		for (int i = 1; i <= (Integer) GetParameter("potencialFirmsPerPeriod"); i++) {

			switch (FirmType.getRandomFirmType()) {
			case OPPORTUNISTIC:
				new OpportunisticFirm();
				break;
			case PREMIUM:
				new PremiumFirm();
				break;
			case BASE_PYRAMID:
				new BasePyramidFirm();
				break;
			case WAIT:
				new WaitFirm();
			}
		}

	}

	@ScheduledMethod(start = 1, priority = RunPriority.KILL_FIRMS_PRIORITY, interval = 1)
	public void wipeDeadFirms() {
		for (Firm f : CreateMarket.toBeKilled)
			f.killFirm();

		CreateMarket.toBeKilled.clear();

	}

	@ScheduledMethod(start = 1, priority = RunPriority.UPDATE_SORT_FIRMS_PER_MARKET, interval = 1)
	public void updateMarketLowerLimits() {
		marketLowerLimits.update();
	}

}

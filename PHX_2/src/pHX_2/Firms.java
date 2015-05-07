package pHX_2;

import java.util.Map;
import java.util.TreeMap;

import cern.jet.random.Gamma;
import firmTypes.BasePyramidFirm;
import firmTypes.FirmType;
import firmTypes.OpportunisticFirm;
import firmTypes.PremiumFirm;
import firmTypes.WaitFirm;
import static repast.simphony.essentials.RepastEssentials.GetParameter;
import repast.simphony.context.DefaultContext;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.essentials.RepastEssentials;

public class Firms extends DefaultContext<Firm> {

	// Random distributions
	private Gamma qualityStepDistrib;
	private Gamma priceStepDistrib;
	private Gamma fixedCostDistrib;

	// Firms ordered according to quality
	private TreeMap<Double, Firm> sortQFirms;

	// Parameters for Firms
	double initiallyKnownByPerc, minimumProfit, diffusionSpeedParam;


	public Firms() {
		super("Firms_Context");

		sortQFirms = new TreeMap<Double, Firm>();

		// Read parameters for all firms
		initiallyKnownByPerc = (Double) GetParameter("initiallyKnownByPerc");
		minimumProfit = (Double) GetParameter("minimumProfit");
		diffusionSpeedParam = (Double) GetParameter("diffusionSpeedParam");

		createProbabilityDistrib();
	}

	public  void createProbabilityDistrib() {
		double mean, stdDevPercent, alfa, lamda;
	
		// Quality Step
		// Create distributions for strategic % steps on price and quality
		// We use Gamma distribution because the domain is > 0
		mean = (Double) GetParameter("qualityStepMean");
		stdDevPercent = (Double) GetParameter("qualityStepStdDevPerc");
		alfa = (1 / Math.pow(stdDevPercent, 2));
		lamda = alfa / mean;
		qualityStepDistrib = RandomHelper.createGamma(alfa, lamda);
	
		// Price Step
		// Create distributions for strategic % steps on price and quality
		// We use Gamma distribution because the domain is > 0
		mean = (Double) GetParameter("priceStepMean");
		stdDevPercent = (Double) GetParameter("priceStepStdDevPerc");
		alfa = (1 / Math.pow(stdDevPercent, 2));
		lamda = alfa / mean;
		priceStepDistrib = RandomHelper.createGamma(alfa, lamda);
	
		// Fixed Cost
		// We use Gamma distribution because the domain is > 0
		mean = (Double) GetParameter("fixedCostMean");
		stdDevPercent = (Double) GetParameter("fixedCostStdDevPerc");
		alfa = (1 / Math.pow(stdDevPercent, 2));
		lamda = alfa / mean;
		fixedCostDistrib = RandomHelper.createGamma(alfa, lamda);
	
	}

	public  Gamma getQualityStepDistrib() {
		return qualityStepDistrib;
	}

	public  Gamma getPriceStepDistrib() {
		return priceStepDistrib;
	}

	public  Gamma getFixedCostDistrib() {
		return fixedCostDistrib;
	}

	public  Firm lowestQFirm() {
		return sortQFirms.firstEntry().getValue();
	}

	public Firm getNextQFirm(double quality) {
		Map.Entry<Double, Firm> higherComp;

		higherComp = sortQFirms.higherEntry(quality);

		if (higherComp == null)
			return null;
		else
			return higherComp.getValue();
	}

	public Firm getNextQFirm(Firm f) {
		return getNextQFirm(f.getQuality());
	}

	public Firm getPrevQFirm(double quality) {
		Map.Entry<Double, Firm> lowComp;

		lowComp = sortQFirms.lowerEntry(quality);

		if (lowComp == null)
			return null;
		else
			return lowComp.getValue();
	}

	public Firm getPrevQFirm(Firm f) {
		return getPrevQFirm(f.getQuality());
	}

	public boolean containsQ(double q) {

		return sortQFirms.containsKey(q);

	}

	public void putQ(double q, Firm firm) {
		sortQFirms.put(q, firm);
	}

	public void removeQ(double q) {
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
		for (Firm f : Market.toBeKilled)
			f.killFirm();

		Market.toBeKilled.clear();

	}

}

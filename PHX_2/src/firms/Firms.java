package firms;

import pHX_2.Market;
import pHX_2.RunPriority;
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

	// Parameters for Firms
	double initiallyKnownByPerc, minimumProfit, diffusionSpeedParam;

	public Firms() {
		super("Firms_Context");

		// Read parameters for all firms
		initiallyKnownByPerc = (Double) GetParameter("initiallyKnownByPerc");
		minimumProfit = (Double) GetParameter("minimumProfit");
		diffusionSpeedParam = (Double) GetParameter("diffusionSpeedParam");

		createProbabilityDistrib();
	}

	public static double getPoorestConsumerMargUtil(double quality, double price) {
		return price / quality;
	}

	public static double getPoorestConsumerMinPrice(double minMargUtil,
			double quality) {
		return minMargUtil * quality;
	}

	public void createProbabilityDistrib() {
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

	public Gamma getQualityStepDistrib() {
		return qualityStepDistrib;
	}

	public Gamma getPriceStepDistrib() {
		return priceStepDistrib;
	}

	public Gamma getFixedCostDistrib() {
		return fixedCostDistrib;
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

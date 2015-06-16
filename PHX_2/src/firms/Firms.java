package firms;

import java.util.Map;
import java.util.TreeMap;

import pHX_2.Market;
import pHX_2.RunPriority;
import cern.jet.random.Gamma;
import firmState.Offer;
import firmTypes.BasePyramidFirm;
import firmTypes.FirmType;
import firmTypes.OpportunisticFirm;
import firmTypes.PremiumFirm;
import firmTypes.WaitFirm;
import graphs.SegmentLimit;
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
	// Firms ordered according to quality
	private TreeMap<Double, Firm> sortQFirms;
	private Firm firstLimitingFirm;

	public Firms() {
		super("Firms_Context");

		// Read parameters for all firms
		initiallyKnownByPerc = (Double) GetParameter("initiallyKnownByPerc");
		minimumProfit = (Double) GetParameter("minimumProfit");
		diffusionSpeedParam = (Double) GetParameter("diffusionSpeedParam");

		createProbabilityDistrib();
		
		sortQFirms = new TreeMap<Double, Firm>();
	}
	
	@ScheduledMethod(start = 1, priority = RunPriority.CREATE_SEGMENT_LIMITS_PRIORITY, interval = 1)
	public void createSegmentsLimits() {

		Market.segments.clear();

		if (firstLimitingFirm == null)
			return;
		else {
			SegmentLimit sL = new SegmentLimit(null, firstLimitingFirm);
			addSegmentLimit(sL);
			Firm f = firstLimitingFirm;
			Firm nextF = f.getHiLimitFirm();
			while (nextF != null) {
				sL = new SegmentLimit(f, nextF);
				addSegmentLimit(sL);
				f = nextF;
				nextF = f.getHiLimitFirm();
			}

			sL = new SegmentLimit(f, null);
			addSegmentLimit(sL);

		}

	}
	public void updateLimitingFirms(Firm firm) {

		// First we need to remove previous links
		removeLimitingLinks(firm);

		// Get Tentative neighbors
		Firm prevF = null;
		Firm nextF = firstLimitingFirm;
		while (nextF != null && nextF.getQuality() < firm.getQuality()) {
			prevF = nextF;
			nextF = nextF.getHiLimitFirm();
		}

		Offer o = new Offer(firm.getQuality(), firm.getPrice());
		Firm lowerLimitFirm = getLowerLimitFirm(prevF, o, true);
		Firm higherLimitFirm = getHigherLimitFirm(nextF, o, true);

		Offer loOffer = (lowerLimitFirm == null ? null : lowerLimitFirm
				.getOffer());
		Offer hiOffer = (higherLimitFirm == null ? null : higherLimitFirm
				.getOffer());

		double loLimit = Utils.calcLimit(loOffer, o);
		double hiLimit = Utils.calcLimit(o, hiOffer);

		if (loLimit < hiLimit) {
			if (lowerLimitFirm != null)
				lowerLimitFirm.setHiLimitFirm(firm);
			else
				firstLimitingFirm = firm;

			firm.setLoLimitFirm(lowerLimitFirm);
			firm.setHiLimitFirm(higherLimitFirm);

			if (higherLimitFirm != null)
				higherLimitFirm.setLoLimitFirm(firm);
		}

	}

	private void addSegmentLimit(SegmentLimit sL) {
		Market.segments.add(sL);
		Market.margUtilProjection.add(sL);
	}

	private Firm getLowerLimitFirm(Firm prevFirm, Offer o, boolean clean) {

		if (prevFirm == null)
			return null;
		else {

			Offer prevOffer = prevFirm.getOffer();
			double limit = Utils.calcLimit(prevOffer, o);

			Firm prevPrevFirm = prevFirm.getLoLimitFirm();
			Offer prevPrevOffer = ((prevPrevFirm == null) ? null : prevPrevFirm
					.getOffer());

			double prevLimit = Utils.calcLimit(prevPrevOffer, prevOffer);

			if (prevLimit < limit)
				return prevFirm;
			else {
				if (clean)
					removeLimitingLinks(prevFirm);

				return getLowerLimitFirm(prevPrevFirm, o, clean);
			}
		}

	}

	private Firm getHigherLimitFirm(Firm nextFirm, Offer o, boolean clean) {

		if (nextFirm == null)
			return null;
		else {

			Offer nextOffer = nextFirm.getOffer();
			double limit = Utils.calcLimit(o, nextOffer);

			Firm nextNextFirm = nextFirm.getHiLimitFirm();
			Offer nextNextOffer = ((nextNextFirm == null) ? null : nextNextFirm
					.getOffer());

			double nextLimit = Utils.calcLimit(nextOffer, nextNextOffer);

			if (limit < nextLimit)
				return nextFirm;
			else {
				if (clean)
					removeLimitingLinks(nextFirm);

				return getHigherLimitFirm(nextNextFirm, o, clean);
			}
		}

	}

	private void removeLimitingLinks(Firm f) {
		if (sortQFirms.size() == 1)
			return;

		// connect links if it had
		Firm loF = f.getLoLimitFirm();
		Firm hiF = f.getHiLimitFirm();

		if (firstLimitingFirm == f && hiF != null) {
			firstLimitingFirm = hiF;
			hiF.setLoLimitFirm(null);

		} else if (firstLimitingFirm == f && hiF == null) {
			// f was the first and the last, it had all the market
			// we need to rebuild the whole chain
			rebuildLimitsExcluding(f);
			return;

		} else if (firstLimitingFirm != f) {

			if (loF != null)
				loF.setHiLimitFirm(hiF);
			
			if (hiF != null)
				hiF.setLoLimitFirm(loF);

		}

		f.setLoLimitFirm(null);
		f.setHiLimitFirm(null);
	}

	private void rebuildLimitsExcluding(Firm exclF) {
		firstLimitingFirm = null;
		exclF.setLoLimitFirm(null);
		exclF.setHiLimitFirm(null);

		for (double q : sortQFirms.navigableKeySet()) {
			Firm f = sortQFirms.get(q);
			if (f != exclF)
				updateLimitingFirms(f);
		}

	}

	public Firm lowestQFirm() {

		Map.Entry<Double, Firm> e = sortQFirms.firstEntry();
		return (e == null ? null : e.getValue());

	}

	public Firm getLowerLimitFirm(double q, boolean clean) {

		Firm prevF = null;
		Firm f = firstLimitingFirm;
		while (f != null && f.getQuality() < q) {
			prevF = f;
			f = f.getHiLimitFirm();
		}

		return prevF;

	}

	public Firm getHigherLimitFirm(double q, boolean clean) {

		Firm f = firstLimitingFirm;
		while (f != null && f.getQuality() < q) {
			f = f.getHiLimitFirm();
		}

		return f;

	}

	public boolean containsQ(double q) {
		return sortQFirms.containsKey(q);
	}

	public void initializeLimitingFirms(Firm f) {
		sortQFirms.put(f.getQuality(), f);
		updateLimitingFirms(f);
	}

	public void removeFromSegments(Firm f) {
		removeLimitingLinks(f);
		sortQFirms.remove(f.getQuality());
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
//				new OpportunisticFirm();
//				break;
			case PREMIUM:
				new PremiumFirm();
				break;
			case WAIT:
//				new WaitFirm();
//				break;
			case BASE_PYRAMID:
				new BasePyramidFirm();
				break;
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

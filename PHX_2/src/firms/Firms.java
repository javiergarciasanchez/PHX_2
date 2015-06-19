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

		// Handle special cases
		if (firstLimitingFirm == null) {
			// It is the first firm
			firm.setLoLimitFirm(null);
			firm.setHiLimitFirm(null);
			firstLimitingFirm = firm;
			return;
		} else if (firstAndLast(firm)) {
			// Nothing to update
			return;
		}

		// Remove previous links
		removeLimitingLinks(firm);

		// Get Tentative neighbors
		Firm prevF = null;
		Firm nextF = firstLimitingFirm;
		while (nextF != null && nextF.getQuality() < firm.getQuality()) {
			prevF = nextF;
			nextF = nextF.getHiLimitFirm();
		}

		Offer o = new Offer(firm.getOffer());
		Firm lowerLimitFirm = getLowerLimitFirm(prevF, o, firm);
		Firm higherLimitFirm = getHigherLimitFirm(nextF, o, firm);

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

	public boolean firstAndLast(Firm firm) {
		return firstLimitingFirm == firm && firm.getHiLimitFirm() == null;
	}

	private void addSegmentLimit(SegmentLimit sL) {
		Market.segments.add(sL);
		Market.margUtilProjection.add(sL);
	}

	private Firm getLowerLimitFirm(Firm prevFirm, Offer o, Firm firstCandidate) {

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
				if (firstCandidate != null && firstAndLast(prevFirm)) {
					prevFirm.setLoLimitFirm(null);
					firstLimitingFirm = firstCandidate;
					return null;
				} else if (firstCandidate != null && !firstAndLast(prevFirm))
					removeLimitingLinks(prevFirm);

				return getLowerLimitFirm(prevPrevFirm, o, firstCandidate);
			}
		}

	}

	private Firm getHigherLimitFirm(Firm nextFirm, Offer o, Firm firstCandidate) {

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
				if (firstCandidate != null && firstAndLast(nextFirm)) {
					nextFirm.setLoLimitFirm(null);
					firstLimitingFirm = firstCandidate;
				} else if (firstCandidate != null && !firstAndLast(nextFirm))
					removeLimitingLinks(nextFirm);

				return getHigherLimitFirm(nextNextFirm, o, firstCandidate);
			}
		}

	}

	public void removeLimitingLinks(Firm f) {
		if (sortQFirms.size() == 1) {
			// It is the unique firm
			f.setLoLimitFirm(null);
			f.setHiLimitFirm(null);
			firstLimitingFirm = null;
			return;
		}

		// connect links if it had them and reset firstLimitingFirm if necessary
		Firm loF = f.getLoLimitFirm();
		Firm hiF = f.getHiLimitFirm();

		if (firstLimitingFirm == f && hiF != null) {
			firstLimitingFirm = hiF;
			hiF.setLoLimitFirm(null);

		} else if (firstLimitingFirm == f && hiF == null) {
			// It shouldn't come here because it is the first and last
			throw new Error("It was intended to remove the first and last firm");

		} else {
			// firstLimitingFirm != f

			if (loF != null)
				loF.setHiLimitFirm(hiF);

			if (hiF != null)
				hiF.setLoLimitFirm(loF);

		}

		f.setLoLimitFirm(null);
		f.setHiLimitFirm(null);
	}

	public Firm lowestQFirm() {

		Map.Entry<Double, Firm> e = sortQFirms.firstEntry();
		return (e == null ? null : e.getValue());

	}

	public boolean isFirstLimitingFirm(Firm f) {
		return f == firstLimitingFirm;
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
		while (f != null && f.getQuality() <= q) {
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
		if (firstAndLast(f)) {
			firstLimitingFirm = null;
		} else
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
				// new OpportunisticFirm();
				// break;
			case PREMIUM:
				new PremiumFirm();
				break;
			case WAIT:
				// new WaitFirm();
				// break;
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

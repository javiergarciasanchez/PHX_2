package pHX_2;

import java.util.Map;
import java.util.TreeMap;

import repast.simphony.context.DefaultContext;
import repast.simphony.engine.schedule.ScheduledMethod;
import firmState.Offer;
import firms.Firm;

public class Segments extends DefaultContext<SegmentLimit> {

	// Firms ordered according to quality
	private TreeMap<Double, Firm> sortQFirms;
	private Firm firstLimitingFirm;

	public Segments() {
		super("Segments_Context");

		sortQFirms = new TreeMap<Double, Firm>();

	}

	@ScheduledMethod(start = 1, priority = RunPriority.CREATE_SEGMENT_LIMITS_PRIORITY, interval = 1)
	public void createSegmentsLimits() {

		clear();

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
		double firmQ = firm.getQuality();

		// First we need to remove previous links
		removeLimitingLinks(firm);

		// Get Tentative neighbors
		Firm prevF = null;
		Firm nextF = firstLimitingFirm;
		while (nextF != null && nextF.getQuality() < firmQ) {
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

		double loLimit = SegmentLimit.calcLimit(loOffer, o);
		double hiLimit = SegmentLimit.calcLimit(o, hiOffer);

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
		add(sL);
		Market.margUtilProjection.add(sL);
	}

	private Firm getLowerLimitFirm(Firm prevFirm, Offer o, boolean clean) {

		if (prevFirm == null)
			return null;
		else {

			Offer prevOffer = prevFirm.getOffer();
			double limit = SegmentLimit.calcLimit(prevOffer, o);

			Firm prevPrevFirm = prevFirm.getLoLimitFirm();
			Offer prevPrevOffer = ((prevPrevFirm == null) ? null : prevPrevFirm
					.getOffer());

			double prevLimit = SegmentLimit.calcLimit(prevPrevOffer, prevOffer);

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
			double limit = SegmentLimit.calcLimit(o, nextOffer);

			Firm nextNextFirm = nextFirm.getHiLimitFirm();
			Offer nextNextOffer = ((nextNextFirm == null) ? null : nextNextFirm
					.getOffer());

			double nextLimit = SegmentLimit.calcLimit(nextOffer, nextNextOffer);

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

	public Firm getNextQFirm(double quality) {

		Map.Entry<Double, Firm> e = sortQFirms.ceilingEntry(quality);
		return (e == null ? null : e.getValue());

	}

	public Firm getNextQFirm(Firm f) {
		return getNextQFirm(f.getQuality());
	}

	public Firm getPrevQFirm(double quality) {

		Map.Entry<Double, Firm> e = sortQFirms.floorEntry(quality);
		return (e == null ? null : e.getValue());

	}

	public Firm getPrevQFirm(Firm f) {
		return getPrevQFirm(f.getQuality());
	}

	public boolean containsQ(double q) {
		return sortQFirms.containsKey(q);
	}

	public void addToSegments(Firm f) {
		sortQFirms.put(f.getQuality(), f);
		updateLimitingFirms(f);
	}

	public void removeFromSegments(Firm f) {
		removeLimitingLinks(f);
		sortQFirms.remove(f.getQuality());
	}

}

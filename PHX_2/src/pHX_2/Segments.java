package pHX_2;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import repast.simphony.context.DefaultContext;
import repast.simphony.engine.schedule.ScheduledMethod;
import firms.Firm;

public class Segments extends DefaultContext<SegmentLimit> {

	// Firms ordered according to quality
	private ArrayList<SegmentLimit> segmentsLimits;
	private TreeMap<Double, Firm> sortQFirms;

	public Segments() {
		super("Segments_Context");

		segmentsLimits = new ArrayList<SegmentLimit>();
		sortQFirms = new TreeMap<Double, Firm>();

	}

	@ScheduledMethod(start = 1, priority = RunPriority.CREATE_SEGMENT_LIMITS_PRIORITY, interval = 1)
	public void createSegmentsLimits() {

		clearSegments();

		if (!sortQFirms.isEmpty()) {

			for (Double key : sortQFirms.navigableKeySet()) {
				addSegmentLimit(sortQFirms.get(key));
			}

			// add the last limit
			addSegmentLimit(null);

		}

	}

	private void clearSegments() {

		// Clean firm pointers
		for (SegmentLimit sL : segmentsLimits) {

			Firm loF = sL.getLowerFirm();
			Firm hiF = sL.getHigherFirm();

			if (loF != null)
				loF.setHiSegment(null);

			if (hiF != null)
				hiF.setHiSegment(null);

		}

		segmentsLimits = new ArrayList<SegmentLimit>();
		clear();

	}

	// Recursively add a limit using firm
	// It is assumed the firms are provided using an ordered by quality list of
	// firms
	private void addSegmentLimit(Firm firm) {

		if (isConsistentToDirectlyAddToSL(firm)) {
			directAddSegmentLimit(firm);
		} else {
			removeLastSegmentLimit();
			addSegmentLimit(firm);
		}

	}

	private boolean isConsistentToDirectlyAddToSL(Firm f) {

		if (f == null)
			// last firm limit
			return true;
		else {
			Firm lastF = getLastFirmOfLastSegment();
			if (lastF == null)
				// f is the first firm
				return true;
			else {
				double limit = SegmentLimit.calcLimit(lastF, f);
				double prevLimit = lastF.getSegmentLowerLimit();
				return (limit > prevLimit);
			}

		}
	}

	private Firm getLastFirmOfLastSegment() {
		if (segmentsLimits.isEmpty())
			return null;
		else
			return segmentsLimits.get(segmentsLimits.size() - 1)
					.getHigherFirm();
	}

	private void directAddSegmentLimit(Firm firm) {

		SegmentLimit sL = new SegmentLimit(getLastFirmOfLastSegment(), firm);

		segmentsLimits.add(sL);
		add(sL);
		Market.margUtilProjection.add(sL);

	}

	private void removeLastSegmentLimit() {
		int size = segmentsLimits.size();
		SegmentLimit sL;

		sL = segmentsLimits.get(size - 1);

		// remove firms' pointers
		Firm loF = sL.getLowerFirm();
		Firm hiF = sL.getHigherFirm();

		if (loF != null)
			loF.setHiSegment(null);

		if (hiF != null)
			hiF.setHiSegment(null);

		segmentsLimits.remove(size - 1);
		remove(sL);
	}

	public Firm lowestQFirm() {
		if (sortQFirms.isEmpty())
			return null;
		else
			return sortQFirms.firstEntry().getValue();
	}

	public Firm getNextQFirm(double quality) {

		Map.Entry<Double, Firm> e = sortQFirms.ceilingEntry(quality);

		if (e != null)
			return e.getValue();
		else
			return null;

	}

	public Firm getNextQFirm(Firm f) {
		return getNextQFirm(f.getQuality());
	}

	public Firm getPrevQFirm(double quality) {

		Map.Entry<Double, Firm> e = sortQFirms.floorEntry(quality);

		if (e != null)
			return e.getValue();
		else
			return null;

	}

	public Firm getPrevQFirm(Firm f) {
		return getPrevQFirm(f.getQuality());
	}

	public boolean containsQ(double q) {
		return sortQFirms.containsKey(q);
	}

	public void removeFromSegments(Firm f) {
		sortQFirms.remove(f.getQuality());		
	}

	public void addToSegments(Firm f) {
		sortQFirms.put(f.getQuality(),f);
	}

}

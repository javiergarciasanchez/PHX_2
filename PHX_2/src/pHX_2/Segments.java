package pHX_2;

import java.util.ArrayList;
import java.util.Collections;

import repast.simphony.context.DefaultContext;
import repast.simphony.engine.schedule.ScheduledMethod;
import firms.Firm;

public class Segments extends DefaultContext<SegmentLimit> {

	// Firms ordered according to quality
	private ArrayList<SegmentLimit> segmentsLimits;
	private ArrayList<Firm> sortQFirms;
	private QualityComparator qComp = new QualityComparator();

	public Segments() {
		super("Segments_Context");

		segmentsLimits = new ArrayList<SegmentLimit>();
		sortQFirms = new ArrayList<Firm>();

	}

	@ScheduledMethod(start = 1, priority = RunPriority.CREATE_SEGMENT_LIMITS_PRIORITY, interval = 1)
	public void createSegmentsLimits() {

		Collections.sort(sortQFirms, qComp);

		clearSegments();

		if (!sortQFirms.isEmpty()) {

			for (Firm f : sortQFirms) {
				addSegmentLimit(f);
			}

			// add the last limit
			addSegmentLimit(null);

		}

	}

	private void clearSegments() {
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
		Firm lastF = getLastFirmOfLastSegment();

		if (f == null)
			// lastF is the last firm
			return true;
		else if (lastF == null)
			// f is the first firm
			return true;
		else {
			double limit = SegmentLimit.calcLimit(lastF, f);
			double absMin = f.getPoorestConsumerMargUtil();
			return (limit > absMin);
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
		segmentsLimits.remove(size - 1);
		remove(sL);
	}

	public Firm lowestQFirm() {
		if (sortQFirms.isEmpty())
			return null;
		else
			return sortQFirms.get(0);
	}

	public Firm getNextQFirm(double quality) {

		for (Firm f : sortQFirms) {
			if (f.getQuality() > quality)
				return f;
		}

		return null;

	}

	public Firm getNextQFirm(Firm f) {
		return getNextQFirm(f.getQuality());
	}

	public Firm getPrevQFirm(double quality) {

		Firm retF = null;

		for (Firm f : sortQFirms) {
			if (f.getQuality() < quality)
				retF = f;
			else
				return retF;
		}

		return retF;

	}

	public Firm getPrevQFirm(Firm f) {
		return getPrevQFirm(f.getQuality());
	}

	public boolean containsQ(double q) {
		for (Firm f : sortQFirms) {
			if (f.getQuality() == q)
				return true;
			else if (f.getQuality() > q)
				return false;
		}

		return false;
	}

	public void removeFromSegments(Firm f) {
		sortQFirms.remove(f);
	}

	public void addToSegments(Firm f) {
		sortQFirms.add(f);
	}

}

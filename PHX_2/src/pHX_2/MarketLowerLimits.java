package pHX_2;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.SortedMap;
import java.util.TreeMap;

public class MarketLowerLimits {

	private static ArrayList<FirmLimit> limitsList;
	private static TreeMap<Double, FirmLimit> limitsPerQ;
	private static Hashtable<Firm, Double> limitsPerFirm;

	public MarketLowerLimits() {
		limitsList = new ArrayList<FirmLimit>();
		limitsPerQ = new TreeMap<Double, FirmLimit>();
		limitsPerFirm = new Hashtable<Firm, Double>();
	}

	public void update() {

		limitsList.clear();

		// Start recursive call with lowest quality firm
		update(Firms.lowestQFirm());

	}

	private void update(Firm f) {

		// End of recursive call
		if (f == null)
			return;

		if (limitsList.isEmpty()) {
			// Initialize limitsList
			addFirm(f, f.getPrice() / f.getQuality());

			// Start recursive call with next quality firm
			update(Firms.getNextQFirm(f));

		} else {

			FirmLimit prev;
			double limit;

			// Read last entry
			prev = limitsList.get(limitsList.size() - 1);

			limit = (f.getPrice() - prev.f.getPrice())
					/ (f.getQuality() - prev.f.getQuality());

			if (prev.limit < limit) {

				// prev keeps its market share
				addFirm(f, limit);
				update(Firms.getNextQFirm(f));

			} else {

				// prev lost its market share. Need to go backward
				removeLast();
				update(f);

			}
		}

	}

	/*
	 * Returns true if the firm has a place in the market taking into account
	 * the lower side of the market
	 */
	private boolean adjustLowerSegment(
			SortedMap<Double, FirmLimit> lowerMarket, Firm f) {

		double absLimit = f.getPrice() / f.getQuality();
		
		// End of recursive call
		if (lowerMarket.isEmpty()) {
			addFirm(f, absLimit);
			return true;
		}

		// Read previous entry
		double lastKey = lowerMarket.lastKey();
		FirmLimit prev = lowerMarket.get(lastKey);

		double limit = (f.getPrice() - prev.f.getPrice())
				/ (f.getQuality() - prev.f.getQuality());

		if (prev.limit < limit) {

			// prev keeps its market share
			addFirm(f, limit);
			return true;

		} else {

			// prev lost its market share. Need to go backward
			lowerMarket.remove(lastKey);
			return adjustLowerSegment(lowerMarket, f);

		}
	}

	// absolute limit
			

			Firm prevFirm = Firms.getPrevQFirm(this);

			if (prevFirm == null)
				return new FirmLimit(null, absLimit);

			double limit = (getPrice() - prevFirm.getPrice())
					/ (getQuality() - prevFirm.getQuality());

			Firm antePrevFirm = prevFirm.getLowerCompetitor().f;

			if (antePrevFirm == null)
				// Prev is the lowest quality firm in the market
				return new FirmLimit(prevFirm, Math.max(absLimit, limit));

			else {

				double prevLimit = (prevFirm.getPrice() - antePrevFirm.getPrice())
						/ (prevFirm.getQuality() - antePrevFirm.getQuality());

				if (prevLimit < limit)
					// prev is in the market
					return new FirmLimit(prevFirm, Math.max(absLimit, limit));

				else {
					// Prev is not in the market. Recalculate limit with antePrev
					limit = (getPrice() - antePrevFirm.getPrice())
							/ (getQuality() - antePrevFirm.getQuality());

					return new FirmLimit(antePrevFirm, Math.max(absLimit, limit));

				}

			}

	
	/*
	 * Returns true if the firm has a place in the market taking into account
	 * the higher side of the market
	 */
	private boolean adjustHigherSegment(
			SortedMap<Double, FirmLimit> higherMarket, FirmLimit target) {

		// End of recursive call
		if (higherMarket.isEmpty())
			return true;

		// Read previous entry
		double firstKey = higherMarket.firstKey();
		FirmLimit next = higherMarket.get(firstKey);

		double limit = (next.f.getPrice() - target.f.getPrice())
				/ (next.f.getQuality() - target.f.getQuality());

		if (target.limit < limit) {

			// target ends up in the market
			next.limit = limit;
			return true;
		} else {
			limitsPerQ.remove(target.f.getQuality());
			return false;
		}
	}

	private FirmLimit getPrevMarketFirm(Firm f) {
		double limit;
		Firm tmpFirm = Firms.getPrevQFirm(f);

		while (tmpFirm != null) {
			limit = limitsPerFirm.get(tmpFirm);
			if (limit != 0.0)
				return new FirmLimit(tmpFirm, limit);
			else
				tmpFirm = Firms.getPrevQFirm(tmpFirm);
		}

		// tmpFirm == null
		return null;

	}

	private boolean addFirm(Firm f, double limit) {
		limitsPerQ.put(f.getQuality(), new FirmLimit(f, limit));
		limitsPerFirm.put(f, limit);
		return limitsList.add(new FirmLimit(f, limit));
	}

	private void removeLast() {
		int lastIndex = limitsList.size() - 1;

		limitsPerFirm.put(limitsList.get(lastIndex).f, 0.0);
		limitsList.remove(lastIndex);

	}

	public double getLimitOfFirm(Firm f) {
		return limitsPerFirm.get(f);
	}

}

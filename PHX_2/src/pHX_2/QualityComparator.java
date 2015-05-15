/**
 * 
 */
package pHX_2;

import java.util.Comparator;

import firms.Firm;

public class QualityComparator implements Comparator<Firm> {

	@Override
	public int compare(Firm f1, Firm f2) {
		if (f1.equals(f2))
			return 0;
		else if (f1.getQuality() < f2.getQuality())
			return -1;
		else
			return 1;

	}

}

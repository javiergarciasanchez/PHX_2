package graphs;

import java.awt.Color;

import firmState.Offer;
import firms.Firm;
import firms.Utils;

public class SegmentLimit {
	private Firm lowerFirm, higherFirm;
	private double value;

	public SegmentLimit(Firm loF, Firm hiF) {
		this.lowerFirm = loF;
		this.higherFirm = hiF;

		if (loF == null && hiF != null) {
			value = hiF.getPoorestConsumerMargUtil();
		} else if (loF != null && hiF == null) {
			value = Double.MAX_VALUE;
		} else if (loF != null && hiF != null) {
			value = calcLimit(loF, hiF);
		} else
			// Both are null. It shouldn't come here
			value = 0.0;
	}

	public static double calcLimit(Offer loOffer, Offer hiOffer) {

		if (loOffer == null && hiOffer != null) {
			double poorest = Utils.getPoorestConsumerMargUtil(
					hiOffer.getQuality(), hiOffer.getPrice());
			return poorest;
		} else if (loOffer != null && hiOffer == null)
			return Double.MAX_VALUE;

		else if (loOffer != null && hiOffer != null) {
			double limit = (hiOffer.getPrice() - loOffer.getPrice())
					/ (hiOffer.getQuality() - loOffer.getQuality());

			double poorest = Utils.getPoorestConsumerMargUtil(
					hiOffer.getQuality(), hiOffer.getPrice());

			return Math.max(poorest, limit);
		} else
			// Both are null. It shouldn't come here
			return 0.0;

	}

	public static double calcLimit(Firm loF, Firm hiF) {
		// it is assumed that loF and hiF are not null
		double limit = (hiF.getPrice() - loF.getPrice())
				/ (hiF.getQuality() - loF.getQuality());

		return Math.max(hiF.getPoorestConsumerMargUtil(), limit);
	}

	private Color getColor() {
		if (higherFirm == null)
			return Color.BLACK;
		else
			return higherFirm.getColor();
	}

	public Firm getLowerFirm() {
		return lowerFirm;
	}

	public Firm getHigherFirm() {
		return higherFirm;
	}

	public String getLowerFirmID() {
		if (lowerFirm == null)
			return "";
		else
			return lowerFirm.getFirmID();
	}

	public String getHigherFirmID() {
		if (higherFirm == null)
			return "";
		else
			return higherFirm.getFirmID();
	}

	public String getLabel() {
		return getLowerFirmID() + " : " + getHigherFirmID();
	}

	public double getValue() {
		return value;
	}

	public int getRed() {
		return getColor().getRed();
	}

	public int getBlue() {
		return getColor().getBlue();
	}

	public int getGreen() {
		return getColor().getGreen();
	}

}

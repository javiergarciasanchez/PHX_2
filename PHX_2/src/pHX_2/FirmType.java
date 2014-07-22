package pHX_2;

import cern.jet.random.Uniform;
import repast.simphony.random.RandomHelper;

public enum FirmType {
	OPPORTUNISTIC, PREMIUM, BASE_PYRAMID, WAIT;
	
	// Create distribution for Random Firm Type to be added
	private static final Uniform firmTypeDistrib = RandomHelper.createUniform(
			1, 4);
	
	public static FirmType getRandomFirmType() {

		switch (firmTypeDistrib.nextInt()) {
		case 1:
			return FirmType.OPPORTUNISTIC;
		case 2:
			return FirmType.PREMIUM;
		case 3:
			return FirmType.BASE_PYRAMID;
		case 4:
			return FirmType.WAIT;
		}
		return null;

	}


}

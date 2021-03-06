package firmTypes;

import cern.jet.random.Uniform;
import repast.simphony.random.RandomHelper;

public enum FirmType {
	RATIONAL, EXPECTATIONS;

	public static final int FIRM_TYPES = 2;

	// Create distribution for Random Firm Type to be added
	private static Uniform firmTypeDistrib;

	public static void resetStaticVars() {
		firmTypeDistrib = RandomHelper.createUniform(1, FIRM_TYPES);
	}

	public static FirmType getRandomFirmType() {

		switch (firmTypeDistrib.nextInt()) {
		case 1:
			return FirmType.RATIONAL;
		case 2:
			return FirmType.EXPECTATIONS;

		}
		return null;

	}

}

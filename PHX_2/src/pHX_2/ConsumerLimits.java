package pHX_2;

import repast.simphony.data2.NonAggregateDataSource;

public class ConsumerLimits implements
		NonAggregateDataSource {

	@Override
	public String getId() {
		return "PoorestCustomer";
	}

	@Override
	public Class<?> getDataType() {
		return double.class;
	}

	@Override
	public Class<?> getSourceType() {
		return Firm.class;
	}

	@Override
	public Object get(Object obj) {
		Firm f = (Firm) obj;
		return f.getPrice()/f.getQuality();
	}

}

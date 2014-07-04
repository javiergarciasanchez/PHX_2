package pHX_2;

public class FirmState {
	Product product;
	double profit = 0.;
	int demand = 0;
	Market market;

	public FirmState(Market market) {
		product = new Product(market);
		this.market = market;
	}

	public double productX() {
		return product.getX();
	}

	public double productY() {
		return product.getY();
	}

}

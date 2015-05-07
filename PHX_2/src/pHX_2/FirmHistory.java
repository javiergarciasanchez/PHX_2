package pHX_2;

import java.util.ArrayList;

public class FirmHistory extends ArrayList<FirmState> {

	public static final int HISTORY_SIZE = 2;
	
	private static final long serialVersionUID = 1L;

	public FirmHistory(int historySize) {
		super(historySize);
	}

	public boolean addCurrentState(FirmState firmState) {
		if (size() == HISTORY_SIZE)
			remove(0);
	
		return add(firmState);
	}

	public FirmState getCurrentState() {
		if (isEmpty())
			return null;
		else
			return get(size() - 1);
	}

	public FirmState getPreviousState() {
		if (isEmpty() || size() < 2)
			return null;
		else
			return get(size() - 2);
	}
}
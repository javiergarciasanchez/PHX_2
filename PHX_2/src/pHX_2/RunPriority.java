package pHX_2;

public class RunPriority {
	// Higher priority means being executed earlier

	// Executed only once
	public static final double SCHEDULE_RECESSIONS_PRIORITY = 100;

	// Every step
	public static final double MAKE_OFFER_PRIORITY = 90;
	public static final double ADD_FIRMS_PRIORITY = 80;
	public static final double CHOOSE_FIRM_PRIORITY = 70;														
	public static final double NEXT_STEP_FIRM_PRIORITY = 60;
	public static final double KILL_FIRMS_PRIORITY = 50;
	public static final double UPDATE_MKT_Q_PER_D_PRIORITY = 30; 
	public static final double CREATE_SEGMENT_LIMITS_PRIORITY = 40;
	public static final double UPDATE_PROJECTIONS_PRIORITY = 20;



}

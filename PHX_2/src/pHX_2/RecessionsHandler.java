package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;

public class RecessionsHandler {
	private static double recessionMagnitude = 0.0;
	
	
	public RecessionsHandler(Context<Object> context) {
		context.add(this);
	}

	@ScheduledMethod(start = 1, priority = RunPriority.SCHEDULE_RECESSIONS_PRIORITY)
	public void scheduleRecessions() {
		double[] start, dur, recesMag;

		// Read start of recessions
		String[] tmp = ((String) GetParameter("recessionStart")).split(":");
		start = new double[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			start[i] = new Double(tmp[i]);
		}

		// Read Duration of recessions
		tmp = ((String) GetParameter("recessionDuration")).split(":");
		dur = new double[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			dur[i] = new Double(tmp[i]);
		}

		// Read magnitude of recessions
		tmp = ((String) GetParameter("recessionMagnitude")).split(":");
		recesMag = new double[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			recesMag[i] = new Double(tmp[i]);
		}

		// Schedule recessions
		for (int i = 0; i < tmp.length; i++) {
			ISchedule sch = RunEnvironment.getInstance().getCurrentSchedule();

			// Set start
			ScheduleParameters params = ScheduleParameters.createOneTime(
					start[i], ScheduleParameters.FIRST_PRIORITY);
			sch.schedule(params, this, "setRecesMagnitude", recesMag[i]);

			// Set end
			params = ScheduleParameters.createOneTime((start[i] + dur[i])
					, ScheduleParameters.FIRST_PRIORITY);
			sch.schedule(params, this, "setRecesMagnitude", 0.0);

		}

	}

	public static void setRecesMagnitude(double mag) {
		recessionMagnitude = mag;
	}

	public static double getRecesMagnitude() {
		return recessionMagnitude;
	}


}

package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.util.ArrayList;
import java.util.List;

import pHX_2.Tools.FirmType;
import cern.jet.random.Gamma;
import cern.jet.random.Uniform;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.collections.IndexedIterable;

public class Market {

	Context<Object> context;

	public Product substitute;

	// The product reference for firms is average of mkt
	public Product referenceProductForFirms;

	Uniform margUtilOfQualityDistrib;
	Gamma fixedCostDistrib;
	Gamma priceStepDistrib;

	Gamma qualityStepDistrib;

	// Create distribution for Random Firm Type to be added
	final private Uniform firmTypeDistrib = RandomHelper.createUniform(1, 4);

	public Market(Context<Object> context) {

		this.context = context;
		context.add(this);

		// Create distributions for consumer's utility parameters
		margUtilOfQualityDistrib = RandomHelper.createUniform(
				Consumer.MinMargUtilOfQuality, Consumer.MaxMargUtilOfQuality);

		// We use Gamma distribution because the domain is > 0
		// Create distribution for firms fixed cost
		double mean = (Double) GetParameter("fixedCostMean");
		double stdDevPercent = (Double) GetParameter("fixedCostStdDevPerc");
		double alfa = (1 / Math.pow(stdDevPercent, 2));
		double beta = mean / alfa;
		fixedCostDistrib = RandomHelper.createGamma(alfa, beta);
				

		// Create distributions for strategic % steps on price and quality
		mean = (Double) GetParameter("priceStepMean");
		stdDevPercent = (Double) GetParameter("priceStepStdDevPerc");
		alfa = (1 / Math.pow(stdDevPercent, 2));
		beta = mean / alfa;
		priceStepDistrib = RandomHelper.createGamma(alfa, beta);

		mean = (Double) GetParameter("qualityStepMean");
		stdDevPercent = (Double) GetParameter("qualityStepStdDevPerc");
		alfa = (1 / Math.pow(stdDevPercent, 2));
		beta = mean / alfa;
		qualityStepDistrib = RandomHelper.createGamma(alfa, beta);

		// Read substitute product & set reference product form firms
		substitute = new Product(this);
		substitute.setPrice((Double) GetParameter("substitutePrice"));
		substitute.setQuality((Double) GetParameter("substituteQuality"));

		referenceProductForFirms = new Product(this);
		referenceProductForFirms.setPrice(substitute.getPrice());
		referenceProductForFirms.setQuality(substitute.getQuality());

	}

	private void removeFirm(Firm f) {
		context.remove(f);
	}

	@ScheduledMethod(start = 1d, priority = ScheduleParameters.FIRST_PRIORITY)
	public void initialStep() {

		addConsumers();
		addFirms();
		initialOffers();
	}

	@ScheduledMethod(start = 1d, interval = 1d)
	public void step() {

		// First offer has already been done

		chooseFirmAndNextStepConsumers();

		// Next Step is not needed for Consumers because it is done in purchases
		nextStepAllFirms();

		nextOffers();

	}

	private void addConsumers() {

		for (int i = 1; i <= (Integer) GetParameter("numberOfConsumers"); i++) {
			new Consumer(this);
		}

	}

	private void addFirms() {

		for (int i = 1; i <= (Integer) GetParameter("potencialFirmsPerPeriod"); i++) {

			switch (randomFirmType()) {
			case OPPORTUNISTIC:
				new OpportunisticFirm(this);
				break;
			case PREMIUM:
				new PremiumFirm(this);
				break;
			case BASE_PYRAMID:
				new BasePyramidFirm(this);
				break;
			case WAIT:
				new WaitFirm(this);
			}
		}

	}

	private FirmType randomFirmType() {

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

	private void initialOffers() {

		for (Object f : context.getObjects(Firm.class))
			((Firm) f).makeInitialOffer();

	}

	private void nextOffers() {

		for (Object f : context.getObjects(Firm.class))
			((Firm) f).makeOffer();

	}

	private void chooseFirmAndNextStepConsumers() {
		// Each consumer Chooses a firm and increments the chosen firm demand
		for (Object c : context.getObjects(Consumer.class)) {
			Firm f = ((Consumer) c).chooseFirmAndNextStep();
			if (f != null)
				f.firmState.demand += 1;
		}

	}

	// Calls each firm next step (to keep history and to calculate profit),
	// kills firms to be killed and sets reference product for firms
	private void nextStepAllFirms() {
		double sumPrice = 0.;
		double sumQuality = 0.;
		int size = 0;

		IndexedIterable<Object> firms = context.getObjects(Firm.class);

		List<Firm> toKill = new ArrayList<Firm>(firms.size());

		for (Object f : firms) {

			((Firm) f).nextStep();

			if (((Firm) f).isToBeKilled())
				toKill.add((Firm) f);
			else {
				sumPrice += ((Firm) f).history[0].product.getPrice();
				sumQuality += ((Firm) f).history[0].product.getQuality();
				size++;
			}

		}

		referenceProductForFirms.setPrice(sumPrice / size);
		referenceProductForFirms.setQuality(sumQuality / size);

		for (Firm f : toKill)
			removeFirm(f);

	}

}

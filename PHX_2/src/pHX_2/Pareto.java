package pHX_2;

import repast.simphony.random.RandomHelper;
import cern.jet.random.AbstractContinousDistribution;
import cern.jet.random.Exponential;

public class Pareto extends AbstractContinousDistribution {
	
	private static final long serialVersionUID = 1L;
	private Exponential implicitDistrib;
	private double minimum;
	
	Pareto(double lambda, double minimum){
		this.minimum = minimum;
		implicitDistrib = RandomHelper.createExponential(lambda);
	}
	
	public static Pareto getPareto(double lambda, double minimum){
		return new Pareto(lambda, minimum);
	}
	
	public double nextDouble(){
		return minimum * Math.exp(implicitDistrib.nextDouble());
	}

}

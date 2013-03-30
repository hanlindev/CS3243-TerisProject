// Optimize feature weights using PSO
import net.sourceforge.jswarm_pso.*;
import java.util.concurrent.*;
import java.util.*;
public class Optimizer {
	static double[] parameters = {-3.3200740, 2.70317569, -2.7157289, -5.1061407, -6.9380080, -2.4075407, -1.0};//for debugging
	static public void main(String[] args) {
		MyFitnessFunction player = new MyFitnessFunction(64);
		System.out.println("Fitness: " + player.evaluate(parameters));
	}
}

class MyFitnessFunction extends FitnessFunction {
	private int numProcess;
	ForkJoinPool mainPool;
	public MyFitnessFunction(int numCores) {
		// Simulating a block
		numProcess = numCores;
		setMaximize(true);
		mainPool = new ForkJoinPool(numCores);
	}
	
	@Override
	
	public double evaluate(double[] position) {
		double rv = 0D;
		ArrayList<Future<FitParameters>> futureList = new ArrayList<Future<FitParameters>>();
		for (int i = 0; i < numProcess; ++i) {
			futureList.add(mainPool.submit(new PlayerSkeletonUltimate(position)));
		}
		for (int i = 0; i < numProcess; ++i) {
			FitParameters aParam = new FitParameters();
			try {
				aParam = futureList.get(i).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			rv += calcFitness(aParam);
		}
		return rv;
	}
	
	public double calcFitness(FitParameters p) {
		double rv = p.L;
		rv -= ((p.Pmax - p.Pavg) / p.Pmax) * 500D;
		rv -= ((p.Hmax - p.Havg) / p.Hmax) * 500D;
		rv -= ((p.Rmax - p.Ravg) / p.Rmax) * 500D;
		rv -= ((p.Cmax - p.Cavg) / p.Cmax) * 500D;
		return rv;
	}
	
}
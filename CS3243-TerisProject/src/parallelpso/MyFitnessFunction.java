package parallelpso;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import Optimizer.FitParameters;
import Optimizer.Optimizer;
import Optimizer.PlayerSkeletonUltimate;

import net.sourceforge.jswarm_pso.FitnessFunction;
import net.sourceforge.jswarm_pso.Particle;

public class MyFitnessFunction extends FitnessFunction implements Callable<Double> {
	private int numProcess;
	ForkJoinPool mainPool;
	Particle toBeEvaluatedByCall = null;
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
			futureList.add(mainPool.submit(new PlayerSkeletonUltimate(position, Optimizer.iter, i)));
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

	@Override
	public Double call() throws Exception {
		if (toBeEvaluatedByCall == null) {
			throw new RuntimeException("Nothing to evaluate! Remember to set the position before submitting.");
		} else {
			double rv = this.evaluate(toBeEvaluatedByCall.getPosition());
			toBeEvaluatedByCall.setFitness(rv, super.isMaximize());
			toBeEvaluatedByCall = null;
			return rv;
		}
	}
	
	public void setParticle(Particle p) {
		this.toBeEvaluatedByCall = p;
	}
	
	public MyFitnessFunction getInstance() {
		return new MyFitnessFunction(numProcess);
	}
}
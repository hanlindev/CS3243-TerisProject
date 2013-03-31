// Optimize feature weights using PSO
import net.sourceforge.jswarm_pso.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.*;
public class Optimizer {
	public static int iter;
	static public double[] parameters = {-3.3200740, 2.70317569, -2.7157289, -5.1061407, -6.9380080, -2.4075407, -1.0};
	static public void main(String[] args) throws Exception{
		int numProcess = Integer.parseInt(args[0]);
		int iterations = Integer.parseInt(args[1]);
		System.out.println(numProcess);//for debugging
		BufferedWriter bw = new BufferedWriter(new FileWriter("bestPositions.txt"));
		MyParticle aParticle = new MyParticle();
		Swarm swarm = new Swarm(25, aParticle, new MyFitnessFunction(numProcess));
		double inertia = 0.72, particleInc = 1.42, globalInc = 1.42, maxVelocity = 0.5;
		
		swarm.setInertia(inertia);
		swarm.setParticleIncrement(particleInc);
		swarm.setGlobalIncrement(globalInc);
		swarm.setMaxMinVelocity(maxVelocity);
		swarm.setMaxPosition(100);
		swarm.setMinPosition(-100);
		
		for (int i = 0; i < iterations; ++i) {
			iter = i;
			swarm.evolve();
			double[] bestPosition = swarm.getBestPosition();
			String comma = "", out = "";
			for (int j = 0; j < bestPosition.length; ++j) {
				out += comma + bestPosition[j];
				comma = ", ";
			}
			out += "\n";
			bw.write(out);
			bw.flush();
		}
		bw.write(swarm.toStringStats() + "\n");
		bw.flush();
		bw.close();
		System.out.println(swarm.toStringStats());
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
}

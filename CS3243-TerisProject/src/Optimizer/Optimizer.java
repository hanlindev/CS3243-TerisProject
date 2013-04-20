package Optimizer;
// Optimize feature weights using PSO
import net.sourceforge.jswarm_pso.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.*;

import parallelpso.*;
public class Optimizer {
	public static int iter;
	// 6.239557155805777, 2.91492176463156, -2.28598349057279, 4.839667743078915, -6.896671105933235, -4.663515769040759, 1.7939425031108065
	// -9.27781317041968, 2.9767865722222666, -9.807341524018167, 4.952981979996906, -4.704796885639986, -4.303930747397512, 7.377104348874285

	static public double[] parameters = {-3.3200740, 2.70317569, -2.7157289, -5.1061407, -6.9380080, -2.4075407, -1.0};
	static public void main(String[] args) throws Exception{
		int numProcess = Integer.parseInt(args[0]);
		int iterations = Integer.parseInt(args[1]);
		String path = args[2];
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		MyParticle aParticle = new MyParticle();
		ParallelSwarm swarm = new ParallelSwarm(30, aParticle, new MyFitnessFunction(numProcess), numProcess);
		double inertia = 0.72, particleInc = 1.42, globalInc = 1.42, maxVelocity = 0.5;
		
		swarm.setInertia(inertia);
		swarm.setParticleIncrement(particleInc);
		swarm.setGlobalIncrement(globalInc);
		swarm.setMaxMinVelocity(maxVelocity);
		swarm.setMaxPosition(10);
		swarm.setMinPosition(-10);
		
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
		// Shutdown protocal
		swarm.mainPool.shutdown();
		for (MyFitnessFunction fi : swarm.particleFitnessFunction) {
			fi.mainPool.shutdown();
		}
		bw.write(swarm.toStringStats() + "\n");
		bw.flush();
		bw.close();
		System.out.println(swarm.toStringStats());
	}
}

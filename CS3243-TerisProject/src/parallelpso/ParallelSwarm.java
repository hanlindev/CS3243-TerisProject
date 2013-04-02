package parallelpso;
import java.util.concurrent.*;
import net.sourceforge.jswarm_pso.*;
import net.sourceforge.jswarm_pso.Particle;
import java.util.*;

public class ParallelSwarm extends Swarm{
	ForkJoinPool mainPool;
	MyFitnessFunction sampleFitnessFunction;
	// TODO add a list of fitness functions

	public ParallelSwarm(int numberOfParticles, Particle sampleParticle,
			MyFitnessFunction fitnessFunction) {
		super(numberOfParticles, sampleParticle, fitnessFunction);
		sampleFitnessFunction = fitnessFunction;
	}
	
	public ParallelSwarm(int numberOfParticles, Particle sampleParticle,
			FitnessFunction fitnessFunction, int numProcess) {
		super(numberOfParticles, sampleParticle, fitnessFunction);
		mainPool = new ForkJoinPool(numProcess * numberOfParticles * 5);
	}
	
	@Override
	/**
	 * added parallel calls to fitness functions
	 */
	public void evaluate() {
		Particle[] particles = super.getParticles();
		FitnessFunction fitnessFunction = super.getFitnessFunction();
		Neighborhood neighborhood = super.getNeighborhood();
		Particle sampleParticle = super.getSampleParticle();
		
		double bestFitness = super.getBestFitness();
		int bestParticleIndex = super.getBestParticleIndex();
		int numberOfEvaliations = super.getNumberOfEvaliations();
		double[] bestPosition = super.getBestPosition();
		if (particles == null) throw new RuntimeException("No particles in this swarm! May be you need to call Swarm.init() method");
		if (fitnessFunction == null) throw new RuntimeException("No fitness function in this swarm! May be you need to call Swarm.setFitnessFunction() method");

		// Initialize
		if (Double.isNaN(bestFitness)) {
			bestFitness = (fitnessFunction.isMaximize() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
			bestParticleIndex = -1;
		}

		//---
		// Evaluate each particle (and find the 'best' one)
		//---
		ArrayList<Future<Double>> futureList = new ArrayList<Future<Double>>(super.getNumberOfParticles());
		for (int i = 0; i < particles.length; ++i) {
			// TODO implement
		}
		for (int i = 0; i < particles.length; i++) {
			// Evaluate particle
			double fit = fitnessFunction.evaluate(particles[i]);

			numberOfEvaliations++; // Update counter

			// Update 'best global' position
			if (fitnessFunction.isBetterThan(bestFitness, fit)) {
				bestFitness = fit; // Copy best fitness, index, and position vector
				bestParticleIndex = i;
				if (bestPosition == null) bestPosition = new double[sampleParticle.getDimension()];
				particles[bestParticleIndex].copyPosition(bestPosition);
			}

			// Update 'best neighborhood' 
			if (neighborhood != null) {
				neighborhood.update(this, particles[i]);
			}

		}
	}

}
